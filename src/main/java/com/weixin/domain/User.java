package com.weixin.domain;

import javax.persistence.*;
import java.util.List;


@Entity
public class User {

    public enum ROLE{
        admin, user;
    }

    @Enumerated(EnumType.STRING)
    private ROLE role;

    @Id
    private String username;

    private String password;

    @Transient
    private List<WxConfig> wxConfigs;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public ROLE getRole() {
        return role;
    }
    public void setRole(ROLE role) {
        this.role = role;
    }

    public List<WxConfig> getWxConfigs() {
        return wxConfigs;
    }

    public void setWxConfigs(List<WxConfig> wxConfigs) {
        this.wxConfigs = wxConfigs;
    }
}