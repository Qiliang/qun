package com.weixin.domain;

import javax.persistence.*;

@Entity
public class MassConfig {

    @Id
    @GeneratedValue
    private Integer id;

    private String title;

    private String username;

    @Transient
    private String operator;

    @Lob
    private String image;
    @Lob
    private String text = "".intern();

    private String type;

    private boolean qun;

    public boolean isQun() {
        return qun;
    }

    public void setQun(boolean qun) {
        this.qun = qun;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
