package com.cmri.tvdemo;

/**
 * Created by Administrator on 2018/3/29.
 */

public class FightInstance  {
    private String takeOffTime;
    private String dPort;
    private String aPort;
    private String flight;

    public FightInstance(String takeOffTime, String dPort, String aPort, String flight) {
        this.takeOffTime = takeOffTime;
        this.dPort = dPort;
        this.aPort = aPort;
        this.flight = flight;
    }

    @Override
    public String toString() {
        return "{" +
                "起飞时间='" + takeOffTime + '\'' +
                ", 始发点='" + dPort + '\'' +
                ", 到达点='" + aPort + '\'' +
                ", 班次='" + flight + '\'' +
                '}';
    }
}
