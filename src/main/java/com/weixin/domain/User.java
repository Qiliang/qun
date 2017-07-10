package com.weixin.domain;

import javax.persistence.*;


@Entity
public class User {

    public enum ROLE{
        ADMIN, USER, EMPLOYEE;
    }

    @Enumerated(EnumType.STRING)
    private ROLE role;

    @Id
    @GeneratedValue
    private Integer id;

    private String username;

    private String displayName;

    private String tel;

    private String password;

    private String employer;

    private String parent;

    private String copyright;

    private boolean active = true;

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

    public String getEmployer() {
        return employer;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}