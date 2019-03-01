package com.cmri.tvdemo;

/**
 * Created by Administrator on 2018/5/24.
 */

public class Person {
    private String name;

    public Person(String name, String idcard) {
        this.name = name;
        this.idcard = idcard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    private String idcard;

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", idcard='" + idcard + '\'' +
                '}';
    }
}
