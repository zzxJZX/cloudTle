package littlec.conference.talk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmri.moudleapp.moudlevoip.ICmccManager;
import com.cmri.moudleapp.moudlevoip.R;
import com.cmri.moudleapp.moudlevoip.bean.AccountInfo;
import com.cmri.moudleapp.moudlevoip.bean.CallRecord;
import com.cmri.moudleapp.moudlevoip.bean.Contact;
import com.cmri.moudleapp.moudlevoip.manager.IVoipManager;
import com.cmri.moudleapp.moudlevoip.utils.DisplayUtils;
import com.cmri.moudleapp.moudlevoip.view.ContactHeadLayout;
import com.mobile.voip.sdk.api.CMVoIPManager;
import com.mobile.voip.sdk.api.utils.MyLogger;
import com.mobile.voip.sdk.api.utils.NetworkUtil;
import com.mobile.voip.sdk.api.utils.StringUtils;
import com.mobile.voip.sdk.callback.VoIP;
import com.mobile.voip.sdk.callback.VoIPCodecObserver;
import com.mobile.voip.sdk.callback.VoIPConferenceDataCallBack;
import com.mobile.voip.sdk.callback.VoIPConferenceInfoCallBack;
import com.mobile.voip.sdk.callback.VoIPInstantConferenceCallBack;
import com.mobile.voip.sdk.callback.VoIPMediaStaticsticCallBack;
import com.mobile.voip.sdk.constants.VoIPConstant;
import com.mobile.voip.sdk.model.MediaMember;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import littlec.conference.base.data.CallState;
import littlec.conference.base.data.ContactEventState;
import littlec.conference.base.data.EventState;
import littlec.conference.talk.adapter.ConferenceVideoAdapter;
import littlec.conference.talk.callback.MemberDiffUtilCallback;
import littlec.conference.talk.model.ConferenceData;
import littlec.conference.talk.model.ConferenceMember;

/**
 * Created by zhangcong on 2017/8/16.
 */

public class ConferenceVideoActivity extends BaseCallActivity implements VoIPConferenceInfoCallBack, View.OnFocusChangeListener, VoIPMediaStaticsticCallBack {
    private static final String TAG = ConferenceVideoActivity.class.getSimpleName();
    public static final String FAKE_USER = "99999999";
    public static final int INVITE_REQUEST_CODE = 100;
    public static final String RESULT_SELECTED_LIST = "str_select_list";

    private String userName;
    private String accessCode = "";      //会议号
    private String creatorPhone = "";    //创建者电话号码（不包含key）
    private String creatorId = "";    //即创建者用户id
    private int screenWidth;
    private int screenHeight;
    private ConferenceVideoAdapter conferenceVideoAdapter;
    private List<String> conferMemberList = new ArrayList<>();
    private VoIPInstantConferenceCallBack mInstantCallBack;
    private Map<Integer, String> hwMap = new HashMap<>();
    private long lastClickTime = 0;
    private int MIN_CLICK_DELAY_TIME = 5000;
    private boolean isMute = false;
    protected Timer mTimer;
    private Handler handlerRing = new Handler();
    private long callStartTime;
    private List<String> members;

    private boolean isAnswer = false;

    private RelativeLayout comingLayout;
    private RelativeLayout answerLayout;
    private ImageView btnAnswer;
    private ImageView btnClose;
    private ImageView btnMute;
    private ImageView photoIv;

    private RecyclerView videoRecyclerView;
    private TextView timeTxt;


    private ContactHeadLayout contactHostIncoming;
    private ContactHeadLayout contactIncomingOne;
    private ContactHeadLayout contactIncomingTwo;
    private ContactHeadLayout contactIncomingThree;
    private List<ContactHeadLayout> contactLayoutList = new ArrayList<>();

    /**
     * 当前显示的列表
     */
    private List<ConferenceMember> currentMembers = new ArrayList<>();

