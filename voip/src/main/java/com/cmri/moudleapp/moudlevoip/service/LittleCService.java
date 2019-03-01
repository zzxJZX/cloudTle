package com.cmri.moudleapp.moudlevoip.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.cmri.moudleapp.moudlevoip.CommonConstants;
import com.cmri.moudleapp.moudlevoip.bean.AccountInfo;
import com.cmri.moudleapp.moudlevoip.manager.IVoipManager;
import com.cmri.moudleapp.moudlevoip.utils.CommonResource;
import com.cmri.moudleapp.moudlevoip.utils.MediaPlayerManager;
import com.mobile.voip.sdk.api.CMVoIPManager;
import com.mobile.voip.sdk.api.utils.MyLogger;
import com.mobile.voip.sdk.api.utils.VoIPServerConnectListener;
import com.mobile.voip.sdk.callback.VoIP;
import com.mobile.voip.sdk.callback.VoIPCallStateCallBack;
import com.mobile.voip.sdk.callback.VoIPConferenceStateCallBack;
import com.mobile.voip.sdk.callback.VoIPInComingCallListener;
import com.mobile.voip.sdk.callback.VoIPLoginCallBack;
import com.mobile.voip.sdk.constants.VoIPConstant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import littlec.conference.base.data.BackGroundCallState;
import littlec.conference.base.data.CallState;
import littlec.conference.base.data.ConnectionState;
import littlec.conference.base.data.EventState;
import littlec.conference.base.data.VoipEventState;

import littlec.conference.talk.util.CallUtil;

/**
 * Created by Kaven on 2017/2/13.
 */

public class LittleCService extends Service {
    private static final String TAG = "LittleCService";
    public static final int NOTIFICATION_FLAG = 1;
    public static final int CALL_MAKE_AUDIO = 8000;
    public static final int CALL_MAKE_VIDEO = CALL_MAKE_AUDIO + 1;
    public static final int CALL_MAKE_CONFERENCE = CALL_MAKE_VIDEO + 1;
    public static final int CALL_JOIN_CONFERENCE = CALL_MAKE_CONFERENCE + 1;
    public static final int CALL_SPEAK_ON = CALL_JOIN_CONFERENCE + 1;
    public static final int CALL_LOGOUT = CALL_SPEAK_ON + 1;
    public static final String REFRESH_CAMERA_DONE = "refresh_camera_done";

    //云固话
    public static final String IMS_DOMAIN = "domain";
    public static final String IMS_SBC = "sbc";
    public static final String IMS_PORT = "port";
    public static final String IMS_USERNAME = "user_name";
    public static final String IMS_NUM = "ims_num";
    public static final String IMS_AUTH_NAME = "ims_auth_name";
    public static final String IMS_PWD = "ims_pwd";

    public static final String LOGIN_TYPE = "demo_login_type";

    public static final String WAKE_UP = "wake_up";

    private String imsDomain;
    private String imsSbc;
    private int imsPort;
    private String imsUserName;
    private String imsNum;
    private String imsAuthName;
    private String imsPwd;

    private int loginType;

    private boolean wakeUp = false;

    private boolean isConnecting = false;
    private boolean loginSuccess = false;
    private boolean loginImsSuccess = false;
    private TelephonyManager telephonyManager;
    private int callState; //当前通话状态
    private String callNumber = null;//通话对象
    private MediaPlayerManager mediaPlayerManager;
    private int hangupOtherSession = -100;  //保存来电时，自动挂断的session,用来处理release抛上来的消息
    private boolean isFirstIn = true;
    private Map<Integer, Integer> mSessionMap = new HashMap<>();
    private Timer timer;
    private String userName;
    private String password;
    private String usbState = REFRESH_CAMERA_DONE;
    private int cameraNum;

