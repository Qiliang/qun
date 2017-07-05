package com.weixin.domain;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DataInit {

    @Autowired
    UserRepository userRepository;
    @Autowired
    WxConfigRepository wxConfigRepository;

    @PostConstruct
    public void dataInit() {
        User admin = new User();
        admin.setPassword("admin");
        admin.setUsername("admin");
        admin.setRole(User.ROLE.admin);
        userRepository.save(admin);
        genConfig(admin);


        User guest = new User();
        guest.setPassword("guest");
        guest.setUsername("guest");
        guest.setRole(User.ROLE.user);
        userRepository.save(guest);
        genConfig(guest);

    }

    private void genConfig(User user) {
        for (int i = 0; i < 5; i++) {
            WxConfig wxConfig = new WxConfig();
            wxConfig.setUsername(user.getUsername());
            wxConfig.setText("测试" + (i + 1));
            wxConfigRepository.save(wxConfig);
        }
    }

}