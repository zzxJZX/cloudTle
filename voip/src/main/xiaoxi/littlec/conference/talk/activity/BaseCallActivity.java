package littlec.conference.talk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ToggleButton;

import com.cmri.moudleapp.moudlevoip.utils.MediaPlayerManager;
import com.mobile.voip.sdk.api.CMVoIPManager;
import com.mobile.voip.sdk.api.utils.MyLogger;
import com.mobile.voip.sdk.callback.VoIPDialCallBack;

import littlec.conference.base.activity.BaseFragmentActivity;


/**
 * @包名：com.littlec.conference.base.activity
 * @类名：BaseCallActivity
 * @描述：通话界面中共有的，比如来电界面
 * @作者：caizhibiao
 * @时间：2016-4-12上午11:36:01
 * @版本：1.0.2
 */
public abstract class BaseCallActivity extends BaseFragmentActivity  {
    private static int stateCount = 0;
    private static final MyLogger logger = MyLogger.getLogger("BaseCallActivity");
    private static final String TAG = BaseCallActivity.class.getSimpleName();

    public static class Key {
        public final static String CALL_COUNT = "call_count";//通话状态，计时
        public final static String CALL_TYPE = "call_type";//0为语音电话，1为视频电话，2为音频会议，3为视频会议
        public final static String CALL_STATE = "call_state";//通话状态，再次进入时显示不同的界面
        public final static String CALL_NUMBER = "call_number";//来电和去点的电话号码
        public final static String CALL_SHOW_NAME = "call_show_name";//来电和去电的nick
        public final static String CALL_INCOMING = "call_incoming";//是否为来电
        public final static String CALL_ONGOING = "call_ongoing";//是否是正在通话之中，该界面可放回，然后再次进入，需保持通话界面
        public final static String CALL_SPEAKER_ON = "call_speaker_on";//通话免提状态
        public final static String CALL_MUTE = "call_mute";//通话免提状态
        public final static String CALL_SESSION ="call_session"; //当前通话序号
        public final static String CALL_CREATE = "call_create"; //是否创建会议
        public final static String CALL_VOICE_MODE = "call_voice_mode"; //声音模式

        public final static String CALL_MEMBERS = "call_members";   //会议成员
//        public final static String IS_AUTO_ANSWER = "is_auto_answer";   //自动应答

    }

    public final static int STOP_NOTIFICATION = 99;

    protected final static int EVENT_TIME_COUNT = 10001;

    //视频丢包
    protected final static int EVENT_VIDEO_LOST = 20001;

    protected int mCount = 3;//计数现在的秒数
    protected int callType;
    protected int callState;
    protected String callNumber;
    protected String callShowName;
    protected boolean autoHangup = true; //是否自动挂断
    protected boolean incoming;   //来电还是去电
    protected boolean ongoing;//是否在通话中
    protected boolean callSpeakerOn;// 语音通话默认关闭，视频通话默认开启
    protected boolean callMute;// 语音通话默认关闭，视频通话默认开启
    protected int callSession;//当前通话序号
    protected boolean callCreate;//是否创建会议
    protected boolean isAutoAnswer; //是否开启自动应答

    //变音
    protected int voiceMode;

    protected boolean isHangupSelf; //拨打电话时主动挂断

    protected ToggleButton btnSpeaker;
    protected ToggleButton btnMute;
    protected MediaPlayerManager mediaPlayerManager;
    protected VoIPDialCallBack mDialCallBack = new VoIPDialCallBack() {
        @Override
        public void onHandleDialSuccess(int session) {
            MyLogger.getLogger(TAG).d("onHandleDialSuccess");
            callSession = session;
        }

        @Override
        public void onHandleDialError(int errorCode) {
            MyLogger.getLogger(TAG).e("onHandleDialError: errorCode "+errorCode);
            finish();
        }
    };