    //add by cll
    private List<String> sbcList;
    private Lock sbcLock = new ReentrantLock();
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Toast.makeText(LittleCService.this,"IMS登陆成功",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(LittleCService.this,"IMS登陆失败",Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };
    private void refreshCameraInfo(final int count){
        MyLogger.getLogger(TAG).i("USB attached refreshCameraInfo count:"+count);
        if (count <= 0){
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new VoipEventState.CameraRefreshState(VoipEventState.CAMERA_REFRESH_START));
                cameraNum = CMVoIPManager.getInstance().refreshCameraInfo(0);

                MyLogger.getLogger(TAG).i("USB attached cameraNum :"+cameraNum);
                usbState = REFRESH_CAMERA_DONE;
                EventBus.getDefault().post(new VoipEventState.CameraRefreshState(VoipEventState.CAMERA_REFRESH_END));
                if(cameraNum>0){
                    EventBus.getDefault().post(new VoipEventState.CameraRefreshState(VoipEventState.CAMERA_EXIST));
                } else {
                    refreshCameraInfo(count - 1);
                }
            }
        }, 3000);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    MyLogger.getLogger(TAG).i("USB attached");
                    if(callState == VoIPConstant.CALL_STATE_RELEASED){
                        refreshCameraInfo(3);
                    } else {
                        usbState = UsbManager.ACTION_USB_DEVICE_ATTACHED;
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    MyLogger.getLogger(TAG).i("USB detached");
                    if(callState == VoIPConstant.CALL_STATE_RELEASED){
                        EventBus.getDefault().post(new VoipEventState.CameraRefreshState(VoipEventState.CAMERA_REFRESH_START));
                        cameraNum = CMVoIPManager.getInstance().refreshCameraInfo(0);

                        MyLogger.getLogger(TAG).i("USB detached cameraNum :"+cameraNum);
                        usbState = REFRESH_CAMERA_DONE;
                        EventBus.getDefault().post(new VoipEventState.CameraRefreshState(VoipEventState.CAMERA_REFRESH_END));
                        if(cameraNum<1){
                            EventBus.getDefault().post(new VoipEventState.CameraRefreshState(VoipEventState.CAMERA_NO_EXIST));
                        }
                    } else {
                        usbState = UsbManager.ACTION_USB_DEVICE_DETACHED;
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver mNetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                MyLogger.getLogger(TAG).d("网络连接断开广播");
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isAvailable()) {
                    if(!isFirstIn){ // 第一次进入不提示
                        EventBus.getDefault().post("aaa");
                        Toast.makeText(getApplicationContext(),"网络已连接", Toast.LENGTH_LONG).show();
                        MyLogger.getLogger(TAG).d("网络已连接");
                        if (TextUtils.isEmpty(userName)) {
                            getLoginParamsTask();
                        }
                    }
                } else {
                    isFirstIn = false;
                    Toast.makeText(getApplicationContext(),"网络已断开", Toast.LENGTH_LONG).show();
                    MyLogger.getLogger(TAG).d("网络已断开");
                }
            }
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                //获取当前的wifi状态int类型数据
                int mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (mWifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        //已打开
                        MyLogger.getLogger(TAG).d("wifi已打开");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        //打开中
                        MyLogger.getLogger(TAG).d("wifi打开中");
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        //已关闭
                        MyLogger.getLogger(TAG).d("wifi已关闭");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        //关闭中
                        MyLogger.getLogger(TAG).d("wifi关闭中");
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        //未知
                        MyLogger.getLogger(TAG).d("wifi未知");
                        break;
                }
            }
        }
    };



    /**
     * 增加home键监听，退到主页kill-app
     */
    private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra("reason");
                if (reason != null) {
                    if (("homekey").equals(reason)) {
                        MyLogger.getLogger(TAG).i("Home key click need kill");
                        stopSelf();
                        CMVoIPManager.getInstance().doLogout();
                        System.exit(0);
                    }
                }
            }
        }
    };


    private void registerNetReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mNetReceiver, filter);
    }

    private void registerHomeReceiver() {
        IntentFilter mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        registerReceiver(mHomeReceiver, mFilter);
    }

    private void registerUsbBroadcast() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, usbFilter);
    }

    /**
     * 视频流参数
     */
