package littlec.conference.base.data;

/**
 * Created by zhenwu on 2017/8/23.
 */

//refreshContact success
public class ContactEventState {

    public static final int REFRESH_CONTACT_SUCCESS = 1;        //获取好友信息成功
    public static final int REFRESH_CONTACT_FAILED = 2;         //获取好友信息失败
    public static final int REFRESH_TV_OWNER_SUCCESS = 3;       //获取绑定者信息成功
    public static final int REFRESH_TV_OWNER_FAILED  = 4;       //获取绑定者信息失败
    public static final int REFRESH_TV_HAS_OWNER  = 5;          //有绑定者信息
    public static final int REFRESH_TV_NO_OWNER  = 6;           //无绑定者信息
    public static final int REFRESH_TV_RECORD  = 7;             //刷新通话记录
    public static final int REFRESH_TV_OFFLINE_RECORD  = 8;     //刷新离线通话记录
    public static final int REFRESH_HAS_CONTACT= 9;             //有好友信息
    public static final int REFRESH_NO_CONTACT = 10;            //无好友信息
    public static final int REFRESH_TV_OWNER_PROCESS=11;        //正在获取绑定者信息

    public static class TVOwnerState{
        public int code;
        public String error;
        public TVOwnerState(int code , String error) {
            this.code = code;
            this.error = error;
        }
    }

    public static class ContactState{
        public int code;
        public String error;
        public ContactState(int code , String error) {
            this.code = code;
            this.error = error;
        }
    }

    public static class RecordState{
        public int code;
        public String error;
        public RecordState(int code , String error) {
            this.code = code;
            this.error = error;
        }
    }

}
