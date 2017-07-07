package com.weixin.web;

import com.weixin.domain.MassConfig;
import com.weixin.domain.MassConfigRepository;
import com.weixin.domain.User;
import com.weixin.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

@Controller
public class HomeController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    MassConfigRepository wxConfigRepository;

    @RequestMapping("/qrcode")
    public String qrcode() {
        return "qrcode";
    }


    @GetMapping(value = "/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/403")
    public String forbidden() {
        return "403";
    }


    @PreAuthorize("hasAuthority('USER') OR hasAuthority('EMPLOYEE')")
    @RequestMapping({"/", ""})
    public String adminIndex(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
//        if (user.getRole().toString().equals(User.ROLE.EMPLOYEE.toString())) {
//            return setMessage(request, m);
//        }
        return "index";
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("/admin-config")
    public String adminConfig(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        // user.setWxConfigs(wxConfigRepository.findByUsername(username));
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
        MassConfig wxConfig = wxConfigRepository.findOne(Integer.valueOf(wxConfigId));
        if (!StringUtils.isEmpty(txt)) wxConfig.setText(txt);
        if (img.getBytes() != null && img.getBytes().length > 0)
            wxConfig.setImage(Base64.getEncoder().encodeToString(img.getBytes()));
        wxConfigRepository.save(wxConfig);
        User user = userRepository.findByUsername(username);
        userRepository.save(user);
        m.addAttribute("user", user);
        return "admin-config";
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("/admin-qrcode")
    public String adminqrcode(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
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

    @PreAuthorize("hasAuthority('USER') OR hasAuthority('EMPLOYEE')")
    @RequestMapping("/setMessage")
    public String setMessage(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        if (user.getRole().toString().equals(User.ROLE.EMPLOYEE.toString())) {
            m.addAttribute("massList", wxConfigRepository.findByUsername(user.getEmployer()));
        } else {
            m.addAttribute("massList", wxConfigRepository.findByUsername(username));
        }
        m.addAttribute("user", user);
        return "setMessage";
    }

    @PreAuthorize("hasAuthority('USER') OR hasAuthority('EMPLOYEE')")
    @GetMapping("/accountSet")
    public String accountSet(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        return "accountSet";
    }

    @PreAuthorize("hasAuthority('USER') OR hasAuthority('EMPLOYEE')")
    @PostMapping("/accountSet")
    public String accountSetPost(User member, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.getOne(member.getId());
        user.setUsername(member.getUsername());
        user.setDisplayName(member.getDisplayName());
        user.setPassword(member.getPassword());
        user.setTel(member.getTel());
        userRepository.save(user);
        m.addAttribute("user", user);
        return "/";
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/memberList")
    public String memberList(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        m.addAttribute("employees", userRepository.findByEmployer(username));
        return "memberList";
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping({"/memberAdd", "/memberAdd/{id}"})
    public String memberAdd(@PathVariable(required = false) Integer id, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        if (id != null) m.addAttribute("employee", userRepository.findOne(id));
        m.addAttribute("user", user);
        return "memberAdd";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping({"/memberAdd"})
    public String memberAddPost(User member, @RequestParam(required = false) String action, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        if ("删除".equals(action)) {
            userRepository.delete(member.getId());
            m.addAttribute("employees", userRepository.findByEmployer(username));
            return "memberList";
        } else {
            if (member.getId() != null || userRepository.findByUsername(member.getUsername()) == null) {
                member.setEmployer(username);
                member.setRole(User.ROLE.EMPLOYEE);
                userRepository.save(member);
                m.addAttribute("employees", userRepository.findByEmployer(username));
                return "memberList";
            }
            m.addAttribute("message", "登录账号重复");
            m.addAttribute("employee", member);
            return "memberAdd";
        }



    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("/massList")
    public String massList(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        m.addAttribute("massList", wxConfigRepository.findByUsername(username));
        return "massList";
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping({"/massSet/{massSetId}", "/massSet"})
    public String massSet(@PathVariable(required = false) Integer massSetId, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        if (massSetId == null) return "massSet";
        m.addAttribute("massSet", wxConfigRepository.findOne(massSetId));
        return "massSet";
    }

    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping({"/massSet/{massSetId}"})
    public String massSetDelete(@PathVariable Integer massSetId, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        wxConfigRepository.delete(massSetId);
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        m.addAttribute("massList", wxConfigRepository.findByUsername(username));
        return "massList";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/massSet")
    public String massSetPost(MultipartHttpServletRequest request, Model m) throws IOException {
        String username = request.getUserPrincipal().getName();

        String id = request.getParameter("id");
        String massText = request.getParameter("massText");
        String massTitle = request.getParameter("massTitle");
        String massType = request.getParameter("massType");
        MassConfig massConfig = new MassConfig();
        if (!StringUtils.isEmpty(id)) {
            massConfig = wxConfigRepository.findOne(Integer.parseInt(id));
        }
        massConfig.setTitle(massTitle);
        massConfig.setUsername(username);
        massConfig.setType(massType);
        if ("1".equals(massType)) {
            massConfig.setText(massText);
            massConfig.setImage(null);
        } else if ("2".equals(massType)) {
            massConfig.setText(null);
            StringBuffer s = new StringBuffer();
            for (MultipartFile file : request.getFiles("file")) {
                s.append(Base64.getEncoder().encodeToString(file.getBytes())).append("\t");
            }
            massConfig.setImage(s.toString());
        } else if ("3".equals(massType)) {
            massConfig.setText(massText);
            StringBuffer s = new StringBuffer();
            for (MultipartFile file : request.getFiles("file")) {
                s.append(Base64.getEncoder().encodeToString(file.getBytes())).append("\t");
            }
            massConfig.setImage(s.toString());
        }
        wxConfigRepository.save(massConfig);
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        m.addAttribute("massList", wxConfigRepository.findByUsername(username));
        return "massList";
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/subordinates")
    public String subordinates(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        m.addAttribute("proxies", userRepository.findByParent(username));
        return "subordinates";
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping({"/subordinatesAdd", "/subordinatesAdd/{id}"})
    public String subordinatesAdd(@PathVariable(required = false) Integer id, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        if (id != null) m.addAttribute("proxy", userRepository.findOne(id));
        m.addAttribute("user", user);
        return "subordinatesAdd";
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping({"/subordinatesAdd"})
    public String subordinatesAddPost(User member, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        if (member.getId() != null || userRepository.findByUsername(member.getUsername()) == null) {
            member.setActive(false);
            member.setRole(User.ROLE.USER);
            member.setParent(username);
            userRepository.save(member);
            m.addAttribute("proxies", userRepository.findByParent(username));
            return "subordinates";
        }
        m.addAttribute("message", "登录账号重复");
        m.addAttribute("proxy", member);
        return "subordinates";

    }





}