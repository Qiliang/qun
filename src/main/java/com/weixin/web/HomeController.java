package com.weixin.web;

import com.weixin.domain.User;
import com.weixin.domain.UserRepository;
import com.weixin.domain.WxConfig;
import com.weixin.domain.WxConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    WxConfigRepository wxConfigRepository;

    @RequestMapping("/qrcode")
    public String qrcode() {
        return "qrcode";
    }


    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping("/403")
    public String forbidden() {
        return "403";
    }


    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping({"/admin-index","/",""})
    public String adminIndex(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        return "admin-index";
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("/admin-config")
    public String adminConfig(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        user.setWxConfigs(wxConfigRepository.findByUsername(username));
        m.addAttribute("user", user);
        return "admin-config";
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping(value = "/admin-config", method = RequestMethod.POST)
    public String saveAdminConfig(MultipartHttpServletRequest request, Model m) throws IOException {
        String username = request.getUserPrincipal().getName();
        MultipartFile img = request.getFile("img");
        String txt = request.getParameter("txt");
        String wxConfigId = request.getParameter("wxConfigId");
        WxConfig wxConfig = wxConfigRepository.findOne(Integer.valueOf(wxConfigId));
        if (!StringUtils.isEmpty(txt)) wxConfig.setText(txt);
        if (img.getBytes() != null && img.getBytes().length > 0)
            wxConfig.setImage(Base64.getEncoder().encodeToString(img.getBytes()));
        wxConfigRepository.save(wxConfig);
        User user = userRepository.findByUsername(username);
        user.setWxConfigs(wxConfigRepository.findByUsername(username));
        userRepository.save(user);
        m.addAttribute("user", user);
        return "admin-config";
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("/admin-qrcode")
    public String adminqrcode(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        user.setWxConfigs(wxConfigRepository.findByUsername(username));
        m.addAttribute("user", user);
        return "admin-qrcode";
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("/admin-help")
    public String adminHelp(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        return "admin-help";
    }


    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("/admin-stat")
    public String adminstat(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        return "admin-stat";
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("/admin-users")
    public String adminusers(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        return "admin-users";
    }

}