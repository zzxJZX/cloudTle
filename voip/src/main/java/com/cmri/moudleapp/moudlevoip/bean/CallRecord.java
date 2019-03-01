package com.cmri.moudleapp.moudlevoip.bean;


import com.alibaba.fastjson.JSONArray;

import java.io.Serializable;

/**
 * @author: fang wei
 * @data: 2017/8/21
 * @Description: <通话记录实体>
 */

public class CallRecord implements Serializable{
    private String call_number = "";//呼叫号码
    private long start_time;//开始时间
    private long last_time;//持续秒数
    private int direction;//呼叫状态
    private int callType;//1v1还是多方

    public String getCall_number() {
        return this.call_number;
    }
    public void setCall_number(String call_number) {
        this.call_number = call_number;
    }
    public long getStart_time() {
        return this.start_time;
    }
    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }
    public long getLast_time() {
        return this.last_time;
    }
    public void setLast_time(long last_time) {
        this.last_time = last_time;
    }
    public int getDirection() {
        return this.direction;
    }
    public void setDirection(int direction) {
        this.direction = direction;
    }
    public int getCallType() {
        return this.callType;
    }
    public void setCallType(int callType) {
        this.callType = callType;
    }

    @Override
    public String toString() {
        return "CallRecord{" +
                ", call_number='" + call_number + '\'' +
                ", start_time=" + start_time +
                ", last_time=" + last_time +
                ", direction=" + direction +
                ", callType=" + callType +
                '}';
    }
}