    /* (non-Javadoc)
     * @see com.littlec.conference.base.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        if (null != savedInstanceState) {//如果退出到过其它界面然后返回来
            incoming = savedInstanceState.getBoolean(Key.CALL_INCOMING, false);
            callType = savedInstanceState.getInt(Key.CALL_TYPE, -1);
            callNumber = savedInstanceState.getString(Key.CALL_NUMBER);
            callShowName = savedInstanceState.getString(Key.CALL_SHOW_NAME);
            ongoing = savedInstanceState.getBoolean(Key.CALL_ONGOING, false);
            callState = savedInstanceState.getInt(Key.CALL_STATE);
            mCount = savedInstanceState.getInt(Key.CALL_COUNT);
            callSpeakerOn = savedInstanceState.getBoolean(Key.CALL_SPEAKER_ON);
            callMute = savedInstanceState.getBoolean(Key.CALL_MUTE);
            callSession = savedInstanceState.getInt(Key.CALL_SESSION);
            callCreate = savedInstanceState.getBoolean(Key.CALL_CREATE, false);
            voiceMode = savedInstanceState.getInt(Key.CALL_VOICE_MODE, 0);
//            isAutoAnswer = savedInstanceState.getBoolean(Key.IS_AUTO_ANSWER, XIAOXI_VOIP_AUTO_ANSWER);
        } else {
            Intent intent = getIntent();
            incoming = intent.getBooleanExtra(Key.CALL_INCOMING, false);//默认false
            callType = intent.getIntExtra(Key.CALL_TYPE, -1);
            callNumber = intent.getStringExtra(Key.CALL_NUMBER);
            callShowName = intent.getStringExtra(Key.CALL_SHOW_NAME);
            ongoing = intent.getBooleanExtra(Key.CALL_ONGOING, false);//是否正在通话中
            callState = intent.getIntExtra(Key.CALL_STATE, -1000);
            mCount = intent.getIntExtra(Key.CALL_COUNT, 3);
            callSpeakerOn = intent.getBooleanExtra(Key.CALL_SPEAKER_ON, false);
            callMute = intent.getBooleanExtra(Key.CALL_MUTE, false);//默认为false
            callSession = intent.getIntExtra(Key.CALL_SESSION,0);
            callCreate = intent.getBooleanExtra(Key.CALL_CREATE, false);
            voiceMode = intent.getIntExtra(Key.CALL_VOICE_MODE, 0);
//            isAutoAnswer = intent.getBooleanExtra(Key.IS_AUTO_ANSWER, XIAOXI_VOIP_AUTO_ANSWER);
        }
        mediaPlayerManager = new MediaPlayerManager(BaseCallActivity.this);
        //开启屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    protected void speakerSet(boolean speakerOn) {
        //make video need open Speaker
        //audio default close Speaker
        btnSpeaker.setChecked(speakerOn);
    }


    /**
     * 设置静音
     *
     * @param mute
     */
    protected void muteDeaultSet(boolean mute) {
        btnMute.setChecked(mute);
        CMVoIPManager.getInstance().setInputMute(mute);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        logger.w("onSaveInstanceState");
        outState.putBoolean(Key.CALL_INCOMING, incoming);
        outState.putInt(Key.CALL_TYPE, callType);
        outState.putString(Key.CALL_NUMBER, callNumber);
        outState.putString(Key.CALL_SHOW_NAME, callShowName);
        outState.putBoolean(Key.CALL_ONGOING, ongoing);
        outState.putInt(Key.CALL_STATE, callState);
        outState.putInt(Key.CALL_COUNT, mCount);
        outState.putBoolean(Key.CALL_SPEAKER_ON, callSpeakerOn);
        outState.putBoolean(Key.CALL_MUTE, callMute);
        outState.putInt(Key.CALL_SESSION,callSession);
        outState.putInt(Key.CALL_VOICE_MODE,voiceMode);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //清除屏幕常亮flags
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //将秒数转换成时间来显示
    public String converCountToTime(int count) {
        StringBuilder sb = new StringBuilder();
        int hor = count / (60 * 60);
        int min = count % (60 * 60) / 60;
        int sec = count % 60;
        if (hor > 0) {
            if (hor <= 9) {
                sb.append("0");
            }
            sb.append(hor);
            sb.append(":");
        }
        if (min <= 9) {
            sb.append("0");
        }
        sb.append(min);
        sb.append(":");
        if (sec <= 9) {
            sb.append("0");
        }
        sb.append(sec);
        return sb.toString();
    }

}
