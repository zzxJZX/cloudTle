package com.cmri.moudleapp.moudlevoip.bean;

import java.io.Serializable;

/**
 * Created by Anderson on 2017/8/17.
 */

public class AccountInfo implements Serializable {

    private static final long serialVersionUID = 20170406092011L;

    private String cityCode;
    private String domain;
    private boolean fromBoss;
    private String imsAccount;
    private String imsNum;
    private String password;
    private String sbc;
    private String urlForQR;
    private String voipId;


    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isFromBoss() {
        return fromBoss;
    }

    public void setFromBoss(boolean fromBoss) {
        this.fromBoss = fromBoss;
    }

    public String getImsAccount() {
        return imsAccount;
    }

    public void setImsAccount(String imsAccount) {
        this.imsAccount = imsAccount;
    }

    public String getImsNum() {
        return imsNum;
    }

    public void setImsNum(String imsNum) {
        this.imsNum = imsNum;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSbc() {
        return sbc;
    }

    public void setSbc(String sbc) {
        this.sbc = sbc;
    }

    public String getUrlForQR() {
        return urlForQR;
    }

    public void setUrlForQR(String urlForQR) {
        this.urlForQR = urlForQR;
    }

    public String getVoipId() {
        return voipId;
    }

    public void setVoipId(String voipId) {
        this.voipId = voipId;
    }


}
