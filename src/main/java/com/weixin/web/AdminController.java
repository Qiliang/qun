package com.weixin.web;

import com.weixin.domain.SystemConfig;
import com.weixin.domain.SystemConfigRepository;
import com.weixin.domain.User;
import com.weixin.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import static com.weixin.web.HomeController.COPYRIGHT;

@Controller
public class AdminController {


    @Autowired
    UserRepository userRepository;

    @Autowired
    SystemConfigRepository systemConfigRepository;

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping({"/admin-index"})
    public String adminIndex(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
//        if (user.getRole().toString().equals(User.ROLE.EMPLOYEE.toString())) {
//            return setMessage(request, m);
//        }
        return "admin/admin-index";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping({"/admin-users"})
    public String adminUsers(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);


        m.addAttribute("users", userRepository.findByRoleOrRoleOrRole(User.ROLE.USER, User.ROLE.USER2, User.ROLE.INIT));
        return "admin/admin-users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping({"/admin-users"})
    public String adminAddUserPost(@RequestParam String action, User user, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        m.addAttribute("user", userRepository.findByUsername(username));

        User otUser = userRepository.findOne(user.getId());
        if ("禁用".equals(action)) {
            otUser.setActive(false);
        } else if ("启用".equals(action)) {
            otUser.setActive(true);
        } else if ("激活为企业用户".equals(action)) {
            otUser.setActive(true);
            otUser.setRole(User.ROLE.USER);
        } else if ("激活为个人用户".equals(action)) {
            otUser.setActive(true);
            otUser.setRole(User.ROLE.USER2);
        }
        userRepository.save(otUser);

        m.addAttribute("users", userRepository.findByRoleOrRoleOrRole(User.ROLE.USER, User.ROLE.USER2, User.ROLE.INIT));
        return "admin/admin-users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping({"/admin-addUser"})
    public String adminUsersPost(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        m.addAttribute("user", userRepository.findByUsername(username));
        return "admin/admin-addUser";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping({"/admin-addUser"})
    public String adminAddUser(User member, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        m.addAttribute("user", userRepository.findByUsername(username));

        if (userRepository.findByUsername(member.getUsername()) != null) {
            m.addAttribute("error", "用登录名已存在");
            m.addAttribute("member", member);
            return "admin/admin-addUser";
        }

        member.setRole(User.ROLE.INIT);
        member.setParent("");
        member.setCopyright(COPYRIGHT);
        member.setActive(false);
        userRepository.save(member);

        return adminUsers(request, m);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping({"/admin-help"})
    public String adminHelp(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);


        return "admin/admin-help";
    }

    @RequestMapping({"/admin-login"})
    public String adminLogin(HttpServletRequest request, Model m) {
        return "admin/admin-login";
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping({"/admin-config"})
    public String adminConfig(HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        m.addAttribute("config", systemConfigRepository.getOne(0));
        return "admin/admin-config";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping({"/admin-config"})
    public String adminConfigPost(SystemConfig config, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username);
        m.addAttribute("user", user);
        systemConfigRepository.save(config);
        return "admin/admin-index";
    }
}
