package littlec.conference.talk.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.cmri.moudleapp.moudlevoip.ICmccManager;
import com.cmri.moudleapp.moudlevoip.R;
import com.cmri.moudleapp.moudlevoip.bean.CallRecord;
import com.cmri.moudleapp.moudlevoip.bean.Contact;
import com.cmri.moudleapp.moudlevoip.manager.IVoipManager;
import com.cmri.moudleapp.moudlevoip.utils.CommonManagerUtil;
import com.cmri.moudleapp.moudlevoip.utils.DisplayUtils;
import com.cmri.moudleapp.moudlevoip.view.ContactHeadLayout;
import com.mobile.voip.sdk.api.CMVoIPManager;
import com.mobile.voip.sdk.api.utils.MyLogger;
import com.mobile.voip.sdk.api.utils.NetworkUtil;
import com.mobile.voip.sdk.api.utils.StringUtils;
import com.mobile.voip.sdk.callback.VoIP;
import com.mobile.voip.sdk.callback.VoIPCameraStatusCallBack;
import com.mobile.voip.sdk.callback.VoIPCodecObserver;
import com.mobile.voip.sdk.callback.VoIPMediaStaticsticCallBack;
import com.mobile.voip.sdk.constants.VoIPConstant;
import com.mobile.voip.sdk.model.MediaMember;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import littlec.conference.base.data.CallState;
import littlec.conference.base.data.ContactEventState;


/**
 * Created by zhangcong on 2017/8/16.
 */

public class CallVideoActivity extends BaseCallActivity implements VoIPMediaStaticsticCallBack, VoIPCameraStatusCallBack {

    private static final String TAG = CallVideoActivity.class.getSimpleName();

    private int remoteWidth = 0;
    private int remoteHeight = 0;
    private int localWidth = 0;
    private int localHeight = 0;

    private final int EVENT_COUNT_UP = 1;
    private final int EVENT_COUNT_DOWN = 2;
    private int countDown;
    private Timer timerUp;
    private Timer timerDown;
    int MIN_CLICK_DELAY_TIME = 5000;
    private boolean isMute = false;
    private TextView cameraErrorText;
    private Handler handler = new Handler();
    private boolean isAnswer = false;
    private boolean isComingEncodeErrorMsg = false;     //openGL创建失败的标志，用于判断是否重新添加本地视频
    private long callStartTime;

    private RelativeLayout rlAudioBg;
    private RelativeLayout answerLayout;
    private RelativeLayout outgoingLayout;
    private RelativeLayout comingLayout;
    private RelativeLayout rlCameraLocal;
    private RelativeLayout rlCameraRemote;

    private TextView tvName;
    private TextView tvNumber;
    private TextView tvDes;
    private TextView tvCountUp;
    private RelativeLayout rlInfo;
    private LinearLayout llDuration;
    private RelativeLayout answerBtnLayout;
    private ImageView ivAnswerHangup;
    private ImageView btnMute;
    private ImageView ivOutgoingHangup;
    private ImageView btnComingPickup;
    private ContactHeadLayout contactIncoming;
    private ContactHeadLayout contactAnswer;
    private int answerCount = 0;

    private boolean isHangup = false;   //是否挂断操作的标志

    //add by cll
    private ImageView btnTest;
    private RelativeLayout.LayoutParams layoutParams = null;
    private boolean isAction = false;
    private ImageView btnAction;
    private int width_ = 0;
    private int height_ = 0;
    private boolean isFinishSelf = false;


