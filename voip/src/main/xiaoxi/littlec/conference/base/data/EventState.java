package littlec.conference.base.data;

/**
 * Created by caizhibiao on 2016/4/21.
 */
public class EventState {
    public static final int LOGIN_SUCCESS = 90000;
    public static final int LOGIN_TIME_OUT = LOGIN_SUCCESS + 1;
    public static final int LOGIN_NOT_AUTHORIZED = LOGIN_TIME_OUT + 1;
    public static final int LOGIN_FAILED = LOGIN_NOT_AUTHORIZED + 1;
    //    public static final int login_success=90001;
//    public static final int CONNECT_RECONNECTED = CONNECT_DISCONNECTED + 1;
//    public static final int CONNECT_DISCONNECTED = CONNECT_ACCOUNT_DESTROYED + 1;
//    public static final int CONNECT_ACCOUNT_DESTROYED = CONNECT_ACCOUNT_CONFLICT + 1;
//    public static final int CONNECT_ACCOUNT_CONFLICT = LOGIN_FAILED + 1;
//    //连接状况
    public static final int VOIP_CONFERENCE_CLOSED = LOGIN_FAILED + 1;
    public static final int VOIP_CONFERENCE_KICKED = VOIP_CONFERENCE_CLOSED + 1;
    public static final int VOIP_CONFERENCE_UPDATED = VOIP_CONFERENCE_KICKED + 1;
    public static final int VOIP_CONFERENCE_MUTED = VOIP_CONFERENCE_UPDATED + 1;
    public static final int VOIP_CONFERENCE_SSRC_CHANGED = VOIP_CONFERENCE_MUTED + 1;
    public static final int LOAD_SYSTEM_SUCCESS=VOIP_CONFERENCE_SSRC_CHANGED+1;

    public static final int NICK_SUCCESS=LOAD_SYSTEM_SUCCESS+1;
    public static final int NEW_MEETING=NICK_SUCCESS+1;
    public static final int CALL_IDLE=NEW_MEETING+1;
    public static final int CALL_RING=CALL_IDLE+1;
    public static final int SYNC_MEETING=CALL_RING+1;

    public static final int INCOMING_CODEC_CHANGED = SYNC_MEETING+1; // 底层返回远端视频长宽
    public static final int OUTGOING_CODEC_CHANGED = INCOMING_CODEC_CHANGED+1;

    public static final int VOIP_CONFERENCE_RESPONSE_LIST = OUTGOING_CODEC_CHANGED + 1;

    //本地远端都黑屏，openGL创建失败，发送些消息重新加载新的view
    public static final int VOIP_ENCODE_MODE_CHANGED = VOIP_CONFERENCE_RESPONSE_LIST + 1;

    public int code;
    public String msg;
    public String arg;
    public int channel;
    public int width;
    public int height;

    public EventState(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public EventState(int code, String msg, String arg) {
        this.code = code;
        this.msg = msg;
        this.arg = arg;
    }


    public EventState(int code, int channel, int width, int height){
        this.code = code;
        this.channel = channel;
        this.width = width;
        this.height= height;
    }
}
