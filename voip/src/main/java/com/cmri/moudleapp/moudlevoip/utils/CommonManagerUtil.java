package com.cmri.moudleapp.moudlevoip.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.cmri.moudleapp.moudlevoip.bean.VideoConfigPlatform;
import com.cmri.moudleapp.moudlevoip.service.LittleCService;
import com.mobile.voip.sdk.api.CMVoIPManager;
import com.mobile.voip.sdk.api.utils.MyLogger;
import com.mobile.voip.sdk.constants.VoIPConstant;

import org.mediasdk.videoengine.VideoConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

/**
 * Created by caizhibiao on 2016/4/28.
 */
public class CommonManagerUtil {
    private static final MyLogger logger = MyLogger.getLogger("CommonManagerUtil");
    private static long lastClickTime = 0;

    public static void stopLittlecService(Context mContext){
        logger.e("stopService");
        Intent intent = new Intent(mContext, LittleCService.class);
        mContext.stopService(intent);
        CMVoIPManager.getInstance().doLogout();
        CMVoIPManager.getInstance().doLogoutIms();
    }
    /**
     * @param mContext
     */
    public static void startLittlecService(Context mContext, String domain, String sbc, int iPort,
                                           String userName, String imsNum, String authName,
                                           String password, int loginTpye , boolean wakeUp) {
        stopLittlecService(mContext);
        if(!isServiceWork(mContext, LittleCService.class.getName())){
            Intent serviceIntent = new Intent(mContext, LittleCService.class);
            serviceIntent.putExtra(LittleCService.IMS_DOMAIN, domain);
            serviceIntent.putExtra(LittleCService.IMS_SBC, sbc);
            serviceIntent.putExtra(LittleCService.IMS_PORT, iPort);
            serviceIntent.putExtra(LittleCService.IMS_USERNAME, userName);
            serviceIntent.putExtra(LittleCService.IMS_NUM, imsNum);
            serviceIntent.putExtra(LittleCService.IMS_AUTH_NAME, authName);
            serviceIntent.putExtra(LittleCService.IMS_PWD, password);
            serviceIntent.putExtra(LittleCService.LOGIN_TYPE, loginTpye);
            serviceIntent.putExtra(LittleCService.WAKE_UP,wakeUp);
            mContext.startService(serviceIntent);
            logger.e("start LittlecService");
        } else {
            logger.e("LittlecService is work");
        }
    }

    private static boolean isServiceWork(Context mContext, String serviceName) {
        logger.e("isServiceWork serviceName = " + serviceName);

        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            String packageName = myList.get(i).service.getPackageName();
            if (mName.equals(serviceName) && packageName.equals(mContext.getPackageName())) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    public static boolean canClick(int delayTime){
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if(currentTime < lastClickTime){
            lastClickTime = 0;
        }

        if (currentTime - lastClickTime > delayTime) {
            lastClickTime = currentTime;
            return true;
        } else {
            return false;
        }
    }

    public static boolean fileIsExists(String filePath){
        File f=new File(filePath);
        if(f.exists()){
            return true;
        }
        return false;
    }

    public static void setSDKVideoConfig(VideoConfigPlatform videoConfigPlatform){
        VideoConfig.VideoEncoderType = videoConfigPlatform.getVideoEncoderType();
        VideoConfig.VideoDecoderType = videoConfigPlatform.getVideoDecoderType();
        VideoConfig.VideoResolutionType1V1 = videoConfigPlatform.getVideoResolutionType()/10;
        VideoConfig.VideoResolutionTypeConference = videoConfigPlatform.getVideoResolutionType()%10;
        VideoConfig.HW264_ENCODE_SURFACE = videoConfigPlatform.isH264EncodeSurface();
        VideoConfig.YUVFormat = videoConfigPlatform.getYUVFormat();
        VideoConfig.UVFormat = videoConfigPlatform.getUVFormat();
        VideoConfig.LocalPreviewMirror = videoConfigPlatform.isLocalPreviewMirror();
        VideoConfig.isVideoComunicationSupport = videoConfigPlatform.isVideoSupport();
        VideoConfig.setVideoConfig(true);
        logger.e("setSDKVideoConfig"+videoConfigPlatform.toString());
    }

    //Add face detect resource and special effects resources to the root of app
    public static int fileAddToRootOfApp(Context mContext) {
        Context context = mContext;
        try {
            if (fileIsExists(context.getFilesDir() + "/glass.png") && fileIsExists(context.getFilesDir() + "/rabbit.png") && fileIsExists(context.getFilesDir() + "/mask.png")) {
                return -1;
            }
            InputStream glassInput = context.getResources().getAssets().open("glass.png");
            InputStream rabbitInput = context.getResources().getAssets().open("rabbit.png");
            InputStream maskInput = context.getResources().getAssets().open("mask.png");
            FileOutputStream glassOutput = new FileOutputStream(context.getFilesDir() + "/glass.png");
            FileOutputStream rabbitOutput = new FileOutputStream(context.getFilesDir() + "/rabbit.png");
            FileOutputStream maskOutput = new FileOutputStream(context.getFilesDir() + "/mask.png");
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = glassInput.read(buffer)) > 0) {
                glassOutput.write(buffer, 0, count);
            }
            count = 0;
            while ((count = rabbitInput.read(buffer)) > 0) {
                rabbitOutput.write(buffer, 0, count);
            }
            count = 0;
            while ((count = maskInput.read(buffer)) > 0) {
                maskOutput.write(buffer, 0, count);
            }
            glassOutput.flush();
            glassOutput.close();
            glassInput.close();
            rabbitOutput.flush();
            rabbitOutput.close();
            rabbitInput.close();
            maskOutput.flush();
            maskOutput.close();
            maskInput.close();
            buffer = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
