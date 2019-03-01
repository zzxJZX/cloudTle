package littlec.conference.talk.model;

import android.os.Parcel;
import android.view.SurfaceView;

import com.mobile.voip.sdk.model.Member;

/**
 * Created by caizhibiao on 2016/5/15.
 */
public class ConferenceMember extends Member implements Cloneable {
    //界面显示，先默认都不在线
    //主持人，禁言（在线），已挂断（在线），未登录（不在线）
    //进行操作 --》 禁言，呼叫，短信邀请
    /**
     * 成员状态，4-10均为离线。
     * 1	E_CALL_INVITE,   呼叫中
     * 2	E_CALL_RING,    振铃中
     * 3	E_CALL_ANSWER,  应答、在线
     * 4	E_CALL_FAIL,      呼叫失败
     * 5	E_CALL_USER_BUSY,	被叫忙
     * 6	E_CALL_404_NOUSER,  未注册
     * 7	E_CALL_NO_ANSWER,  无应答
     * 8	E_CALL_NOT_LOGIN,   未登录
     * 9	E_CALL_REJECTED,     拒接
     * 10	E_CALL_HANGUP,     挂机、离线
     */
    private String status_;
    /**
     * 1 禁言，0发言状态。
     */
    private int audio_mute_;

    private int video_mute_;

    private int cs_call_;

    private String audio_ssrc_;

    private String video_ssrc_;

    private String userID;

    private SurfaceView videoView;

    private int videoWidth;

    private int videoHeight;

    private int channel = -100;

    public ConferenceMember() {
    }

    public ConferenceMember(String phone) {
        setUserName(phone);
    }

    public ConferenceMember(String phone, String status, int audio_mute, int video_mute, int cs, String audiossrc, String videossrc) {
        super();
        setUserName(phone);
        this.status_ = status;
        this.audio_mute_ = audio_mute;
        this.video_mute_ = video_mute;
        this.audio_ssrc_ = audiossrc;
        this.video_ssrc_ = videossrc;
        this.cs_call_ = cs;
    }

    public ConferenceMember(String phone, String status, int audio_mute, int video_mute, int cs, String audiossrc, String videossrc, String userID) {
        super();
        setUserName(phone);
        this.status_ = status;
        this.audio_mute_ = audio_mute;
        this.video_mute_ = video_mute;
        this.audio_ssrc_ = audiossrc;
        this.video_ssrc_ = videossrc;
        this.cs_call_ = cs;
        this.userID = userID;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public SurfaceView getVideoView() {
        return videoView;
    }

    public void setVideoView(SurfaceView videoView) {
        this.videoView = videoView;
    }

    public String getStatus() {
        return status_;
    }

    public void setStatus(String status) {
        this.status_ = status;
    }

    public int getAudioMute() {
        return audio_mute_;
    }

    public void setAudioMute(int mute) {
        this.audio_mute_ = mute;
    }

    public int getVideoMute() {
        return video_mute_;
    }

    public void setVideoMute(int mute) {
        this.video_mute_ = mute;
    }

    public void setCscall(int cscall) {
        this.cs_call_ = cscall;
    }

    public int getCscall() {
        return cs_call_;
    }

    public void setAudioSSRC(String ssrc) {
        audio_ssrc_ = ssrc;
    }

    public String getAudioSSRC() {
        return video_ssrc_;
    }

    public void setVideoSSRC(String ssrc) {
        video_ssrc_ = ssrc;
    }

    public String getVideoSSRC() {
        return video_ssrc_;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public ConferenceMember clone() throws CloneNotSupportedException {
        return (ConferenceMember) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(status_);
        parcel.writeInt(audio_mute_);
    }

    public static final Creator<ConferenceMember> CREATOR = new Creator<ConferenceMember>() {
        @Override
        public ConferenceMember createFromParcel(Parcel parcel) {
            ConferenceMember conferenceMember = new ConferenceMember();
            conferenceMember.setUserName(parcel.readString());
            conferenceMember.setMemberId(parcel.readString());
            conferenceMember.setJoined(parcel.readByte() != 0);
            conferenceMember.setCreator(parcel.readByte() != 0);
            conferenceMember.setStatus(parcel.readString());
            conferenceMember.setAudioMute(parcel.readInt());
  /*         conferenceMember.setVideoMute(parcel.readInt());
            conferenceMember.setCSCall(parcel.readInt());
             conferenceMember.setAudioSSRC(parcel.readString());
            conferenceMember.setVideoSSRC(parcel.readString());*/
            return conferenceMember;
        }

        @Override
        public ConferenceMember[] newArray(int i) {
            return new ConferenceMember[i];
        }
    };


    @Override
    public String toString() {
        return "ConferenceMember{" +
                "status_='" + status_ + '\'' +
                ", audio_mute_=" + audio_mute_ +
                ", video_mute_=" + video_mute_ +
                ", cs_call_=" + cs_call_ +
                ", audio_ssrc_='" + audio_ssrc_ + '\'' +
                ", video_ssrc_='" + video_ssrc_ + '\'' +
                ", userID='" + userID + '\'' +
                ", videoView=" + videoView +
                ", videoWidth=" + videoWidth +
                ", videoHeight=" + videoHeight +
                ", channel=" + channel +
                '}';
    }
}
