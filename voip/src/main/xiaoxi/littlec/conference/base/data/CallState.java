package littlec.conference.base.data;

public class CallState {
    public int callStateCode;
    public int callType;

    public CallState(int callStateCode) {
        this.callStateCode = callStateCode;
    }
    public CallState(int callStateCode, int callType) {
        this.callStateCode = callStateCode;
        this.callType = callType;
    }
}
