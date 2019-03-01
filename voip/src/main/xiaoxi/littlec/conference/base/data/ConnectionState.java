package littlec.conference.base.data;

/**
 * Created by caizhibiao on 2016/5/5.
 */
public class ConnectionState {
    //连接状况
    public static final int CONNECT_ACCOUNT_CONFLICT = 8000 + 1;
    public static final int CONNECT_ACCOUNT_DESTROYED = CONNECT_ACCOUNT_CONFLICT + 1;
    public static final int CONNECT_DISCONNECTED = CONNECT_ACCOUNT_DESTROYED + 1;
    public static final int CONNECT_SUCCEED = CONNECT_DISCONNECTED + 1;
    public static final int LOGIN_SUCCEED = CONNECT_SUCCEED + 1;
    public static final int LOGIN_FAILED = LOGIN_SUCCEED + 1;

    // 账号冲突标志位：如果冲突，release消息不再发，避免release后 界面finish，造成的dialog show错误
    public static final String CONNECT_ACCOUNT_CONFLICT_FLAG = "connectAccountConflictFlag";


    public int code;

    public ConnectionState(int code) {
        this.code = code;
    }
}