//    private VoIPCodecObserver voIPCodecObserver = new VoIPCodecObserver() {
//        @Override
//        public void incomingRate(int session, int channel, int framerate, int bitrate) {
//
//        }
//
//        @Override
//        public void DecoderTiming(int decode_ms, int max_decode_ms, int current_delay_ms, int target_delay_ms, int jitter_buffer_ms, int min_playout_delay_ms, int render_delay_ms) {
//
//        }
//
//        @Override
//        public void incomingCodecChanged(int session, int channel, int width, int height) {
//            EventBus.getDefault().post(new EventState(EventState.INCOMING_CODEC_CHANGED, channel, width, height));
//        }
//
//        @Override
//        public void requestNewKeyFrame(int session, int Channel) {
//
//        }
//
//        @Override
//        public void outgoingRate(int session, int channel, int framerate, int bitrate) {
//
//        }
//
//        @Override
//        public void suspendChange(int session, int channel, boolean is_suspended) {
//
//        }
//
//        @Override
//        public void outgoingCodecChanged(int session, int channel, int width, int height) {
//            EventBus.getDefault().post(new EventState(EventState.OUTGOING_CODEC_CHANGED, channel, width, height));
//        }
//
//        @Override
//        public void videoEncodeModeChanged(int session, boolean isOpengl) {
//            EventBus.getDefault().post(new EventState(EventState.VOIP_ENCODE_MODE_CHANGED, null));
//        }
//    };
    private VoIPServerConnectListener voIPServerConnectListener = new VoIPServerConnectListener() {
        @Override
        public void onLoginSucceed(int sipFlag) {
            if (sipFlag == 0) {
            loginSuccess = true;
//            VoIPConfig.setToken(CMVoIPManager.getInstance().getUserPasswordEncrypted());
            MyLogger.getLogger(TAG).e("voip loginsuccess: "+loginSuccess + ", sipFlag:"+sipFlag); //",token:"+ CMVoIPManager.getInstance().getUserPasswordEncrypted());
            EventBus.getDefault().post(new ConnectionState(ConnectionState.LOGIN_SUCCEED));

            }

            if(sipFlag==1){
                loginImsSuccess = true;
                MyLogger.getLogger(TAG).e("IMS loginsuccess");
                Message mes = Message.obtain();
                mes.what=0;
                handler.sendMessage(mes);
              /*  Intent intent=new Intent();
                intent.setClassName("com.cmri.tvdemo","com.cmri.tvdemo.ImsCallAcitivity");

                startActivity(intent);*/
              if (!wakeUp){
                  Intent intent = new Intent();
                  intent.setAction("panhouye");
                  intent.putExtra("action","login");
                  sendBroadcast(intent);
              } else {

              }
            }

        }

        @Override
        public void onLoginFailed(int failedReason , int sipFlag) {
            if (sipFlag == 0 ) {
                loginSuccess = false;
                MyLogger.getLogger(TAG).e("loginstate,loginFailed:" + loginSuccess + ", sipFlag:" + sipFlag);
                MyLogger.getLogger(TAG).d("server login failed reason:" + VoIP.ConnectionFailReason.fromInt(failedReason));
                EventBus.getDefault().post(new ConnectionState(ConnectionState.LOGIN_FAILED));
            }
            if (sipFlag == 1){
//                doLoginIMS();
                loginImsSuccess = false;
                Message mes = Message.obtain();
                mes.what=1;
                handler.sendMessage(mes);
                MyLogger.getLogger(TAG).e("loginstate,loginFailed: IMS + ");

            }
        }

        @Override
        public void onImsLogging(int result, int sipFlag) {
            // TODO: 2017/11/10   IMS正在登录中，处理逻辑 result: 100  sipFlag: 1
        }

        @Override
        public void onDisConnected(int failedReason) {
            loginSuccess = false;
            MyLogger.getLogger(TAG).e("loginstate,disconnected:"+loginSuccess+", failed reason"+failedReason);
            MyLogger.getLogger(TAG).d("server connection failed reason:"+ VoIP.ConnectionFailReason.fromInt(failedReason));
            EventBus.getDefault().post(new ConnectionState(ConnectionState.CONNECT_DISCONNECTED));
        }
        @Override
        public void onConnectSucceed() {
            loginSuccess = false;
            MyLogger.getLogger(TAG).e("loginstate,connectsuccess:"+loginSuccess);
            EventBus.getDefault().post(new ConnectionState(ConnectionState.CONNECT_SUCCEED));
        }
    };

    private VoIPInComingCallListener inComingCallListener = new VoIPInComingCallListener() {
        @Override
        public void onInComingCall(String phoneNumber, int callType, int session) {
            MyLogger.getLogger(TAG).d("***************onInComingCall**********************");
            MyLogger.getLogger(TAG).d("phoneNumber:          " + phoneNumber);
            MyLogger.getLogger(TAG).d("callType:             " + callType);
            MyLogger.getLogger(TAG).d("session:              " + session);
            MyLogger.getLogger(TAG).d("***************onInComingCall**********************");
            if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                if (callState != VoIPConstant.CALL_STATE_RELEASED) {
                    CMVoIPManager.getInstance().hangUpCall(session);
                    hangupOtherSession = session;
                    MyLogger.getLogger(TAG).d("hangUpCall session: " + session);

                    return;
                }
                mSessionMap.put(session, session);
                callState = VoIPConstant.CALL_STATE_INCOMING;
                boolean isAutoAnswer = CommonResource.getInstance().getBoolean(CommonResource.IS_AUTO_ANSWER, false);
                if (callType == VoIPConstant.CallType.CALLTYPE_CONFERENCE_AUDIO
                        || callType == VoIPConstant.CallType.CALLTYPE_CONFERENCE_VIDEO) {
                    //jump to conference page
                    MyLogger.getLogger(TAG).d("jump to conference page");
                    CallUtil.jump2ConferencePage(LittleCService.this, session, callType, true, VoIPConstant.CALL_STATE_INCOMING, phoneNumber,
                            "", false, isAutoAnswer,null);
//                            ContactMgr.getInstance().getNameByNum(phoneNumber), false, null, isAutoAnswer);
                } else {
                    //jump to 1v1 call page
                    MyLogger.getLogger(TAG).d("jump to 1v1 call page");
                    CallUtil.jump2CallPage(LittleCService.this, session,
                            callType, true, VoIPConstant.CALL_STATE_INCOMING, phoneNumber,
                            "", false, false, true, 0, isAutoAnswer);
//                            ContactMgr.getInstance().getNameByNum(phoneNumber), false, false, true, 0, isAutoAnswer);
                }
            } else {
                CMVoIPManager.getInstance().hangUpCall(session);
            }
        }
    };
    private VoIPCallStateCallBack callStateCallBack = new VoIPCallStateCallBack() {
        @Override
        public void onCallProceeding(int session) {
            MyLogger.getLogger(TAG).d("onCallProceeding");
            callState = VoIPConstant.CALL_STATE_PROCEEDING;
            mSessionMap.put(session,session);
            EventBus.getDefault().post(new CallState(callState));
        }

        @Override
        public void onCallAlerting(int session) {
            MyLogger.getLogger(TAG).d("onCallAlerting");
            callState = VoIPConstant.CALL_STATE_ALERTING;
            EventBus.getDefault().post(new CallState(callState));
            mSessionMap.put(session,session);
            if (mediaPlayerManager != null){
                mediaPlayerManager.startOutGoingMusic();
            }

        }

        @Override
        public void onStopCallAlerting(int session) {
            MyLogger.getLogger(TAG).d("onStopCallAlerting");
            callState = VoIPConstant.CALL_STATE_STOP_ALERTING;
            mSessionMap.put(session,session);
            EventBus.getDefault().post(new CallState(callState));
            if (mediaPlayerManager != null){
                mediaPlayerManager.stop();
            }

        }

        @Override
        public void onCallAnswered(int session, int callType) {
            MyLogger.getLogger(TAG).d("onCallAnswered ");
            callState = VoIPConstant.CALL_STATE_ANSWERED;
            EventBus.getDefault().post(new CallState(callState, callType));
            mSessionMap.put(session,session);
            if (mediaPlayerManager != null) {
                mediaPlayerManager.stop();
            }

        }

        @Override
        public void onCallForward(int session) {
            MyLogger.getLogger(TAG).d("onCallForward ");
            EventBus.getDefault().post(new CallState(VoIPConstant.VOIP_STATE_CALL_FORWARD));
        }

        @Override
        public void onLogout() {
            MyLogger.getLogger(TAG).d("onLogout");
            BackGroundCallState msg = new BackGroundCallState(LittleCService.CALL_LOGOUT);
            EventBus.getDefault().post(msg);
        }

        @Override
        public void onMakeCallFailed(int session, String status) {
            MyLogger.getLogger(TAG).d("onMakeCallFailed status： " + status );
            callState = VoIPConstant.CALL_STATE_MAKE_FAILED;
            mSessionMap.put(session,session);
            EventBus.getDefault().post(new CallState(callState));
        }

        @Override
        public void onCallReleased(int session) {
            //TODO
            MyLogger.getLogger(TAG).d("onCallReleased session: "+session);
            //当抛上来的release与来电自动挂断时的session相同时，不处理
//            if (session == hangupOtherSession) {
//                hangupOtherSession = -100;
//                return;
//            }
            if(mSessionMap.containsKey(session)){
                mSessionMap.remove(session);
                callState = VoIPConstant.CALL_STATE_RELEASED;
                EventBus.getDefault().post(new CallState(callState));
                MyLogger.getLogger(TAG).d("post CALL_STATE_RELEASED message, session: "+session);
            }

            if(!usbState.equals(REFRESH_CAMERA_DONE)){
                cameraNum = CMVoIPManager.getInstance().refreshCameraInfo(0);
                usbState = REFRESH_CAMERA_DONE;
                if(cameraNum>0){
                    EventBus.getDefault().post(new VoipEventState.CameraRefreshState(VoipEventState.CAMERA_EXIST));
                } else {
                    EventBus.getDefault().post(new VoipEventState.CameraRefreshState(VoipEventState.CAMERA_NO_EXIST));
                }
            }
        }

        @Override
        public void onReceiveCallSwitch(int session) {
            //TODO
            MyLogger.getLogger(TAG).d("onReceiveCallSwitch");
        }

        @Override
        public void onCallReBuildResult(int session, int callType) {
            //TODO
            MyLogger.getLogger(TAG).d("onCallReBuildResult");
            EventBus.getDefault().post(new CallState(VoIPConstant.CALL_STATE_SWITCH_RESULT, callType));
        }

    };

    /**
     * 会议状态回调
     */
    private VoIPConferenceStateCallBack conferenceStateCallBack = new VoIPConferenceStateCallBack() {
        @Override
        public void onConferenceClosed(String conferNum) {
            EventBus.getDefault().post(new EventState(EventState.VOIP_CONFERENCE_CLOSED, conferNum));
        }

        @Override
        public void onConferenceKicked(String conferNum, String kicked) {

        }

        @Override
        public void onConferenceMuted(String conferNum, boolean mute) {

        }

        @Override
        public void onConferenceUpdated(String conferNum) {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        registerNetReceiver();
        registerHomeReceiver();
        registerUsbBroadcast();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mediaPlayerManager = new MediaPlayerManager(this);
        CMVoIPManager.getInstance().addServerConnectListener(voIPServerConnectListener);
        CMVoIPManager.getInstance().addInComingCallListener(inComingCallListener);
        CMVoIPManager.getInstance().addCallStateListener(callStateCallBack);
        CMVoIPManager.getInstance().addConferenceStateListener(conferenceStateCallBack);
//        CMVoIPManager.getInstance().registerVideoCodecObserver(voIPCodecObserver);

        sbcList = new ArrayList<String>();
        MyLogger.getLogger(TAG).i("onCreate do Login");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLogger.getLogger(TAG).i("onDestroy");
        unregisterReceiver(mNetReceiver);
        unregisterReceiver(usbReceiver);
//        unregisterReceiver(mHomeReceiver);
        EventBus.getDefault().unregister(this);
        CMVoIPManager.getInstance().removeServerConnectListener(voIPServerConnectListener);
        CMVoIPManager.getInstance().removeCallStateListener(callStateCallBack);
        CMVoIPManager.getInstance().removeConferenceStateListener(conferenceStateCallBack);
//        CMVoIPManager.getInstance().deregisterVideoCodecObserver();
        stopTimer();
//        Intent intent = new Intent();
//        intent.setAction("panhouye");
//        intent.putExtra("action","destroy");
//        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLogger.getLogger(TAG).i("onStartCommand do Login");

        if (intent != null) {
            imsDomain = intent.getStringExtra(LittleCService.IMS_DOMAIN);
            imsSbc = intent.getStringExtra(LittleCService.IMS_SBC);
            imsPort = intent.getIntExtra(LittleCService.IMS_PORT, 0);
            imsUserName = intent.getStringExtra(LittleCService.IMS_USERNAME);
            imsNum = intent.getStringExtra(LittleCService.IMS_NUM);
            imsAuthName = intent.getStringExtra(LittleCService.IMS_AUTH_NAME);
            imsPwd = intent.getStringExtra(LittleCService.IMS_PWD);
            loginType = intent.getIntExtra(LittleCService.LOGIN_TYPE, 0);
            wakeUp = intent.getBooleanExtra(LittleCService.WAKE_UP,false);
        }
        if (loginType == 0) {
            doLogin(imsNum);
        } else {
            doLoginIMS(imsDomain, imsSbc, imsPort, imsUserName, imsNum, imsAuthName, imsPwd);
        }

        return START_STICKY;

    }

    private void getLoginParamsTask() {

//        getLoginParams.getAdapterConfigTask(getApplicationContext(), new LoginCallback() {
//
//            @Override
//            public void onSuccess(String ImsNum, String oldImsNum) {
//                MyLogger.getLogger(TAG).i("ImsNum： "+ImsNum+", oldImsNum: "+oldImsNum);
//                startGetBindStatus();
//                EventBus.getDefault().post(new ContactSyncUtil(ContactSyncUtil.MODE_SYNC_CONTACT));
//                if (TextUtils.isEmpty(oldImsNum) || ImsNum.equals(oldImsNum)) {// 第一登陆，或者退出app，后续打开app的自动登陆
//                    doLogin(ImsNum, "");
//                }else{
//                    MyLogger.getLogger(TAG).e("ImsNum changed");
//                    //退出再登陆
//                    CMVoIPManager.getInstance().doLogout();
//                    LoginManager.getInstance().getUser().setUserName(ImsNum);
//                    doLogin(ImsNum, "");
//                }
//            }
//
//            @Override
//            public void onFailure(int errorCode, final String errorString) {
//                MyLogger.getLogger(TAG).e("errorCode: "+errorCode+" ,errorString: "+errorString);
//                new Thread() {
//                    @Override
//                    public void run() {
//                        Looper.prepare();
//                        Toast.makeText(LittleCService.this, "登录失败: " + errorString, Toast.LENGTH_SHORT).show();
//                        Looper.loop();
//                    }
//                }.start();
//
//            }
//        });
    }


    private void doLogin(String userName) {
        MyLogger.getLogger(TAG).i("execute do Login");
        Map<String, String> map = new HashMap<>();
        map.put(VoIPConstant.ACCOUNT, userName);
        map.put(VoIPConstant.APPKEY, CommonConstants.XIAOXI_VOIP_KEY );
        map.put(VoIPConstant.ACCOUNTTOKEN,CommonConstants.XIAOXI_VOIP_KEYPWD);
        CMVoIPManager.getInstance().doLogin(map, new VoIPLoginCallBack() {
            @Override
            public void onLoginSuccess() {
                MyLogger.getLogger(TAG).d("VoIP login success!");

            }

            @Override
            public void onLoginFailed(int code) {
                MyLogger.getLogger(TAG).e("VoIP login failed! code: " + code);
            }
        });
    }

    private void doLoginIMS(String domain, String sbc, int port, String user_name, String ims_num, String ims_auth_name, String pwd ){
        MyLogger.getLogger(TAG).e("doLoginIMS serverlUrl:"+domain+",serverAddr :"+sbc+",userName:"+user_name+",displayName:"+ims_num+",authName:"+ims_auth_name+",password:"+pwd);
        if (!TextUtils.isEmpty(domain) && !TextUtils.isEmpty(ims_num) && !TextUtils.isEmpty(ims_auth_name) && !TextUtils.isEmpty(pwd)) {
            CMVoIPManager.getInstance().register2Ims(domain,sbc,port,user_name,ims_num,ims_auth_name,pwd);
//            if (CommonConstants.DEBUG){
                CMVoIPManager.getInstance().setCallForwardingFlag(true);
//            } else {
//                CMVoIPManager.getInstance().setCallForwardingFlag(false);
//            }

//                    CMVoIPManager.getInstance().register2Ims("ims.sd.chinamobile.com","211.137.192.165",5060,"+8653158098616","+8653158098616", "+8653158098616@ims.sd.chinamobile.com","n9c6CCY6KFc9y3w");
//                    CMVoIPManager.getInstance().setCallForwardingFlag(true);
        }


       /* AccountInfo account = IVoipManager.getInstance().getAccount();
        if(account==null){
            MyLogger.getLogger(TAG).e("IMS账号为空");
            return;
        }
        if(account.isFromBoss()==false){
            MyLogger.getLogger(TAG).e("未从boss获取到IMS账号");
            return;
        }
        sbcLock.lock();
        MyLogger.getLogger(TAG).e("sbcList.size :"+sbcList.size());
        if (sbcList.size() > 0){
            String tmp = sbcList.get(0);
            sbcList.remove(0);
            String [] params = tmp.split(":");

            int port = 5060;
            if (params.length == 2){
                port = Integer.parseInt(params[1]);
            }
            if (account != null) {
                String domain= account.getDomain();
                String imsNum = account.getImsNum();
                imsNum = "+86"+imsNum.substring(1);
                String imsAccount = account.getImsAccount();
                String userName = imsAccount;
                int index = imsAccount.indexOf("@");
                if (index != -1){
                    userName = imsAccount.substring(0,index);
                }
                String password = account.getPassword();
                MyLogger.getLogger(TAG).e("doLoginIMS serverlUrl:"+domain+",serverAddr :"+params[0]+",userName:"+userName+",displayName:"+imsNum+",authName:"+imsAccount+",password:"+password);
                if (!TextUtils.isEmpty(domain) && !TextUtils.isEmpty(imsNum) && !TextUtils.isEmpty(imsAccount) && !TextUtils.isEmpty(password)&&params.length > 0) {
                    MyLogger.getLogger(TAG).e("doLoginIMS serverlUrl:"+domain+",serverAddr :"+params[0]+",iPort:"+port+",userName:"+userName+",displayName:"+imsNum+",authName:"+imsAccount+",password:"+password);
                    CMVoIPManager.getInstance().register2Ims(domain,params[0],port,userName,imsNum,imsAccount,password);
                    if (CommonConstants.DEBUG){
                        CMVoIPManager.getInstance().setCallForwardingFlag(true);
                    } else {
                        CMVoIPManager.getInstance().setCallForwardingFlag(false);
                    }

//                    CMVoIPManager.getInstance().register2Ims("ims.sd.chinamobile.com","211.137.192.165",5060,"+8653158098616","+8653158098616", "+8653158098616@ims.sd.chinamobile.com","n9c6CCY6KFc9y3w");
//                    CMVoIPManager.getInstance().setCallForwardingFlag(true);
                }
            }
        }
        sbcLock.unlock();*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(CallState state) {
        switch (state.callStateCode) {
            case VoIPConstant.CALL_STATE_RELEASED:
                MyLogger.getLogger(TAG).e("onMessage CALL_STATE_RELEASED");
                callState = VoIPConstant.CALL_STATE_RELEASED;
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackGroundCallState(BackGroundCallState msg) {
        MyLogger.getLogger(TAG).d("onBackGroundCallState");
        switch (msg.code) {
            case VoIPConstant.CALL_STATE_PROCEEDING:
                break;
            case VoIPConstant.CALL_STATE_ALERTING:
                break;
            case VoIPConstant.CALL_STATE_ANSWERED:
                break;
            case VoIPConstant.CALL_STATE_MAKE_FAILED:
                break;
            case CALL_MAKE_AUDIO:
                makeVideoCall(msg.callNumber, msg.callShowName, msg.callType);
                break;
            case CALL_MAKE_VIDEO:
                makeVideoCall(msg.callNumber, msg.callShowName, msg.callType);
                break;
            case CALL_MAKE_CONFERENCE:
                makeConferenceCall(msg.callType, msg.callNumber, msg.callShowName,msg.con_member);
                //TODO
                break;
            case CALL_JOIN_CONFERENCE:
                //TODO
                break;
            case CALL_LOGOUT:
                //                VoIPUtil.setAudioMode(LittlecApp.mContext, AudioManager.MODE_NORMAL);
                //                VoIPUtil.setSpeakerOn(LittlecApp.mContext, false);
                //                stopSelf();//直接停止服务
                Toast.makeText(getApplicationContext(),"账号被登出，正在重连中...", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    /**
     * 创建会议
     *
     * @param
     * @param conferenceNumber
     * @param conferenceName
     */
    private void makeConferenceCall(int callType, String conferenceNumber, String conferenceName, ArrayList<String> con_member) {

        if(!loginSuccess && !checkIsImsType(callType)){
            Toast.makeText(getApplicationContext(),"网络连接异常，请检查网络设置", Toast.LENGTH_LONG).show();
            return;
        }

        if (!loginImsSuccess && checkIsImsType(callType)) {
            Toast.makeText(getApplicationContext(),"网络连接异常，请检查网络设置", Toast.LENGTH_LONG).show();
            return;
        }

      /*  if (callState != VoIPConstant.CALL_STATE_RELEASED) {
            MyLogger.getLogger(TAG).e("makeConferenceCall callstate :"+callState);
            Toast.makeText(LittleCService.this, "当前正在通话中，请结束后再试", Toast.LENGTH_SHORT).show();
            return;
        }*/
        callState = VoIPConstant.CALL_STATE_PREPARING;
        CallUtil.jump2ConferencePage(LittleCService.this, 0, callType, false, callState, conferenceNumber, conferenceName, true,false,con_member);
    }


    private void makeVideoCall(String username, String showName, int callType) {

        if(!loginSuccess && !checkIsImsType(callType)){
            Toast.makeText(getApplicationContext(),"网络连接异常，请检查网络设置", Toast.LENGTH_LONG).show();
            return;
        }

        if (!loginImsSuccess && checkIsImsType(callType)) {
            Toast.makeText(getApplicationContext(),"网络连接异常，请检查网络设置", Toast.LENGTH_LONG).show();
            return;
        }
        boolean onGoing = false;
//        if (callState != VoIPConstant.CALL_STATE_RELEASED) {
//            MyLogger.getLogger(TAG).e("makeVideoCall callstate :"+callState);
//            onGoing = true;
//            return;
//        }
        callState = VoIPConstant.CALL_STATE_PREPARING;
        CallUtil.jump2CallPage(LittleCService.this, 0, callType, false, 0, username, showName,
                onGoing, false,
                callType == VoIPConstant.CallType.CALLTYPE_1V1_VIDEO || callType == VoIPConstant.CallType.CALLTYPE_1V1_VIDEO_IMS,
                0, false);
    }

    /**
     * 拉取绑定状态
     */
//    private void startGetBindStatus() {
//        stopTimer();
//        timer = new Timer();
//
//        TimerTask getBindStatusTask = new TimerTask() {
//            @Override
//            public void run() {
//                    BindStatusSyncUtil.startSyncBindStatus(getApplicationContext(), new HejiaqinCallback() {
//                        @Override
//                        public void onSuccess(String result) {
//                            String lastFamilyName = LoginManager.getInstance().getUser().getFamilyName();
//                            if (!result.equals(lastFamilyName)) {
//                                EventBus.getDefault().post(new ContactSyncUtil(ContactSyncUtil.MODE_SYNC_CONTACT));
//                                LoginManager.getInstance().getUser().setFamilyName(result);
//                                EventBus.getDefault().post(new TVTokenRefreshed());
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(int errorCode, String errorString) {
//
//                        }
//                    });
//            }
//        };
//        timer.schedule(getBindStatusTask, 2 * 1000, 10 * 1000);
//    }

    private void stopTimer(){
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
    }

    private boolean checkIsImsType(int type) {
        return type == VoIPConstant.CallType.CALLTYPE_1V1_AUDIO_IMS || type == VoIPConstant.CallType.CALLTYPE_1V1_VIDEO_IMS;
    }
}
