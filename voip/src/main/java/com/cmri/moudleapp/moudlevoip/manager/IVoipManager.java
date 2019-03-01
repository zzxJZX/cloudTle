package com.cmri.moudleapp.moudlevoip.manager;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;

import com.alibaba.fastjson.JSONObject;
import com.cmri.moudleapp.moudlevoip.CommonConstants;
import com.cmri.moudleapp.moudlevoip.bean.AccountInfo;
import com.cmri.moudleapp.moudlevoip.bean.VideoConfigPlatform;
import com.cmri.moudleapp.moudlevoip.service.LittleCService;
import com.cmri.moudleapp.moudlevoip.utils.CommonManagerUtil;
import com.cmri.moudleapp.moudlevoip.utils.CommonResource;
import com.mobile.voip.sdk.api.CMVoIPManager;
import com.mobile.voip.sdk.api.utils.CameraUtil;
import com.mobile.voip.sdk.api.utils.MyLogger;
import com.mobile.voip.sdk.callback.VoIP;
import com.mobile.voip.sdk.constants.VoIPConstant;

import org.greenrobot.eventbus.EventBus;
import org.mediasdk.videoengine.VideoConfig;

import java.util.ArrayList;

import littlec.conference.base.data.BackGroundCallState;

/**
 * Created by zhangcong on 2017/8/16.
 */

public class IVoipManager {
    public boolean CALLVIDEO = false;
    public boolean CONFERENCE = false;

    private static IVoipManager instance;

    public static IVoipManager getInstance(){
        if(instance == null){
            instance = new IVoipManager();
        }
        return instance;
    }

    public void initVoip(Context context, boolean useDebugModule) {
        String voipServer;
        String conferenceServer;
        if(useDebugModule){
            CommonConstants.XIAOXI_VOIP_KEY = "027633pl";
            CommonConstants.XIAOXI_VOIP_KEYPWD = "dikadsf48asdf";
            voipServer = "112.54.207.63:5061";
            conferenceServer = "112.54.207.63:1080";
        } else {
            CommonConstants.XIAOXI_VOIP_KEY = "127633pl";
            CommonConstants.XIAOXI_VOIP_KEYPWD = "zhgjgg042709";
            voipServer = "223.99.141.143:5061";
            conferenceServer = "223.99.141.143:1080";
        }
        //文件夹创立
        CommonManagerUtil.fileAddToRootOfApp(context);
        VoIPConstant.DEVICE_TYPE_TV = CommonResource.getInstance().getBoolean("is_tv_mode", true);
        String videoStr = CommonResource.getInstance().getString(CommonResource.VIDEO_CONFIG, "");
        VideoConfigPlatform videoConfig = com.alibaba.fastjson.JSONObject.parseObject(videoStr, VideoConfigPlatform.class);
        if (videoConfig != null){
            CommonManagerUtil.setSDKVideoConfig(videoConfig);
        } else {
            VideoConfig.setVideoConfig(false);
        }
        CMVoIPManager.getInstance().init(context);
        if(Build.PRODUCT.equals("Armstrong Q8311") && Build.MODEL.equals("Q8311")){
            CMVoIPManager.getInstance().setVideoRotationLandscape(true);
        }
        CMVoIPManager.getInstance().setVoipServerAddress(voipServer, conferenceServer);
        MyLogger.initLogger(CommonConstants.DEBUG, "");
    }

    public void setSDKVideoConfig(VideoConfigPlatform videoConfig) {
        CommonManagerUtil.setSDKVideoConfig(videoConfig);
        CMVoIPManager.getInstance().setSDKVideoConfig();
    }

    public void loginVoip(Context context, String domain, String sbc, int iPort,
                          String userName, String imsNum, String authName, String password, int loginTpye , boolean wakeUp) {
        MyLogger.getLogger("IVoipManagerImpl").i("loginVoip");
        CommonManagerUtil.startLittlecService(context, domain, sbc, iPort, userName, imsNum, authName, password, loginTpye , wakeUp);
    }

    public void logoutVoip(Context context) {
        CommonManagerUtil.stopLittlecService(context);
    }

    public void destroyVoip() {
        MyLogger.getLogger("IVoipManagerImpl").i("destroyVoip");
        CMVoIPManager.getInstance().destroy();
    }

    public void actionStartVideoConf(Context context, final ArrayList<String> members) {
        AccountInfo accountInfo = getAccount();
        BackGroundCallState msg = new BackGroundCallState(LittleCService.CALL_MAKE_CONFERENCE);
        msg.callShowName = accountInfo.getImsNum();
        msg.callNumber = accountInfo.getImsNum();
        msg.callType = VoIP.CallType.toInt(VoIP.CallType.CALLTYPE_CONFERENCE_VIDEO);
        msg.con_member = members;
        EventBus.getDefault().post(msg);
    }

    public void actionStartVideo1V1(Context context, final String targetMobil, final String targetName) {
        startMedia1V1Video(targetMobil, targetName, VoIPConstant.CallType.CALLTYPE_1V1_VIDEO);
    }

    public void actionStartAudio1V1(Context context, final String targetMobil, final String targetName) {
        startMedia1V1Video(targetMobil, targetName, VoIPConstant.CallType.CALLTYPE_1V1_AUDIO);
    }


    public void actionStartImsCallVideo(Context context, String targetMobil, String targetName) {
        startMedia1V1Video(targetMobil, targetName, VoIPConstant.CallType.CALLTYPE_1V1_VIDEO_IMS);
    }

    public void actionStartImsCallAudio(Context context, String targetMobil, String targetName) {
        startMedia1V1Audio(targetMobil, targetName, VoIPConstant.CallType.CALLTYPE_1V1_AUDIO_IMS);
    }

    private void startMedia1V1Video(String targetMobil, String targetName, int type){
        BackGroundCallState msg = new BackGroundCallState(LittleCService.CALL_MAKE_VIDEO);
        msg.callShowName = targetName;
        msg.callNumber = targetMobil;
        msg.callType = type;
        EventBus.getDefault().post(msg);
    }

    private void startMedia1V1Audio(String targetMobil, String targetName, int type){
        BackGroundCallState msg = new BackGroundCallState(LittleCService.CALL_MAKE_AUDIO);
        msg.callShowName = targetName;
        msg.callNumber = targetMobil;
        msg.callType = type;
        EventBus.getDefault().post(msg);
    }

    public int getCameraNum() {
        return CMVoIPManager.getInstance().getCameraNum();
    }

    public int refreshCamera() {
        return CMVoIPManager.getInstance().refreshCameraInfo(0);
    }

    public Camera open(int id) {return CameraUtil.open(id);}

    public String getSDKVersion() {
        return CMVoIPManager.getInstance().getVersion();
    }

    public AccountInfo getAccount() {
        String accountStr = CommonResource.getInstance().getString(CommonResource.ACCOUNT_INFO, "");
        AccountInfo accountInfo = JSONObject.parseObject(accountStr, AccountInfo.class);
        if(accountInfo == null){
            accountInfo = new AccountInfo();
        }
        return accountInfo;
    }

}
