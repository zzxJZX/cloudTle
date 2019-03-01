package com.cmri.tvdemo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/5/10.
 */

public class TrainInstance1 {
    private String train_no;
    private String start_station;

    public String getTrain_no() {
        return train_no;
    }

    public void setTrain_no(String train_no) {
        this.train_no = train_no;
    }

    public String getStart_station() {
        return start_station;
    }

    public void setStart_station(String start_station) {
        this.start_station = start_station;
    }

    public String getEnd_station() {
        return end_station;
    }

    public void setEnd_station(String end_station) {
        this.end_station = end_station;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getRun_time() {
        return run_time;
    }

    public void setRun_time(String run_time) {
        this.run_time = run_time;
    }

    public ArrayList<TrainPrice> getTrainPrices() {
        return trainPrices;
    }

    public void setTrainPrices(ArrayList<TrainPrice> trainPrices) {
        this.trainPrices = trainPrices;
    }

    private String end_station;
    private String start_time;
    private String end_time;

    public TrainInstance1(String train_no, String start_station, String end_station, String start_time, String end_time, String run_time, ArrayList<TrainPrice> trainPrices) {
        this.train_no = train_no;
        this.start_station = start_station;
        this.end_station = end_station;
        this.start_time = start_time;
        this.end_time = end_time;
        this.run_time = run_time;
        this.trainPrices = trainPrices;
    }

    @Override
    public String toString() {
        return "{" +
                "车次'" + train_no + '\'' +
                ", 出发站'" + start_station + '\'' +
                ", 到达站'" + end_station + '\'' +
                ", 出发时间'" + start_time + '\'' +
                ", 到达时间'" + end_time + '\'' +
                ", 运行时间'" + run_time + '\'' +
                ", 票价" + trainPrices +
                '}'+'\n';
    }

    private String run_time;
    private ArrayList<TrainPrice> trainPrices;



}
