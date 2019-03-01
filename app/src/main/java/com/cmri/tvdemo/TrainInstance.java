package com.cmri.tvdemo;

/**
 * Created by Administrator on 2018/3/29.
 */

public class TrainInstance {
    private String startTime;
    private String originStation;
    private String terminalStation;
    private String trainNo;
    public TrainInstance(String startTime, String originStation, String terminalStation, String trainNo) {
        this.startTime = startTime;
        this.originStation = originStation;
        this.terminalStation = terminalStation;
        this.trainNo = trainNo;
    }

    @Override
    public String toString() {
        return "{" +
                "开始时间='" + startTime + '\'' +
                ", 始发站='" + originStation + '\'' +
                ", 到达站='" + terminalStation + '\'' +
                ", 车次='" + trainNo + '\'' +
                '}';
    }
}
