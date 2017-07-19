package com.weixin.web;

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
import java.util.List;

@Controller
public class AdminController {


    @Autowired
    UserRepository userRepository;

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


        List<User> users = userRepository.findByRole(User.ROLE.USER);
        m.addAttribute("users", users);
        return "admin/admin-users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping({"/admin-users"})
    public String adminUsersPost(@RequestParam String action, User user, HttpServletRequest request, Model m) {
        String username = request.getUserPrincipal().getName();
        m.addAttribute("user", userRepository.findByUsername(username));

        User otUser = userRepository.findOne(user.getId());
        if ("禁用".equals(action)) {
            otUser.setActive(false);
        } else if ("激活".equals(action)) {
            otUser.setActive(true);
        }
        userRepository.save(otUser);

        m.addAttribute("users", userRepository.findByRole(User.ROLE.USER));
        return "admin/admin-users";
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
}
