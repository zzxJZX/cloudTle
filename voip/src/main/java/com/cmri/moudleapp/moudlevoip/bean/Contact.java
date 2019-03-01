package com.cmri.moudleapp.moudlevoip.bean;


import java.io.Serializable;

public class Contact implements Serializable{
    private String name;
    private String phoneNum;
    private String avatar;//头像

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhoneNum() {
        return this.phoneNum;
    }
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
    public String getAvatar() {
        return this.avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "Contact{" +
                ", name='" + name + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
