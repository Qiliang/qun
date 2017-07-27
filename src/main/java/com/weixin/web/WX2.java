package com.weixin.web;

import com.weixin.domain.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Controller
@RequestMapping("/api")
public class WX2 implements InitializingBean {


    @Autowired
    private ExecutorService executorService;


    @Autowired
    SendHistoryRepository sendHistoryRepository;
    @Autowired
    SendHistoryDetailRepository sendHistoryDetailRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MassConfigRepository wxConfigRepository;

    private Map<String, WebWeixin> drivers = new ConcurrentHashMap<>();

    @Value("${test}")
    private boolean test = true;

    //private PhantomJSDriver webDriver;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @PreAuthorize("hasAuthority('USER') OR hasAuthority('USER2') OR hasAuthority('EMPLOYEE')")
    @RequestMapping("/qr")
    @ResponseBody
    public Map<String, Object> qr(HttpServletResponse response) throws IOException, URISyntaxException {
        WebWeixin wx = new WebWeixin();
        String id = System.currentTimeMillis() + "";
        drivers.put(id, wx);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("qr", wx.qrGen());
        executorService.execute(() -> wx.waitForScan());

        return result;
    }


    @PreAuthorize("hasAuthority('USER') OR hasAuthority('USER2') OR hasAuthority('EMPLOYEE')")
    @RequestMapping("/state/{id}")
    @ResponseBody
    public String state(@PathVariable String id, HttpServletResponse response) throws IOException {
        WebWeixin wx = drivers.get(id);
        if(wx==null)return "none";
        return wx.isScan() ? "block" : "none";
    }

    private String getCopyright(String username) {
        User user = userRepository.findByUsername(username);
        if (user.getRole().toString().equals(User.ROLE.EMPLOYEE.toString())) {
            return getCopyright(user.getEmployer());
        } else if (user.getRole().toString().equals(User.ROLE.USER.toString())) {
            return StringUtils.isBlank(user.getCopyright()) ? HomeController.COPYRIGHT : user.getCopyright();
        } else if (user.getRole().toString().equals(User.ROLE.USER2.toString())) {
            return StringUtils.isBlank(user.getParent()) ? HomeController.COPYRIGHT : getCopyright(user.getParent());
        }
        return HomeController.COPYRIGHT;
    }

    @PreAuthorize("hasAuthority('USER') OR hasAuthority('USER2') OR hasAuthority('EMPLOYEE')")
    @RequestMapping("/contact/{id}/{wxConfigId}")
    @ResponseBody
    public String contact(@PathVariable String id, @PathVariable Integer wxConfigId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getUserPrincipal().getName();
        String copyright = getCopyright(username);

        final MassConfig wxConfig = wxConfigRepository.findOne(wxConfigId);
        if (StringUtils.isNotBlank(wxConfig.getText())) {
            wxConfig.setText(wxConfig.getText() + "--" + (StringUtils.isBlank(copyright) ? "" : copyright));
        } else {
            wxConfig.setText((StringUtils.isBlank(copyright) ? "" : copyright));
        }
        wxConfig.setOperator(username);
        executorService.execute(() -> contract(id, wxConfig));
        return "accept";
    }

    private void contract(String id, MassConfig wxConfig) {
        WebWeixin wx = drivers.get(id);
        SendHistory sendHistory = new SendHistory();
        try {
            wx.initUser();
            sendHistory.setConfigName(wxConfig.getTitle());
            sendHistory.setUser(wxConfig.getUsername());
            sendHistory.setOperator(wxConfig.getOperator());
            sendHistory.setCreateTime(new Date());
            sendHistory.setWxName(wx.getUser().get("UserName").toString());
            sendHistory.setWxNick(wx.getUser().get("NickName").toString());
            sendHistoryRepository.save(sendHistory);
            List<Map<String, Object>> friends = wx.getContracts();
            if ("1".equals(wxConfig.getType())) {
                wx.sendText(friends, wxConfig, f -> {
                    saveToDb(sendHistory, f);
                });
            } else if ("2".equals(wxConfig.getType())) {
                wx.sendImage(friends, wxConfig, f -> {
                    saveToDb(sendHistory, f);
                });
            } else if ("3".equals(wxConfig.getType())) {
                wx.sendTextImage(friends, wxConfig, f -> {
                    saveToDb(sendHistory, f);
                });
            }
            sendHistoryRepository.save(sendHistory);
            wx.logout();
            wx.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            sendHistoryRepository.save(sendHistory);
        }
    }

    private void saveToDb(SendHistory sendHistory, Map<String, Object> friend) {
        SendHistoryDetail sendHistoryDetail = new SendHistoryDetail();
        sendHistoryDetail.setFromName(sendHistory.getWxName());
        sendHistoryDetail.setFromNick(sendHistory.getWxNick());
        sendHistoryDetail.setToName(friend.get("UserName").toString());
        sendHistoryDetail.setToNick(friend.get("NickName").toString());
        sendHistoryDetail.setSendTime(new Date());
        sendHistoryDetail.setHistoryId(sendHistory.getId());
        sendHistoryDetailRepository.save(sendHistoryDetail);
        sendHistory.setCount(sendHistory.getCount() + 1);
    }

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        for (String key : drivers.keySet()) {
            long timestamp = Long.valueOf(key);
            if ((System.currentTimeMillis() - timestamp) / 1000 > 3600) {
                drivers.get(key).close();
                drivers.remove(key);
            }
        }
    }


}
