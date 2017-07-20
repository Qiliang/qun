package com.weixin.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SystemConfig {

    @Id
    private int id;

    private int textInterval;

    private int imageInterval;

    private int textImageInterval;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTextInterval() {
        return textInterval;
    }

    public void setTextInterval(int textInterval) {
        this.textInterval = textInterval;
    }

    public int getImageInterval() {
        return imageInterval;
    }

    public void setImageInterval(int imageInterval) {
        this.imageInterval = imageInterval;
    }

    public int getTextImageInterval() {
        return textImageInterval;
    }

    public void setTextImageInterval(int textImageInterval) {
        this.textImageInterval = textImageInterval;
    }
}
