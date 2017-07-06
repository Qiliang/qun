package com.weixin.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Service
public class DataInit {

    @Autowired
    UserRepository userRepository;
    @Autowired
    MassConfigRepository wxConfigRepository;

    @PostConstruct
    public void dataInit() {
        if (wxConfigRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setPassword("admin");
            admin.setUsername("admin");
            admin.setRole(User.ROLE.USER);
            userRepository.save(admin);
        }

    }


}