    private TextView tvFrame, tvCodeDown, tvCodeUp, tvRttms;
    private long preVideoByteSent = 0;
    private long preVideoByteRev = 0;
    private TableLayout tableLayout;
    private long lastTime = 0;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayerManager.isMediaPlaying()) {
                MyLogger.getLogger(TAG).i("media is playing over 40s, finish activity");
                mediaPlayerManager.stop();
                finishSelf();
            }
        }
    };

    protected void beforeInitViews() {
        callStartTime = System.currentTimeMillis();
        Contact contactInfo = ICmccManager.getInstance().queryContactByPhone(callNumber);
        if (contactInfo != null) {
            callShowName = contactInfo.getName();
        }
    }

    protected void initViews() {
        MyLogger.getLogger(TAG).i("initViews");
        IVoipManager.getInstance().CALLVIDEO = true;
        rlAudioBg = findViewById(R.id.rl_audio_bg);
        answerLayout = findViewById(R.id.answer_layout);
        outgoingLayout = findViewById(R.id.outgoing_layout);
        comingLayout = findViewById(R.id.coming_layout);
        rlCameraLocal = findViewById(R.id.rl_camera_local);
        rlCameraRemote = findViewById(R.id.rl_camera_remote);

        tvFrame = findViewById(R.id.tv_frame);
        tvCodeDown = findViewById(R.id.code_down);
        tvCodeUp = findViewById(R.id.code_up);
        tvRttms = findViewById(R.id.tv_rttms);
        tableLayout = findViewById(R.id.table_layout);

        tvName = findViewById(R.id.tv_name);
        tvNumber = findViewById(R.id.tv_number);
        tvDes = findViewById(R.id.tv_des);

        tvCountUp = findViewById(R.id.tv_count_up);
        rlInfo = findViewById(R.id.rl_info);
        llDuration = findViewById(R.id.ll_duration);
        answerBtnLayout = findViewById(R.id.answer_btn_layout);
        ivAnswerHangup = findViewById(R.id.iv_answer_hangup);
        btnMute = findViewById(R.id.btn_mute);
        ivOutgoingHangup = findViewById(R.id.iv_outgoing_hangup);
        btnComingPickup = findViewById(R.id.btn_coming_pickup);
        contactIncoming = findViewById(R.id.contact_incoming);
        contactAnswer = findViewById(R.id.contact_answer);
        btnTest = findViewById(R.id.btn_test);
        btnAction = findViewById(R.id.btn_action);
        btnAction.setEnabled(true);
        switch (callState) {
            case VoIPConstant.CALL_STATE_ANSWERED:
//                showVideoAnswerView(true);
                break;
            case VoIPConstant.CALL_STATE_ALERTING://当前手机处于振铃中，来电被呼起这个界面
            case VoIPConstant.CALL_STATE_STOP_ALERTING:
                if (incoming) {//来电
                    showComingView(true);
                } else {//去电，拨打电话
                    showGoingView(true);

                }
                break;
            default:
                if (incoming) {//来电
                    showComingView(true);
                } else {
                    //去电，拨打电话
                    showGoingView(true);
                    makeCall();
                }
                break;
        }
    }

    protected void initEvents() {
        if (NetworkUtil.isWifi(this)) {
            showToast(getResources().getString(R.string.wifi_tip));
        }
        CMVoIPManager.getInstance().registerMediaStaitcsticCallback(this);
        CMVoIPManager.getInstance().registerCameraStatusListener(this);
        CMVoIPManager.getInstance().registerVideoCodecObserver(voIPCodecObserver);

    }

    /**
     * 显示接通后的界面
     *
     * @param isShow
     */
    private void showVideoAnswerView(boolean isShow, int callType) {
        answerCount++;
        if (callType == VoIP.CallType.toInt(VoIP.CallType.CALLTYPE_1V1_AUDIO)) {
            rlAudioBg.setVisibility(View.VISIBLE);
            setContactHeadInfo(contactAnswer);
            rlCameraLocal.setVisibility(View.GONE);
            rlCameraRemote.setVisibility(View.GONE);
            btnAction.setVisibility(View.GONE);
            showToast("切换成语音接听");
        }
        answerLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        outgoingLayout.setVisibility(isShow ? View.GONE : View.VISIBLE);
        comingLayout.setVisibility(isShow ? View.GONE : View.VISIBLE);
        rlCameraLocal.setVisibility(isShow ? View.VISIBLE : View.GONE);
        ivAnswerHangup.requestFocus();
        rlCameraLocal.setVisibility(callType == VoIP.CallType.toInt(VoIP.CallType.CALLTYPE_1V1_AUDIO) ? View.GONE : View.VISIBLE);
//        binding.rlCameraLocal.setVisibility(View.VISIBLE);
        btnMute.setImageResource(isMute ? R.mipmap.voice_btn_press : R.mipmap.voice_btn);
        btnMute.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnMute.setImageResource(isMute ? R.mipmap.voice_btn_press_focus : R.mipmap.voice_btn_focus);
                } else {
                    btnMute.setImageResource(isMute ? R.mipmap.voice_btn_press : R.mipmap.voice_btn);
                }
            }
        });

        btnAction.setImageResource(isAction ? R.mipmap.btn_no_video : R.mipmap.btn_video_nor);
        btnAction.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnAction.setImageResource(isAction ? R.mipmap.btn_no_video_press : R.mipmap.btn_video_press);
                } else {
                    btnAction.setImageResource(isAction ? R.mipmap.btn_no_video : R.mipmap.btn_video_nor);
                }
            }
        });

        startCountUp();

        if (Build.PRODUCT.equals("Armstrong Q8311") && Build.MODEL.equals("Q8311")) {

        } else {
            if (answerCount < 2) {
                startCountDown();
            }
        }

        ShowLocalLayout(localWidth, localHeight);

    }

    /**
     * 去电时候界面
     *
     * @param isShow
     */
    private void showGoingView(boolean isShow) {
        outgoingLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        answerLayout.setVisibility(isShow ? View.GONE : View.VISIBLE);
        comingLayout.setVisibility(isShow ? View.GONE : View.VISIBLE);
        ivOutgoingHangup.requestFocus();
        rlCameraLocal.setVisibility(View.VISIBLE);

        //设置头像
        setAvatarView(false, false);

        //设置信息
        tvName.setText(TextUtils.isEmpty(callShowName) ? "好友" : callShowName);
        tvNumber.setText(callNumber);


        // 设置来去电标志
//        ivStatus.setImageDrawable(CommonUtil.isTvNum(callNumber)
//                ? getResources().getDrawable(R.drawable.ic_status_tv_out)
//                : CommonUtil.isPhoneNum(callNumber)
//                ? getResources().getDrawable(R.drawable.ic_status_phone_out)
//                : getResources().getDrawable(R.drawable.ic_status_tv_out));

        // 去电提示
//        tvDes.setVisibility(View.VISIBLE);
//        tvDes.setText(incoming ? "邀请您进行视频通话..." : "正在等待对方接受邀请...");
    }


    /**
     * @param isShow true 来电界面, false 会场界面
     * @author fang wei
     */
    private void showComingView(boolean isShow) {
        comingLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        answerLayout.setVisibility(isShow ? View.GONE : View.VISIBLE);
        outgoingLayout.setVisibility(isShow ? View.GONE : View.VISIBLE);
        btnComingPickup.requestFocus();

        setAvatarView(true, false);
        contactIncoming.setMakeInverted(true);
        floatAnim(contactIncoming);

        mediaPlayerManager.startInComingMusic(this);//来电需要播放
        handler.postDelayed(runnable, 40 * 1000);

       /* if (isAutoAnswer) {
            btnComingPickup.setClickable(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onPickupAction();
                    btnComingPickup.setClickable(true);
                    showToast("自动接听");

                }
            }, 3000);
        }*/

    }

    private void setContactHeadInfo(ContactHeadLayout contactLayout) {
        String avatar;
        String nick;
        Contact contact = ICmccManager.getInstance().queryContactByPhone(callNumber);

        if (contact != null) {
            avatar = contact.getAvatar();
            nick = contact.getName();
        } else {
            avatar = "";
            nick = callNumber;
        }
        contactLayout.setContactInfo(avatar, nick, callNumber);
    }

    private void setAvatarView(final boolean incoming, boolean isSwitchAudio) {
        setContactHeadInfo(contactIncoming);

        //设置头像
//        final String photoUrl = ContactMgr.getInstance().getPhotoByNum(callNumber);
        final String photoUrl = "";

//        if (isSwitchAudio) {
//            if (!TextUtils.isEmpty(photoUrl)) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Object loader = null;
//                        if (photoUrl.startsWith("http")) {
//                            loader = photoUrl;
//                        } else {
//                            loader = getResources().getIdentifier(photoUrl, "drawable", getPackageName());
//                        }
//                        Glide.with(getApplicationContext()).load(loader).asBitmap()
//                                .error(R.drawable.strange_activate)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
//                                .into(imgAudioAvatar);
//                    }
//                });
//            }
//            return;
//        }

        if (TextUtils.isEmpty(photoUrl)) {
//            if(incoming){
//                ivAvatarComing.setImageDrawable(getResources().getDrawable(R.drawable.strange_activate));
//            } else {
//                ivAvatar.setImageDrawable(getResources().getDrawable(R.drawable.strange_activate));
//            }

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Object loader = null;
                    if (photoUrl.startsWith("http")) {
                        loader = photoUrl;
                    } else {
                        loader = getResources().getIdentifier(photoUrl, "drawable", getPackageName());
                    }

//                    if(incoming){
//                        Glide.with(getApplicationContext()).load(loader).asBitmap()
//                                .error(R.drawable.strange_activate)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
//                                .into(ivAvatarComing);
//
//                    } else {
//                        Glide.with(getApplicationContext()).load(loader).asBitmap()
//                                .error(R.drawable.strange_activate)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
//                                .into(ivAvatar);
//                    }


                }
            });
        }
    }

    private void makeCall() {
        if (ongoing) {
            // 如果正在通话之中再次进入，则不再拨打电话
            MyLogger.getLogger(TAG).e("not call out when ongoing!");
            return;
        }

        CMVoIPManager.getInstance().callOut(callNumber, callType, mDialCallBack);
        ShowLocalLayout(DisplayUtils.getDisplayWidth(this), DisplayUtils.getDisplayRealHeight(this));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateChanged(CallState callState) {
        MyLogger.getLogger(TAG).d("onCallStateChanged: " + callState.callStateCode);
        if (callState.callStateCode == VoIPConstant.CALL_STATE_ANSWERED) {
            this.callState = callState.callStateCode;
            this.callType = callState.callType;
            MyLogger.getLogger(TAG).d("HqCallActivity answer, callType: " + callType);
            showVideoAnswerView(true, callType);
            isAnswer = true;

//            tableLayout.setVisibility(View.VISIBLE);

        } else if (callState.callStateCode == VoIPConstant.CALL_STATE_RELEASED) {
//            this.callState = VoIPConstant.CALL_STATE_RELEASED;
//            if (mediaPlayerManager != null) {
//                mediaPlayerManager.stop();
//            }
////            System.out.println("通话结束1");
//            finishSelf();
//            showToast("通话结束");

        } else if (callState.callStateCode == VoIPConstant.CALL_STATE_SWITCH_RESULT) {
            if (callState.callType != callType && callState.callType == VoIPConstant.CallType.CALLTYPE_1V1_AUDIO) {
                callType = VoIPConstant.CallType.CALLTYPE_1V1_AUDIO;

                showVideoAnswerView(true, callType);
                setAvatarView(false, true);

            }
        } else if (callState.callStateCode == VoIPConstant.VOIP_STATE_CALL_FORWARD) {
            tvDes.setText("正在自动转接高清线路，请稍后……");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String IMSCallNumber = "+86" + StringUtils.getStringWithOutAppKey(callNumber);
                    CMVoIPManager.getInstance().callOut(IMSCallNumber, VoIPConstant.CallType.CALLTYPE_1V1_VIDEO_IMS, mDialCallBack);
                    ShowLocalLayout(DisplayUtils.getDisplayWidth(CallVideoActivity.this), DisplayUtils.getDisplayRealHeight(CallVideoActivity.this));
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(Integer state) {
        switch (state) {
            case EVENT_VIDEO_LOST://20001
                if (callType == VoIPConstant.CallType.CALLTYPE_1V1_VIDEO) {
                    showToast("信号不稳定");
                }
                break;
            default:
                break;
        }
    }

    public void onMuteClick(View view) {
        isMute = !isMute;
        CMVoIPManager.getInstance().setInputMute(isMute);
        btnMute.setImageResource(isMute ? R.mipmap.voice_btn_press_focus : R.mipmap.voice_btn_focus);
        if (isMute) {
            showToast("静音开启");
        } else {
            showToast("静音关闭");
        }
        removeCountDownAndRestart();
    }

    public void onActionClick(View view) {
        isAction = !isAction;
        if (layoutParams != null) {
            btnAction.setImageResource(isAction ? R.mipmap.btn_no_video_press : R.mipmap.btn_video_press);
            if (isAction) {
                MyLogger.getLogger(TAG).d("onActionClick width_ :" + width_ + ",height_ :" + height_);
                CMVoIPManager.getInstance().StopTVLocalVideoAndInfoRemote();
                if (width_ * 9 / 16 == height_) {
                    btnTest.setImageResource(R.drawable.no_camera);
                } else {
                    btnTest.setImageResource(R.drawable.no_camera_1);
                }
                rlCameraLocal.addView(btnTest, layoutParams);
                showToast("关闭摄像头");
            } else {
                CMVoIPManager.getInstance().StartTVLocalVideoAndInfoRemote();
                rlCameraLocal.removeView(btnTest);
                showToast("开启摄像头");
            }
        }
        removeCountDownAndRestart();
    }

    private void onHangUpAction() {
        MyLogger.getLogger(TAG).d("onHangUpAction autoHangup :" + autoHangup);
        autoHangup = false;
        isHangup = true;
        CMVoIPManager.getInstance().hangUpCall(callSession);
        callState = VoIPConstant.CALL_STATE_RELEASED;
        EventBus.getDefault().post(new CallState(callState));
        if (mediaPlayerManager != null) {
            mediaPlayerManager.stop();
        }
        finishSelf();
    }

    public void onHangUpClick(View view) {
        System.out.println("CallVideoActivity挂断");
        MyLogger.getLogger(TAG).d("onHangUpClick: 点击挂断按键");
        if (CommonManagerUtil.canClick(MIN_CLICK_DELAY_TIME)) {
            onHangUpAction();
        }
        Intent intent = new Intent();
        intent.setAction("HangUp");
        sendBroadcast(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.setAction("HangUp");
        sendBroadcast(intent);
        finish();
    }

    public void onPickUpClick(View view) {
        MyLogger.getLogger(TAG).d("onPickUpClick: 点击接通按键");
        if (CommonManagerUtil.canClick(MIN_CLICK_DELAY_TIME)) {
            onPickupAction();
        }
    }

    private void onPickupAction() {
        MyLogger.getLogger(TAG).d("zzz onPickupAction autoHangup :" + autoHangup);
        autoHangup = false;
        int result = CMVoIPManager.getInstance().pickUpCall(callSession);
        if (result != 0) {
            showToast("接听失败");
            finishSelf();
        }
        mediaPlayerManager.stop();
        //接听电话，音量键为通话中的音量键
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//        ShowLocalLayout(localWidth, localHeight);
    }

   /* @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventState(EventState msg) {
        int screenWidth = DisplayUtils.getDisplayWidth(this);
        int screenHeight = DisplayUtils.getDisplayRealHeight(this);

        switch (msg.code) {
            case EventState.INCOMING_CODEC_CHANGED: // 远端视频长宽 90015
                MyLogger.getLogger(TAG).i("sdk returns remote resolution ：" + ",width：" + msg.width + ",height" + msg.height);

                remoteWidth = screenWidth;
                remoteHeight = screenHeight;
                float dataBack = (float)msg.height / msg.width;
                float dataLocal = (float) remoteHeight / remoteWidth;

                if (dataBack < dataLocal) {
                    remoteHeight = remoteWidth * msg.height / msg.width;
                } else {
                    remoteWidth = remoteHeight * msg.width / msg.height;
                }
                if (callType == VoIP.CallType.toInt(VoIP.CallType.CALLTYPE_1V1_VIDEO)) {
                    if (isComingEncodeErrorMsg) {
                        isComingEncodeErrorMsg = false;
                        MyLogger.getLogger(TAG).i("sdk return re-showLocal");
                        ShowLocalLayout(localWidth, localHeight);
                    }
                    ShowRemoteLayout(remoteWidth, remoteHeight);
                }

                break;

            case EventState.OUTGOING_CODEC_CHANGED:// 本地视频长宽 90016
                MyLogger.getLogger(TAG).i("sdk returns local resolution" + ",width：" + msg.width + ",height" + msg.height);

                localHeight = (int) (0.35*DisplayUtils.getDisplayRealHeight(this));
                if(msg.height < msg.width){
                    localWidth = localHeight * msg.width / msg.height;
                } else { // 防止底层宽高推错
                    localWidth = localHeight * msg.height / msg.width;
                }

                ShowLocalLayout(localWidth, localHeight);
                break;
            case EventState.VOIP_ENCODE_MODE_CHANGED:
                MyLogger.getLogger(TAG).e("sdk return encode error msg, need re-addView");
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        ShowLocalLayout(true, localWidth,localHeight);
//                    }
//                }, 2000);
                isComingEncodeErrorMsg = true;
                break;
            default:
                break;
        }
    }*/

    /**
     * 显示远端视频
     *
     * @param
     * @param remoteWidth
     * @param remoteHeight
     */
    private void ShowRemoteLayout(int remoteWidth, int remoteHeight) {
        MyLogger.getLogger(TAG).e("ShowRemoteLayout ，remoteWidth:" + remoteWidth + ",remoteHeight:" + remoteHeight);

        SurfaceView sv_remote = CMVoIPManager.getInstance().getRemoteRenderSurfaceView(callSession);
        if (remoteWidth == 0 || remoteHeight == 0) {
            remoteWidth = 720;
            remoteHeight = 1080;
        }

        if (rlCameraRemote.getChildCount() == 0) {
            rlCameraRemote.addView(sv_remote, 0, new RelativeLayout.LayoutParams(remoteWidth, remoteHeight));
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins((DisplayUtils.getDisplayWidth(this) - remoteWidth) / 2, 0, 0, 0);

            rlCameraRemote.setLayoutParams(lp);
        } else {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(remoteWidth, remoteHeight);
            lp.setMargins((DisplayUtils.getDisplayWidth(this) - remoteWidth) / 2, 0, 0, 0);
            rlCameraRemote.setLayoutParams(lp);
            sv_remote.setLayoutParams(new RelativeLayout.LayoutParams(remoteWidth, remoteHeight));
        }

    }

    /**
     * 设置本地视频
     *
     * @param
     * @param localWidth
     * @param localHeight
     */
    private void ShowLocalLayout(int localWidth, int localHeight) {
        MyLogger.getLogger(TAG).e("ShowLocalLayout ，localWidth:" + localWidth + ",localHeight:" + localHeight + ",callState:" + callState);
        View sv_local = CMVoIPManager.getInstance().getLocalPreviewSurfaceView(callSession);//得到本地的画布
        if (sv_local == null) {
            MyLogger.getLogger(TAG).e("ShowLocalLayout ，sv_local: null");
            return;
        }
        // 如果底层没有返回，设置随意值显示
        if (localWidth == 0 || localHeight == 0) {
            localWidth = (int) (0.35 * DisplayUtils.getDisplayWidth(this));
            localHeight = (int) (0.35 * DisplayUtils.getDisplayRealHeight(this));
        }
        if (CMVoIPManager.getInstance().getCameraStatus(callSession) != 0) {
            if (cameraErrorText == null) {
                cameraErrorText = new TextView(this);
                cameraErrorText.setBackgroundColor(getResources().getColor(R.color.black));
                cameraErrorText.setGravity(Gravity.CENTER);
                cameraErrorText.setTextColor(getResources().getColor(R.color.side_text_color));
                cameraErrorText.setTextSize(20);
                cameraErrorText.setText(R.string.camera_error_tip);
            }
            sv_local = cameraErrorText;
            btnAction.setEnabled(false);
        }

        ViewGroup svLocalParent = (ViewGroup) sv_local.getParent();

        rlCameraLocal.removeAllViews();
        if (null != svLocalParent) {
            svLocalParent.removeAllViews();
        }

//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(localWidth, localHeight);
        layoutParams = new RelativeLayout.LayoutParams(localWidth, localHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (callState == VoIPConstant.CALL_STATE_ANSWERED) {
            layoutParams.setMargins(0, DisplayUtils.getDisplayRealHeight(this) - localHeight, 0, 0);
        }
        MyLogger.getLogger(TAG).e("ShowLocalLayout ，addview ");
        rlCameraLocal.addView(sv_local, 0, layoutParams);
    }

    private Handler timeHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
            switch (msg.what) {
                case EVENT_COUNT_UP:
                    tvCountUp.setText(converCountToTime(mCount++));
                    break;
                case EVENT_COUNT_DOWN:
                    countDown--;
                    if (countDown == 0 && callState == VoIPConstant.CALL_STATE_ANSWERED && callType == VoIPConstant.CallType.CALLTYPE_1V1_VIDEO) {
                        rlInfo.setVisibility(View.GONE);
                        llDuration.setVisibility(View.GONE);
                        answerBtnLayout.setVisibility(View.GONE);
                        stopCountDown();
                    }

                    break;
                default:
                    break;
            }
        }
    };

    private void startCountUp() {
        if (timerUp != null) {
            timerUp.cancel();
            timerUp = null;
        }
        timerUp = new Timer();

        timerUp.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = EVENT_COUNT_UP;
                timeHandler.sendMessage(message);
            }
        }, 0, 1 * 1000);
        mCount = 0;
    }

    private void startCountDown() {
        if (timerDown != null) {
            timerDown.cancel();
            timerDown = null;
        }
        timerDown = new Timer();

        timerDown.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = EVENT_COUNT_DOWN;
                timeHandler.sendMessage(message);
            }
        }, 0, 1 * 1000);
        countDown = 10;
    }


    private void stopCountDown() {
        if (timerDown != null) {
            timerDown.cancel();
            timerDown = null;
        }
    }

    private void stopCountUp() {
        if (timerUp != null) {
            timerUp.cancel();
            timerUp = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hq_call);
        beforeInitViews();
        initViews();
        initEvents();
    }

    @Override
    protected void onResume() {
        MyLogger.getLogger(TAG).d("onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        MyLogger.getLogger(TAG).d("onPause finishSelf :" + isFinishSelf);
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackKeyPressed(null);
            return true;
        } else {
            if (countDown == 0 && callState == VoIPConstant.CALL_STATE_ANSWERED) {
                startCountDown();
                rlInfo.setVisibility(View.GONE);
                llDuration.setVisibility(View.VISIBLE);
                answerBtnLayout.setVisibility(View.VISIBLE);
                ivAnswerHangup.requestFocus();
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                timeHandler.removeMessages(EVENT_COUNT_DOWN);
                countDown = 10;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                startCountDown();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void removeCountDownAndRestart() {
        timeHandler.removeMessages(EVENT_COUNT_DOWN);
        countDown = 10;
        startCountDown();
    }

    public void onBackKeyPressed(View v) {
        if (callState == VoIPConstant.CALL_STATE_INCOMING) {
            return;
        }
        /*if (callState != VoIPConstant.CALL_STATE_RELEASED) {
            //TODO
        }*/
//        finish();
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    int count = 0;

    @Override
    public void onUpdateStaticstic(int rttms, int audiobitrate, int audiolostrate, long audioBytesSent, long audioBytesReceived, int videobitrate, int videolostrate, int framerate, long videoBytesSent, long videoBytesReceived) {
//        MyLogger.getLogger(TAG).i("videolostrate： " + videolostrate);
        if (videolostrate > 10) {
            //连续四次丢包率达到10以上，给予提示
            count++;
            if (count == 3) {
                EventBus.getDefault().post(EVENT_VIDEO_LOST);
                count = 0;
            }
        } else {
            count = 0;
        }

        final String frame = framerate + " fps";
        final double codeUp = (videoBytesSent - preVideoByteSent) / (8 * 1024);
        final double codeDown = (videoBytesReceived - preVideoByteRev) / (8 * 1024);
        final String rttm = "" + rttms;
//        lastTime = curTime;
        preVideoByteSent = videoBytesSent;
        preVideoByteRev = videoBytesReceived;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvFrame.setText(frame);
                tvCodeUp.setText("" + codeUp + "kbps");
                tvCodeDown.setText("" + codeDown + "kbps");
                tvRttms.setText("" + rttm);
            }
        });
    }

    @Override
    public void onUpdateConferenceStaticstic(List<MediaMember> mediaMembers) {

    }


    public void finishSelf() {
        MyLogger.getLogger(TAG).d("finishSelf :" + isFinishSelf);
        isFinishSelf = true;
        CMVoIPManager.getInstance().deregisterMediaStaitcsticCallback();
        CMVoIPManager.getInstance().unregisterCameraStatusListener();
        CMVoIPManager.getInstance().deregisterVideoCodecObserver();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountUp();
        stopCountDown();
//        CMVoIPManager.getInstance().deregisterMediaStaitcsticCallback();
//        CMVoIPManager.getInstance().unregisterCameraStatusListener();
//        CMVoIPManager.getInstance().deregisterVideoCodecObserver();

        handler.removeCallbacks(runnable);
        int callLogType = CallLog.Calls.MISSED_TYPE;
        MyLogger.getLogger(TAG).d("zzz onDestroy autoHangup :" + autoHangup + ", incoming :" + incoming);
        if (incoming) {
            if (autoHangup) {
                callLogType = CallLog.Calls.MISSED_TYPE;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    callLogType = mCount == 0 ? CallLog.Calls.REJECTED_TYPE : CallLog.Calls.INCOMING_TYPE;
                } else {
                    callLogType = CallLog.Calls.INCOMING_TYPE;
                }
            }
        } else {
            callLogType = CallLog.Calls.OUTGOING_TYPE;
        }


        CallRecord bean = new CallRecord();
        bean.setCall_number(callNumber);
        bean.setDirection(callLogType);
        bean.setStart_time(callStartTime);
        bean.setLast_time(mCount);
        bean.setCallType(VoIPConstant.CallType.CALLTYPE_1V1_VIDEO);
        ICmccManager.getInstance().insertCallRecord(bean);
//        EventBus.getDefault().post(new ContactEventState.RecordState(ContactEventState.REFRESH_TV_RECORD, ""));

        //add by cll
        layoutParams = null;

        IVoipManager.getInstance().CALLVIDEO = false;
    }



    private void floatAnim(View view) {
        ObjectAnimator translationYAnim = ObjectAnimator.ofFloat(view, "translationY", -40.0f, 40.0f, -40.0f);
        translationYAnim.setDuration(5000);
        translationYAnim.setRepeatCount(ValueAnimator.INFINITE);
        translationYAnim.setRepeatMode(ValueAnimator.REVERSE);
        translationYAnim.start();
    }

    @Override
    public void onOpenCameraError() {
        //底层开启摄像头失败时处理显示文字
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cameraErrorText == null) {
                    cameraErrorText = new TextView(CallVideoActivity.this);
                    cameraErrorText.setBackgroundColor(getResources().getColor(R.color.black));
                    cameraErrorText.setGravity(Gravity.CENTER);
                    cameraErrorText.setTextColor(getResources().getColor(R.color.side_text_color));
                    cameraErrorText.setTextSize(20);
                    cameraErrorText.setText(R.string.camera_error_tip);

                    ViewGroup svLocalParent = (ViewGroup) cameraErrorText.getParent();
                    rlCameraLocal.removeAllViews();
                    if (null != svLocalParent) {
                        svLocalParent.removeAllViews();
                    }
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(localWidth, localHeight);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.ALIGN_PARENT_BOTTOM);
                    rlCameraLocal.addView(cameraErrorText, 0, layoutParams);
                    MyLogger.getLogger(TAG).e("onOpenCameraError ，add error tip");

                    btnAction.setEnabled(false);
                }
            }
        });
    }


    private boolean localAddComplete = false;
    private VoIPCodecObserver voIPCodecObserver = new VoIPCodecObserver() {
        @Override
        public void incomingRate(int session, int channel, int framerate, int bitrate) {

        }

        @Override
        public void DecoderTiming(int decode_ms, int max_decode_ms, int current_delay_ms, int target_delay_ms, int jitter_buffer_ms, int min_playout_delay_ms, int render_delay_ms) {

        }

        @Override
        public void incomingCodecChanged(int session, int channel, int width, int height) {
            int screenWidth = DisplayUtils.getDisplayWidth(CallVideoActivity.this);
            int screenHeight = DisplayUtils.getDisplayRealHeight(CallVideoActivity.this);
            MyLogger.getLogger(TAG).i("sdk returns remote resolution ：" + ",width：" + width + ",height" + height);

            remoteWidth = screenWidth;
            remoteHeight = screenHeight;
            float dataBack = (float) height / width;
            float dataLocal = (float) remoteHeight / remoteWidth;

            if (dataBack < dataLocal) {
                remoteHeight = remoteWidth * height / width;
            } else {
                remoteWidth = remoteHeight * width / height;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (callType == VoIP.CallType.toInt(VoIP.CallType.CALLTYPE_1V1_VIDEO)) {
                        if (isComingEncodeErrorMsg) {
                            isComingEncodeErrorMsg = false;
                            MyLogger.getLogger(TAG).i("sdk return re-showLocal");
                            ShowLocalLayout(localWidth, localHeight);
                        }
                        ShowRemoteLayout(remoteWidth, remoteHeight);
                    }
                }
            });


        }

        @Override
        public void requestNewKeyFrame(int session, int Channel) {

        }

        @Override
        public void outgoingRate(int session, int channel, int framerate, int bitrate) {

        }

        @Override
        public void suspendChange(int session, int channel, boolean is_suspended) {

        }

        @Override
        public void outgoingCodecChanged(int session, int channel, int width, int height) {
            MyLogger.getLogger(TAG).i("sdk returns local resolution" + ",width：" + width + ",height" + height);

            width_ = width;
            height_ = height;
            localHeight = (int) (0.35 * DisplayUtils.getDisplayRealHeight(CallVideoActivity.this));
            if (height < width) {
                localWidth = localHeight * width / height;
            } else { // 防止底层宽高推错
                localWidth = localHeight * height / width;
            }
            localAddComplete = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowLocalLayout(localWidth, localHeight);
                    localAddComplete = true;
                }
            });
            //等待本地视频加载完成再进行下面的操作
            for (; ; ) {
                //挂断操作后，如还在循环，跳出，防止线程阻塞
                if (isHangup) return;
                if (localAddComplete) {
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void videoEncodeModeChanged(int session, boolean isOpengl) {
            isComingEncodeErrorMsg = true;
        }
    };


    // TODO: 2017/11/17 网络断开直接挂机？？？？？ 
   /* @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkStateChanged(WifiState wifiState) {
        MyLogger.getLogger(TAG).i("onNetworkStateChanged wifiState.wifiState:" + wifiState.wifiState);
        switch (wifiState.wifiState) {
            case WifiState.WIFI_CONNECTED:
                break;
            case WifiState.WIFI_DISCONNECTED:
                if (callState != VoIPConstant.CALL_STATE_ANSWERED) {
                    CMVoIPManager.getInstance().hangUpCall(callSession);
                    if (mediaPlayerManager != null) {
                        mediaPlayerManager.stop();
                    }
                    finishSelf();
                }
                break;
        }
    }*/

    // TODO: 2017/11/17 挂机？？？？？？ 
    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onDoHangupCall(VoipEventState.DoHangupCall doHangupCall) {
        onHangUpAction();
    }*/
}
