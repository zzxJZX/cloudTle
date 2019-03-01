package littlec.conference.base.data;

import java.util.ArrayList;

/**
 * Created by cmcc on 16/5/8.
 */
public class BackGroundCallState {
//    public static final int CALL_CALLING = VoIPConstant.;
//    public static final int CONNECT_ACCOUNT_DESTROYED = CONNECT_ACCOUNT_CONFLICT + 1;
//    public static final int CONNECT_DISCONNECTED = CONNECT_ACCOUNT_DESTROYED + 1;
//    public static final int CONNECT_RECONNECTED = CONNECT_DISCONNECTED + 1;


    public int code;
    public int count;
    public boolean incoming;
    public int callType;
    public String callShowName;
    public String callNumber;
    public String pushFrom;
    public String pushMsgTag;
    public boolean callSpeakerOn;
    public boolean callMute;
    public int callSession;
    public int callSpecialType;
    public boolean callBeautyMode;
    public int callVoiceMode;
    public ArrayList<String> con_member;
    public boolean auto;//是否是代码里自动进入到通话界面的，非人为点击操作

    public BackGroundCallState(int code) {
        this.code = code;
    }

}
