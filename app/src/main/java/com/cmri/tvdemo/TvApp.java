package com.cmri.tvdemo;

import android.app.Application;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cmri.moudleapp.moudlevoip.ICmccManager;
import com.cmri.moudleapp.moudlevoip.utils.CommonResource;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * @author: MOONY
 * @data: 2017/11/18
 * @Description: <>
 */

public class TvApp extends Application {


    private VoiceWakeuper mIvw;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startwakelistener(String s){
        start_Wake();
    }
    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);


        new Thread(runnable).start();
       /* Intent serviceintent=new Intent(this,WakeService.class);
        startService(serviceintent);*/
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.i("TvApp", "onCreate");

            CommonResource.getInstance().setContext(getApplicationContext());
            ICmccManager.getInstance().initCmcc(getApplicationContext(), true);
            SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=56c6ab94");
            mIvw = VoiceWakeuper.createWakeuper(getApplicationContext(), null);
            start_Wake();
            actionloginin();

        }
    };

    public void actionloginin() {
        ICmccManager.getInstance().loginCmccIms(this,
                "",
                "ims.sd.chinamobile.com",
                "223.99.141.165",
                6000 ,
                "+8653158021024" ,
                "+8653158021024",
                "+8653158021024@ims.sd.chinamobile.com",
                "a7d6wU9Jr5LK7SI",false);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("TvApp", "onTerminate");
        ICmccManager.getInstance().destroyCmcc();
    }
    public void start_Wake () {
        Log.i("smartinteran","start_Wake");
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            String curThresh = "0:1450";
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "1");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            System.out.println("启动1");
            // 启动唤醒
            mIvw.startListening(mWakeuperListener);
            System.out.println("启动2");
        } else {
            Log.i("smartinteran","未初始化成功");
        }
    }
    private  String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/"+getString(R.string.app_id)+".jet");
        Log.d( TAG, "resPath: "+resPath );
        return resPath;
    }
    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Log.d(TAG, "onResult222222");

            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 "+text);
                buffer.append("\n");
                buffer.append("【操作类型】"+ object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】"+ object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                System.out.println(TAG+":"+ buffer.toString());
                if (object.optString("sst").equals("wakeup")){
                    if (SmartInteractive.WakeUp) {
                        Intent intent2 = new Intent(ConnectivityManager.EXTRA_REASON);
                        intent2.putExtra("wake_up", true);
                        sendBroadcast(intent2);
                    } else {

                        ICmccManager.getInstance().loginCmccIms(getApplicationContext(),
                                "",
                                "ims.sd.chinamobile.com",
                                "223.99.141.165",
                                6000 ,
                                "+8653158021024" ,
                                "+8653158021024",
                                "+8653158021024@ims.sd.chinamobile.com",
                                "a7d6wU9Jr5LK7SI",true);

                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), SmartInteractive.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onError(SpeechError error) {
//            Toast.makeText(getApplicationContext(),"唤醒失败！"+error.getPlainDescription(true),Toast.LENGTH_SHORT).show();
            System.out.println("error:"+error.getPlainDescription(true));
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch( eventType ){
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray( SpeechEvent.KEY_EVENT_RECORD_DATA );
                    Log.i( TAG, "ivw audio length: "+audio.length );
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {
//            System.out.println("启动:"+volume);
        }
    };

    public void stop_wake(){
        Log.i("smartinteran","stop_wake");
        if (mIvw != null){
            mIvw.stopListening();
        }
    }
}
