package littlec.conference.talk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.mobile.voip.sdk.callback.VoIP;

import java.util.HashMap;
import java.util.List;

/**
 * Created by caizhibiao on 2016/5/10.
 */
public class ConferenceData implements Parcelable {
    public final static int CALL_STATE_MAKE_FAILED_NOT_EXIT = 80;//此会议不存在
    public final static int CALL_STATE_MAKE_FAILED_NOT_AUTHORITY = 81;//您没有加入此会议的权限
    public final static int CALL_STATE_MAKE_FAILED_TIME_OUT = 82;//断网超时挂断


    private String confName;//会议主题
    private String confNumber;//会议号
    private String createPhone;//发起者
    private String time;//发起时间
    private int islock;//会场锁定状态，0未锁定，1锁定
    private int callType;
    private String confType;
    private List<ConferenceMember> conferMemberList;
    private HashMap<String,String> mMap;


    private String confPw;
    private String confid;//会议唯一id


    public ConferenceData(String confName, String confNumber, String createPhone, String time, int islock, int type) {
        this.confName = confName;
        this.confNumber = confNumber;
        this.createPhone = createPhone;
        this.time = time;
        this.islock = islock;
        if(1 ==  type){
            callType = VoIP.CallType.toInt(VoIP.CallType.CALLTYPE_CONFERENCE_VIDEO);
        }else{
            callType = VoIP.CallType.toInt(VoIP.CallType.CALLTYPE_CONFERENCE_AUDIO);
        }
    }

    public ConferenceData(){

    }

    public void setConfType(String confType) {
        this.confType = confType;
    }
    public String getConfType() {
        return confType;

    }

    public List<ConferenceMember> getConferMemberList() {
        return conferMemberList;
    }

    public void setConferMemberList(List<ConferenceMember> conferMemberList) {
        this.conferMemberList = conferMemberList;
    }

    public void setConferMemberSsrc(HashMap<String,String> map){
        this.mMap = map;
    }
    public HashMap<String,String> getConferMemberSsrc(){
        return mMap;
    }

    public void setConfName(String confName) {
        this.confName = confName;
    }

    public String getConfName() {
        return confName;
    }

    public String getConfNumber() {
        return confNumber;
    }

    public void setConfNumber(String confNumber) {
        this.confNumber = confNumber;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setIslock(int islock) {
        this.islock = islock;
    }

    public String getConfid() {
        return confid;
    }

    public void setConfid(String confid) {
        this.confid = confid;
    }

    public void setConfPw(String confPw) {
        this.confPw = confPw;
    }

    public String getConfPw() {
        return confPw;
    }

    public void setCreatePhone(String createPhone) {
        this.createPhone = createPhone;
    }

    public String getCreatePhone() {
        return createPhone;
    }

    public void setConfId(String confid) {
        this.confid = confid;
    }

    public String getConfId() {
        return confid;
    }

    public void setCallType(int type){
        this.callType = type;
    }
    public int getCallType(){
        return callType;
    }

    public void setIsLock(int isLock) {
        this.islock = isLock;
    }

    public int getIslock() {
        return islock;
    }


    protected ConferenceData(Parcel in) {
        this.confName = in.readString();
        this.confNumber = in.readString();
        this.confPw = in.readString();
        this.createPhone = in.readString();
        this.confid = in.readString();
        this.time = in.readString();
        this.islock = in.readInt();
        this.callType = in.readInt();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeString(confName);
        dest.writeString(confNumber);
        dest.writeString(confPw);
        dest.writeString(createPhone);
        dest.writeString(confid);
        dest.writeString(time);
        dest.writeInt(islock);
        dest.writeInt(callType);
    }

    public static final Creator<ConferenceData> CREATOR = new Creator<ConferenceData>() {
        public ConferenceData createFromParcel(Parcel in) {
            return new ConferenceData(in);
        }

        public ConferenceData[] newArray(int size) {
            return new ConferenceData[size];
        }
    };
}