    /**
     * 更新来的新列表
     */
    private List<ConferenceMember> newMembers = new ArrayList<>();

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
        AccountInfo accountInfo = IVoipManager.getInstance().getAccount();
        userName = accountInfo.getImsNum();
        members = getIntent().getStringArrayListExtra(Key.CALL_MEMBERS);
        if (members != null) {
            for (String str : members) {
                conferMemberList.add(str);
            }
        }
        callStartTime = System.currentTimeMillis();
    }

    protected void initViews() {
        IVoipManager.getInstance().CONFERENCE = true;
        comingLayout = findViewById(R.id.coming_layout);
        answerLayout = findViewById(R.id.answer_layout);
        btnAnswer = findViewById(R.id.btn_answer);
        btnClose = findViewById(R.id.btn_close);
        btnMute = findViewById(R.id.btn_mute);
        photoIv = findViewById(R.id.photo_iv);
        videoRecyclerView = findViewById(R.id.video_recycler_view);
        timeTxt = findViewById(R.id.time_txt);

        contactHostIncoming = findViewById(R.id.contact_incoming);
        contactIncomingOne = findViewById(R.id.contact_incoming_item_1);
        contactIncomingTwo = findViewById(R.id.contact_incoming_item_2);
        contactIncomingThree = findViewById(R.id.contact_incoming_item_3);

        contactLayoutList.add(contactHostIncoming);
        contactLayoutList.add(contactIncomingOne);
        contactLayoutList.add(contactIncomingTwo);
        contactLayoutList.add(contactIncomingThree);
        initData();
    }


    protected void initEvents() {

        videoRecyclerView.setOnFocusChangeListener(this);
        int xSpace = getResources().getDimensionPixelSize(R.dimen.px_positive_2);
        int ySpace = getResources().getDimensionPixelSize(R.dimen.px_positive_2);
        screenWidth = DisplayUtils.getDisplayRealWidth(this) - xSpace;
        screenHeight = DisplayUtils.getDisplayRealHeight(this) - ySpace;
        videoRecyclerView.setItemAnimator(new DefaultItemAnimator());
        videoRecyclerView.addItemDecoration(new SpaceItemDecoration(this, xSpace, ySpace));
        videoRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        conferenceVideoAdapter = new ConferenceVideoAdapter(this, accessCode, currentMembers, screenWidth, screenHeight);
        videoRecyclerView.setAdapter(conferenceVideoAdapter);

        /*conferenceVideoAdapter.setOnItemClickListener(new ConferenceVideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                ArrayList<String> selectedList = new ArrayList<>();
                Iterator<ConferenceMember> iterator = conferenceVideoAdapter.getData().iterator();
                while (iterator.hasNext()) {
                    ConferenceMember member = iterator.next();
                    if (!member.getUserName().equals(ConferenceVideoActivity.FAKE_USER)) {
                        selectedList.add(member.getUserName());
                    }
                }
                // TODO: 2017/8/17 多方中邀请人
//                Choick.launch(view.getContext(), selectedList, accessCode);
                IContactManager.getInstance().actionChoiceContact(ConferenceVideoActivity.this, 100, creatorPhone, selectedList, selectedList, 4, 2);
            }
        });*/
        switch (callState) {
            case VoIPConstant.CALL_STATE_ANSWERED:
                initTimeTask();
                break;
            default:
                if (incoming) {
                    //来电
                    initComingData();
                    showComingView(true);
                    autoAnswer();
                } else {
                    showComingView(false);
                    setCallOutVoIPCallBack();
                    //去电，拨打电话
                    makeCall(callType, callCreate);
                }
                break;
        }

        CMVoIPManager.getInstance().registerVideoCodecObserver(voIPCodecObserver);

        CMVoIPManager.getInstance().addConferenceInfoListener(userName, callType, this);

        //添加视频丢包率的
        CMVoIPManager.getInstance().registerMediaStaitcsticCallback(this);

        if (NetworkUtil.isWifi(this)) {
            showToast(getResources().getString(R.string.wifi_tip));
        }
    }

    // 通过会议号获取发起者号码
    private void initComingData() {
        creatorPhone = accessCode = callNumber;

        CMVoIPManager.getInstance().requestMeetingInfo(userName, callNumber, new VoIPConferenceDataCallBack() {
            @Override
            public void onSuccess(com.mobile.voip.sdk.model.ConferenceData data) {
                creatorPhone = StringUtils.getStringWithOutAppKey(data.getCreator());
                MyLogger.getLogger(TAG).i("getMeetingInfo success : " + creatorPhone);
                setContactLayoutInfo(creatorPhone, contactHostIncoming);

                if (data.getMediaMembers() != null) {
                    int flag = 1;
                    conferMemberList.clear();
                    for (MediaMember member : data.getMediaMembers()) {
                        if (!member.getUserName().equals(ConferenceVideoActivity.FAKE_USER)) {
                            conferMemberList.add(member.getUserName());
                            if (!member.getUserName().equals(creatorPhone)) {
                                setContactLayoutInfo(member.getUserName(), contactLayoutList.get(flag));
                                flag++;
                            }

                        }
                    }
                }
            }

            @Override
            public void onFailed(int code, String error) {
                MyLogger.getLogger(TAG).i("getMeetingInfo fail : " + code + ", error: " + error);
                setContactLayoutInfo("好友", contactHostIncoming);
            }
        });

    }


    private void setContactLayoutInfo(String phone, ContactHeadLayout layout) {

        if (layout != contactHostIncoming) {
            if (phone.equals(creatorPhone)) {
                return;
            }
        }

        layout.setVisibility(View.VISIBLE);
        Contact contact = ICmccManager.getInstance().queryContactByPhone(phone);
        Contact newContact = new Contact();

        String avatarUrl, nick;
        if (contact != null) {
            newContact.setAvatar(contact.getAvatar());
            newContact.setName(contact.getName());
            avatarUrl = newContact.getAvatar();
            nick = newContact.getName();
        } else {
            avatarUrl = "";
            nick = phone;
        }

        layout.setContactInfo(avatarUrl, nick, phone);
    }


    /**
     * 设置默认数据
     */
    private void initData() {
        ConferenceData conferenceData = new ConferenceData();
        if (TextUtils.isEmpty(callShowName)) {
            callShowName = callNumber;
        }
        conferenceData.setConfName(callShowName);
        conferenceData.setConfNumber(callNumber);
    }

    private void setCallOutVoIPCallBack() {
        //创建会议的回调
        mInstantCallBack = new VoIPInstantConferenceCallBack() {
            /**
             * @param conferenceNumber 会议号码
             * @param conferenceId     会议id
             */
            @Override
            public void onSuccess(int session, String conferenceNumber, String conferenceId, int csTime) {
                MyLogger.getLogger(TAG).i("创建onSuccess ,conferenceNumber: " + conferenceNumber + " ,conferenceId：" + conferenceId + " session " + session);
                callSession = session;
            }

            @Override
            public void onError(String errorString) {
                MyLogger.getLogger(TAG).i("创建失败: " + errorString);

                if (errorString.contains("-1003")) {
//                    showToast("无法创建会议，请先结束上一次会再创建新的会议");
                } else if (errorString.contains("-1004")) {
//                    showToast("会议成员达到最大数");
                } else {
//                    showToast("创建会议失败");
                }
                doMakeCallFailed();
            }
        };

    }

    //设置头像
    private void setAvatarView() {
//        String photoUrl = ContactMgr.getInstance().getPhotoByNum(creatorPhone);
        String photoUrl = "";

        if (!TextUtils.isEmpty(photoUrl)) {
//            Glide.with(this).load(photoUrl).error(R.drawable.strange_activate).into(photoIv);
        }
    }

    private void makeCall(int callType, boolean bCreate) {
        if (ongoing) {
            return;
        }

        if (bCreate) {
            //创建会议
            List<String> mMemberList = new ArrayList<>();
            /*for (int i = 0; i < members.size() ; i++) {
                if (members.get(i).getIsPhone() == ContactInfo.TEL_TYPE_PHONE) {
                    mMemberList.add(members.get(i).getPhoneNums().get(0));
                } else {
                    mMemberList.add(members.get(i).getTvNums().get(0));
                }
            }*/

            for (int i = 0; i < members.size(); i++) {
                mMemberList.add(members.get(i));

            }
            CMVoIPManager.getInstance().createConference(mInstantCallBack, callType, callShowName, mMemberList);
        } else {
            //加入会议
            CMVoIPManager.getInstance().joinConference(callNumber, callType, mDialCallBack);
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(CallState state) {
        MyLogger.getLogger(TAG).d("onEventMainThread state:" + state.callStateCode);
        switch (state.callStateCode) {
            case VoIPConstant.CALL_STATE_RELEASED://0
                //通话结束
                if (mTimer != null) {
                    mTimer.cancel();
                }
                callState = VoIPConstant.CALL_STATE_RELEASED;
                if (mediaPlayerManager != null) {
                    mediaPlayerManager.stop();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishSelf();
//                        System.out.println("通话结束2");
                        showToast("通话结束");
                        MyLogger.getLogger(TAG).i("ConferenceVideoActivity finish");
                    }
                }, 400);
                break;
            case VoIPConstant.CALL_STATE_PROCEEDING://1
                callState = VoIPConstant.CALL_STATE_PROCEEDING;
                break;
            case VoIPConstant.CALL_STATE_ALERTING://5
                callState = VoIPConstant.CALL_STATE_ALERTING;
                break;
            case VoIPConstant.CALL_STATE_STOP_ALERTING://6
                callState = VoIPConstant.CALL_STATE_STOP_ALERTING;
                break;
            case VoIPConstant.CALL_STATE_ANSWERED://3
                isAnswer = true;
                mCount = 0;
                initTimeTask();
                callState = VoIPConstant.CALL_STATE_ANSWERED;

                break;
            case VoIPConstant.CALL_STATE_MAKE_FAILED://4
                break;
            case VoIPConstant.CALL_STATE_RECEIVE_CALL_SWITCH://15
                callState = VoIPConstant.CALL_STATE_RECEIVE_CALL_SWITCH;
                break;
            case VoIPConstant.CALL_STATE_SWITCH_RESULT://17
                callState = VoIPConstant.CALL_STATE_SWITCH_RESULT;
                break;
            default:
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventState(EventState msg) {
        MyLogger.getLogger(TAG).i("onEventState meetingInfo ,msg >> " + msg.msg + " , " + msg.code);
        switch (msg.code) {
            case EventState.VOIP_CONFERENCE_RESPONSE_LIST:
                updateVideoDimensions();
                break;
            case EventState.VOIP_CONFERENCE_CLOSED:
                return;
            case EventState.VOIP_CONFERENCE_KICKED:
                if (msg.arg.equals(userName)) {
//                    showToast("你已被主持人移出会议，通话结束");
                } else {
//                    showToast(msg.arg + "已被主持人移出会议");
                }
                return;
            case EventState.VOIP_CONFERENCE_MUTED:
                if (msg.arg.equals("1")) {
                    callMute = true;
//                    btnMute.setChecked(true);
//                    btnMute.setClickable(false);
//                    showToast("您已被主持人禁言");
                } else {
                    callMute = false;
//                    btnMute.setClickable(true);
//                    if (!mUserMute)
//                        btnMute.setChecked(false);
//                    showToast("您已被主持人解除禁言");
                }
                break;
            case EventState.INCOMING_CODEC_CHANGED: // 远端视频长宽
//                MyLogger.getLogger(TAG).i("====> remote w, h : " + msg.width + " " + msg.height);
                MyLogger.getLogger(TAG).i("====> remote w, h : " + msg.channel + " " + msg.width + " " + msg.height);

                if (msg.width != 0 && msg.height != 0) {
                    hwMap.put(msg.channel, msg.width + "x" + msg.height);
                    updateVideoDimensions();
                }

                break;
            case EventState.OUTGOING_CODEC_CHANGED:// 本地视频长宽
                MyLogger.getLogger(TAG).i("====> local w, h : " + msg.width + " " + msg.height);
                if (msg.width != 0 && msg.height != 0) {
                    hwMap.put(-1, msg.width + "x" + msg.height);
                }
                break;
            default:
                break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(Integer state) {
        switch (state) {
            case EVENT_TIME_COUNT://10001
                timeTxt.setText(converCountToTime(mCount++));
                break;
            case EVENT_VIDEO_LOST://20001
                showToast("信号不稳定");
                break;
            default:
                break;
        }

    }

    private void doMakeCallFailed() {
        callState = VoIPConstant.CALL_STATE_MAKE_FAILED;
        //拨打失败
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callState = VoIPConstant.CALL_STATE_RELEASED;
                finishSelf();
            }
        }, 1400);
    }


    public void onMuteConfClick(View view) {
        isMute = !isMute;
        CMVoIPManager.getInstance().setInputMute(isMute);
        btnMute.setImageResource(isMute ? R.mipmap.voice_btn_press_focus : R.mipmap.voice_btn_focus);
        if (isMute) {
            showToast("静音开启");
        } else {
            showToast("静音关闭");
        }
    }
//    /**
//     * 关闭会议,主持人才调用
//     */
//    public void closeMeeting() {
//        CMVoIPManager.getInstance().closeConference(userName + BuildConfig.APP_KEY, accessCode, new VoIP.CallBack() {
//            @Override
//            public void onSuccess() {
//                MyLogger.getLogger(TAG).i("关闭会议成功");
//            }
//
//            @Override
//            public void onFailed(int code, String errorString) {
//                MyLogger.getLogger(TAG).i("关闭会议失败: " + code + " " + errorString);
//            }
//        });
//    }

    /**
     * @param isShow true 来电界面, false 会场界面
     * @author fang wei
     */
    private void showComingView(boolean isShow) {
        comingLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        answerLayout.setVisibility(isShow ? View.GONE : View.VISIBLE);

        //来电播放铃声
        if (isShow) {
            mediaPlayerManager.startInComingMusic(this);
            handlerRing.postDelayed(runnable, 40 * 1000);

        }
//        setAvatarView();

        if (isShow) {
            btnAnswer.setFocusable(true);
            btnAnswer.requestFocus();
        } else {
            btnAnswer.setFocusable(true);
            btnClose.requestFocus();
        }

        if (!isShow) {
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
        }
    }

    private void autoAnswer() {
        if (isAutoAnswer) {
            btnAnswer.setClickable(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onPickupAction();
                    lastClickTime = getCurrentTime();
                    showToast("自动接听");
                    showComingView(false);
                    btnAnswer.setClickable(true);
                }
            }, 3000);
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        //当RecyclerView中无可聚焦的控件时，RecyclerView会自动聚焦，此时设置禁言按钮聚焦
        if (view.getId() == R.id.video_recycler_view) {
            if (b) {
                btnMute.requestFocus();
            }

        }

//        if (b) {
//            ViewCompat.animate(view).scaleX(1.1f).scaleY(1.1f).start();
//        } else {
//            ViewCompat.animate(view).scaleX(1.0f).scaleY(1.0f).start();
//        }
    }

    //丢包率计数
    int count = 0;

    @Override
    public void onUpdateStaticstic(int rttms, int audiobitrate, int audiolostrate, long audioBytesSent, long audioBytesReceived, int videobitrate, int videolostrate, int framerate, long videoBytesSent, long videoBytesReceived) {
        MyLogger.getLogger(TAG).i("videolostrate： " + videolostrate);
//        if (videolostrate > 10) {
//            //连续四次丢包率达到10以上，给予提示
//            count++;
//            if (count == 4) {
//                EventBus.getDefault().post(EVENT_VIDEO_LOST);
//                count = 0;
//            }
//        } else {
//            count = 0;
//        }
    }

    @Override
    public void onUpdateConferenceStaticstic(List<MediaMember> mediaMembers) {
        if (mediaMembers != null) {
            int lostSum = 0;
            for (int i = 0; i < mediaMembers.size(); i++) {
                lostSum = lostSum + mediaMembers.get(i).getVideoLostRate();
            }
            //连续4次总丢包率大于30时，给予提示
            if (lostSum >= 25) {
                count++;
                if (count == 3) {
                    EventBus.getDefault().post(EVENT_VIDEO_LOST);
                    count = 0;
                }
            } else {
                count = 0;
            }
        }
    }

    /**
     * 主持人排序
     */
    private class ComparatorMember implements Comparator<ConferenceMember> {
        @Override
        public int compare(ConferenceMember member1, ConferenceMember member2) {
            String m1 = member1.getUserName();
            int result = 1;
            MyLogger.getLogger(TAG).i("sort member: " + creatorPhone);
            if (m1.equals(creatorPhone)) {
                result = -1;
            }
            return result;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MyLogger.getLogger(TAG).i("onKeyDown keyCode : " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onResponseConferenceData(String conferNum, String creator, String conferName, int isLock, int callType, String time) {
        accessCode = conferNum;
        creatorId = creator;
        creatorPhone = StringUtils.getStringWithOutAppKey(creator);
    }

    @Override
    public void onResponseMemberList(List<MediaMember> mediaMembers) {
        MyLogger.getLogger(TAG).i("====> onResponseMemberList");
        newMembers.clear();
        int memberCount = mediaMembers.size();
        for (int i = 0; i < memberCount; i++) {
            ConferenceMember member = new ConferenceMember();
            member.setUserName(mediaMembers.get(i).getUserName());
            member.setStatus(mediaMembers.get(i).getCallStatus());
            member.setAudioMute(mediaMembers.get(i).getMute());
            member.setCscall(mediaMembers.get(i).getCsStatus());
            member.setUserID(mediaMembers.get(i).getUserID());
            member.setVideoView(mediaMembers.get(i).getVideoView());
            member.setChannel(mediaMembers.get(i).getChannel());
            member.setVideoWidth(0);
            member.setVideoHeight(0);
            MyLogger.getLogger(TAG).i("response member: " + member.toString());
            newMembers.add(member);
        }

        for (int i = 4; i > memberCount; i--) {
            ConferenceMember member = new ConferenceMember();
            member.setUserName(FAKE_USER);
            member.setStatus("6");
            member.setVideoWidth(0);
            member.setVideoHeight(0);
            newMembers.add(member);
            MyLogger.getLogger(TAG).i("添加invite");
        }
        Collections.sort(newMembers, new ComparatorMember());


        EventBus.getDefault().post(new EventState(EventState.VOIP_CONFERENCE_RESPONSE_LIST, null));

    }

    @Override
    public void onResponseError(String errorString) {

    }


    public void onHangUpAction() {
        MyLogger.getLogger(TAG).d("onHangUpAction autoHangup conference :" + autoHangup);
        autoHangup = false;
        CMVoIPManager.getInstance().hangUpCall(callSession);
        callState = VoIPConstant.CALL_STATE_RELEASED;
        EventBus.getDefault().post(new CallState(callState));
        mediaPlayerManager.stop();
        finishSelf();
    }

    public void onHangUpConfClick(View view) {
        MyLogger.getLogger(TAG).d("onHangUpClick: 点击挂断按键");
        if (getCurrentTime() - lastClickTime > MIN_CLICK_DELAY_TIME) {
            onHangUpAction();
        }
    }

    public void onPickUpConfClick(View view) {
        autoHangup = false;
        MyLogger.getLogger(TAG).d("onPickUpClick: 点击接通按键");
        long currentTime = getCurrentTime();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onPickupAction();
            showComingView(false);
        }
    }

    private void onPickupAction() {
        int result = CMVoIPManager.getInstance().pickUpCall(callSession);
        mediaPlayerManager.stop();
        //接听电话，音量键为通话中的音量键
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        if (result != 0) {
            showToast("接听失败");
            finishSelf();
        }
    }

    public void onInviteConfClick(View view) {
        ArrayList<String> selectedList = new ArrayList<>();
        Iterator<ConferenceMember> iterator = conferenceVideoAdapter.getData().iterator();
        while (iterator.hasNext()) {
            ConferenceMember member = iterator.next();
            if (!member.getUserName().equals(ConferenceVideoActivity.FAKE_USER)) {
                selectedList.add(member.getUserName());
            }
        }

        if (selectedList.size() >= 4) {
            showToast("当前通话人员已满4人");
            return;
        }
        ICmccManager.getInstance().actionChoiceContact(ConferenceVideoActivity.this, INVITE_REQUEST_CODE, "333", selectedList, selectedList, 4, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case INVITE_REQUEST_CODE:
                    ArrayList<String> mMembers = data.getStringArrayListExtra(RESULT_SELECTED_LIST);
                    CMVoIPManager.getInstance().inviteConferenceMembers(accessCode, mMembers, new VoIP.CallBack() {
                        @Override
                        public void onSuccess() {
                            showToast("邀请成功");
                        }

                        @Override
                        public void onFailed(int code, String errorString) {
                            showToast(errorString);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private void updateVideoDimensions() {
        for (int i = 0; i < newMembers.size(); i++) {
            if (hwMap.containsKey(newMembers.get(i).getChannel())) {

                MyLogger.getLogger(TAG).i("====> getChannel w, h : " + newMembers.get(i).getChannel()
                        + " " + hwMap.get(newMembers.get(i).getChannel()).split("x")[0]
                        + " " + hwMap.get(newMembers.get(i).getChannel()).split("x")[1]);

                int w = Integer.parseInt(hwMap.get(newMembers.get(i).getChannel()).split("x")[0]);
                int h = Integer.parseInt(hwMap.get(newMembers.get(i).getChannel()).split("x")[1]);
                newMembers.get(i).setVideoWidth(w);
                newMembers.get(i).setVideoHeight(h);
            }
        }

        DiffUtil.DiffResult gridDiffResult = DiffUtil.calculateDiff(new MemberDiffUtilCallback(currentMembers, newMembers), true);
        gridDiffResult.dispatchUpdatesTo(conferenceVideoAdapter);
        conferenceVideoAdapter.setData(newMembers, accessCode);
        currentMembers.clear();
        conferMemberList.clear();
        //克隆对象，保持不被修改
        for (int i = 0; i < newMembers.size(); i++) {
            try {
                currentMembers.add(newMembers.get(i).clone());

                if (!newMembers.get(i).getUserName().equals(ConferenceVideoActivity.FAKE_USER)) {
                    conferMemberList.add(newMembers.get(i).getUserName());
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取没有图片时显示字符串
     *
     * @param name
     * @return
     */
    private String getNameLogo(String name) {
        String nameLogo = "";
        if (name != null && name.length() > 0) {
            if (name.length() == 1) {
                return name;
            } else {
                nameLogo = name.substring(name.length() - 2);
            }
        }
        return nameLogo;
    }

    private void initTimeTask() {
        if (mTimer != null) {//将已存在的线程给销毁掉
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {//开了一个新的线程
            @Override
            public void run() {
                EventBus.getDefault().post(EVENT_TIME_COUNT);
            }
        }, 0, 1 * 1000);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mult_video);
        beforeInitViews();
        initViews();
        initEvents();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void finishSelf() {
        MyLogger.getLogger(TAG).d("finishSelf");
        this.finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
        CMVoIPManager.getInstance().removeConferenceInfoListener();
        CMVoIPManager.getInstance().deregisterMediaStaitcsticCallback();
        CMVoIPManager.getInstance().deregisterVideoCodecObserver();
        handlerRing.removeCallbacks(runnable);

        int callLogType = CallLog.Calls.MISSED_TYPE;
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

//        StringBuffer names = new StringBuffer();
        StringBuffer phones = new StringBuffer();

        CallRecord bean = new CallRecord();
        bean.setCallType(VoIPConstant.CallType.CALLTYPE_CONFERENCE_VIDEO);

        Collections.sort(conferMemberList, new ComparatoString());
        int len = conferMemberList.size();
        for (int i = 0; i < len; i++) {
            String member = conferMemberList.get(i);
//                String name = "";
//                if (member.equals(IAccountManager.getInstance().getAccount().getImsNum())) {
//                    name = "我家的电视";
//                } else {
//                    Contact contact = IContactManager.getInstance().queryContactByPhone(member);
//                    if (contact != null)
//                        name = contact.getName();
//                }
//                if(TextUtils.isEmpty(name)) {
//                    name = member;
//                }
//                names.append(name);
            phones.append(member);

            MyLogger.getLogger(TAG).d("onDestroy-->addCallRecord--> phone=" + member + ", name=");

            if (i != len - 1) {
//                    names.append("，");
                phones.append("，");
            }
        }

        bean.setCall_number(phones.length() == 0 ? creatorPhone : phones.toString());
//        bean.setName(names.length() == 0 ? callShowName : names.toString());

        bean.setDirection(callLogType);
        bean.setStart_time(callStartTime);
        bean.setLast_time(mCount);
        ICmccManager.getInstance().insertCallRecord(bean);
//        EventBus.getDefault().post(new ContactEventState.RecordState(ContactEventState.REFRESH_TV_RECORD, ""));

        IVoipManager.getInstance().CONFERENCE = false;
    }


    /**
     * 号码排序
     */
    private class ComparatoString implements Comparator<String> {
        @Override
        public int compare(String member1, String member2) {
            return member1.compareTo(member2);
        }
    }


    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        int xSpace;
        int ySpace;
        private Drawable mDivider;

        public SpaceItemDecoration(Context context, int xSpace, int ySpace) {
            mDivider = context.getResources().getDrawable(R.drawable.video_line_divider);
            this.xSpace = xSpace;
            this.ySpace = ySpace;
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            drawHorizontal(c, parent);
            drawVertical(c, parent);
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin
                        + mDivider.getIntrinsicWidth();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getTop() - params.topMargin;
                final int bottom = child.getBottom() + params.bottomMargin;
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicWidth();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int childPosition = parent.getChildAdapterPosition(view);
            if (childPosition == 0) {
                outRect.right = xSpace;
                outRect.bottom = ySpace;
            }
            if (childPosition == 1) {
                outRect.bottom = ySpace;
            }
            if (childPosition == 2) {
                outRect.right = xSpace;
            }
        }
    }

    private VoIPCodecObserver voIPCodecObserver = new VoIPCodecObserver() {
        @Override
        public void incomingRate(int session, int channel, int framerate, int bitrate) {

        }

        @Override
        public void DecoderTiming(int decode_ms, int max_decode_ms, int current_delay_ms, int target_delay_ms, int jitter_buffer_ms, int min_playout_delay_ms, int render_delay_ms) {

        }

        @Override
        public void incomingCodecChanged(int session, int channel, int width, int height) {
            EventBus.getDefault().post(new EventState(EventState.INCOMING_CODEC_CHANGED, channel, width, height));
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
            EventBus.getDefault().post(new EventState(EventState.OUTGOING_CODEC_CHANGED, channel, width, height));
        }

        @Override
        public void videoEncodeModeChanged(int session, boolean isOpengl) {

        }
    };

    // TODO: 2017/11/17
    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkStateChanged(WifiState wifiState) {
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

    // TODO: 2017/11/17
    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onDoHangupCall(VoipEventState.DoHangupCall doHangupCall) {
        onHangUpAction();
    }*/
}
