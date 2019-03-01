package littlec.conference.base.data;

/**
 * Created by zhangcong on 2017/9/6.
 */

public class VoipEventState {

    public static final int CAMERA_REFRESH_START = 0;
    public static final int CAMERA_REFRESH_END = 1;
    public static final int CAMERA_EXIST = 2;
    public static final int CAMERA_NO_EXIST = 3;

    public static class CameraRefreshState{
        public int state;

        public CameraRefreshState(int state) {
            this.state = state;
        }
    }

    public static class DoHangupCall{

        public DoHangupCall() {
        }
    }

}
