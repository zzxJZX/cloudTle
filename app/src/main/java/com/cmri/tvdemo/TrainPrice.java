package com.cmri.tvdemo;

/**
 * Created by Administrator on 2018/5/10.
 */

public class TrainPrice {
    private String price_type;
    private String price;

    public TrainPrice(String price_type, String price) {
        this.price_type = price_type;
        this.price = price;
    }

    public String getPrice_type() {
        return price_type;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "{" +
                "'" + price_type + '\'' +
                ", '" + price + "元"+'\'' +
                '}';
    }
}
