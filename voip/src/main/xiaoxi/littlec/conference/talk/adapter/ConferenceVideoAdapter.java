package littlec.conference.talk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmri.moudleapp.moudlevoip.ICmccManager;
import com.cmri.moudleapp.moudlevoip.R;
import com.cmri.moudleapp.moudlevoip.bean.Contact;
import com.cmri.moudleapp.moudlevoip.manager.IVoipManager;
import com.cmri.moudleapp.moudlevoip.view.ContactHeadLayout;
import com.cmri.moudleapp.moudlevoip.view.TextViewLoadingAnimator;
import com.mobile.voip.sdk.api.CMVoIPManager;
import com.mobile.voip.sdk.api.utils.MyLogger;
import com.mobile.voip.sdk.callback.VoIP;

import java.util.ArrayList;
import java.util.List;

import littlec.conference.talk.activity.ConferenceVideoActivity;
import littlec.conference.talk.model.ConferenceMember;

/**
 * @author: fang wei
 * @data: 2017/3/6
 * @Description: <多人视频适配器>
 */

public class ConferenceVideoAdapter extends RecyclerView.Adapter<ConferenceVideoAdapter.MemberViewHolder>{
    private static final String TAG = ConferenceVideoAdapter.class.getSimpleName();
    private String creatorPhone;
    private List<ConferenceMember> members;
    private int screenHeight;
    private int screenWidth;

    private int videoHeight;
    private int videoWidth;

    private Context mContext;
    private OnItemClickListener mOnItemClickListener = null;

    private TextView cameraErrorText;

    public interface OnItemClickListener {
        void onItemClick(View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    public ConferenceVideoAdapter(Context mContext, String creatorPhone, List<ConferenceMember> members, int screenWidth, int screenHeight) {
        this.mContext = mContext;
        this.creatorPhone = creatorPhone;
        this.members = members;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void setData(List<ConferenceMember> members, String conferNum) {
        this.members.clear();
        this.members.addAll(members);
//        this.members = members;
        this.creatorPhone = conferNum;
    }

    public List<ConferenceMember> getData() {
        return this.members;
    }
    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conference_video, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Bundle bundle = (Bundle) payloads.get(0);
            //更新状态
            if (bundle.containsKey("status")) {
                MyLogger.getLogger(TAG).i("payloads member: " + members.get(position).getUserName());
                setInviteUI(members.get(position), holder);
            }
        }
    }

    @Override
    public void onBindViewHolder(final MemberViewHolder holder, int position) {
            MyLogger.getLogger(TAG).i("MemberViewHolder: position " + position );
            ConferenceMember member = members.get(position);
            RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp1.height = videoHeight = screenHeight / 2;
            lp1.width = videoWidth = screenWidth / 2;
            holder.layoutVideoParent.setLayoutParams(lp1);
            holder.reInviteLayout.setLayoutParams(lp1);

            int result = setInviteUI(member, holder);
            if (result == 1){
                return;
            }


            boolean isLocalUser = member.getUserName().equals(IVoipManager.getInstance().getAccount().getImsNum());

            if (member.getVideoWidth() == 0 || member.getVideoHeight() == 0 || !member.getStatus().equals("3")) {
//                memberViewHolder.reInviteLayout.setVisibility(View.VISIBLE);
            } else {
                if (!isLocalUser) {
                    float dataBack = (float) member.getVideoHeight() / member.getVideoWidth();
                    float dataLocal = (float) videoHeight / videoWidth;
                    if (dataBack < dataLocal) {
                        videoHeight = videoWidth * member.getVideoHeight() / member.getVideoWidth();
                    } else {
                        videoWidth = videoHeight * member.getVideoWidth() / member.getVideoHeight();
                    }
                }
                MyLogger.getLogger(TAG).i("~~~~~~~~~: " + member.getUserName() + " 加载宽高： " + videoWidth + " " + videoHeight);
                RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp2.width = videoWidth;
                lp2.height = videoHeight;
                lp2.addRule(RelativeLayout.CENTER_IN_PARENT);
                holder.layoutVideo.setLayoutParams(lp2);

                if (member.getVideoView() != null) {
                    View view;
                    if (CMVoIPManager.getInstance().getCameraStatus(0) != 0 && isLocalUser) {
                        if (cameraErrorText == null) {
                            cameraErrorText = new TextView(mContext);
                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            cameraErrorText.setLayoutParams(layoutParams);
                            cameraErrorText.setBackgroundColor(mContext.getResources().getColor(R.color.black));
                            cameraErrorText.setGravity(Gravity.CENTER);
                            cameraErrorText.setTextColor(mContext.getResources().getColor(R.color.side_text_color));
                            cameraErrorText.setTextSize(20);
                            cameraErrorText.setText(R.string.camera_error_tip);
                        }
                        view = cameraErrorText;
                    } else {
                        view = member.getVideoView();
                    }
                    holder.layoutVideoParent.setVisibility(View.VISIBLE);
                    holder.layoutVideoParent.setBackgroundColor(Color.BLACK);
//                    holder.loadingLayout.setVisibility(View.GONE);
                    ViewGroup parent = (ViewGroup) view.getParent();
                    if (parent != null) {
                        parent.removeAllViews();
                    }
//                    memberViewHolder.layoutVideo.removeAllViews();
                    holder.layoutVideo.addView(view);
//                    member.getVideoView().setZOrderOnTop(true);
//                    member.getVideoView().setZOrderMediaOverlay(true);
                    MyLogger.getLogger(TAG).i("====>addView normal " + member.getUserName());
                }
            }

    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    /**
     * 正常成员holder
     */
    public static class MemberViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout layoutVideoParent;
        RelativeLayout layoutVideo;
//        LinearLayout loadingLayout;
//        TextView loadingText;
        RelativeLayout reInviteLayout;
//        ImageView memberAvatar;
//        TextView memberName;
        TextView tvMemberStatus;
        TextView tvInviteTip;
        TextViewLoadingAnimator tvNumberAnimator;

        RelativeLayout emptyBackground;
        ContactHeadLayout mContactHeadLayout;
//        RelativeLayout mContactReInviteLayout;
//        ImageView inviteBtn;

        MemberViewHolder(View itemView) {
            super(itemView);
            layoutVideoParent = itemView.findViewById(R.id.layout_video_parent);
            layoutVideo = itemView.findViewById(R.id.layout_video);
//            loadingLayout = itemView.findViewById(R.id.loading_layout);
//            loadingText =  itemView.findViewById(R.id.loading_text);
            reInviteLayout = itemView.findViewById(R.id.re_invite_layout);
//            memberAvatar = itemView.findViewById(R.id.member_avatar);
//            memberName = itemView.findViewById(R.id.member_name);
            mContactHeadLayout = itemView.findViewById(R.id.member_avatar);

            tvNumberAnimator=itemView.findViewById(R.id.name_tv);
//            mContactReInviteLayout = itemView.findViewById(R.id.member_avatar_layout);

            tvMemberStatus = itemView.findViewById(R.id.member_status_text);
            emptyBackground = itemView.findViewById(R.id.empty_layout);
            tvInviteTip = itemView.findViewById(R.id.re_invite_tip);
        }
        public TextViewLoadingAnimator getTvNumber(){
            return tvNumberAnimator;
        }
    }

    private String updateState(String sta) {
        switch (sta) {
            case "1":
            case "2":
                return "呼叫中";
            case "3":
                return "通话中";
            case "4":
            case "5":
            case "7":
                return "未接听";
            case "6":
                return "未注册";
            case "8":
                return "未登录";
            case "9":
            case "10":
                return "已挂断";
            default:
                return "";
        }
    }

    private int setInviteUI(ConferenceMember member, MemberViewHolder holder) {
        //空数据时，显示空背景
        if (member.getUserName().equals(ConferenceVideoActivity.FAKE_USER)) {
            holder.reInviteLayout.setVisibility(View.GONE);
            holder.emptyBackground.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp1.height = screenHeight / 2;
            lp1.width = screenWidth / 2;
            holder.emptyBackground.setLayoutParams(lp1);
            return 1;
        }

        holder.emptyBackground.setVisibility(View.GONE);
        String status = updateState(member.getStatus());
        if (("通话中").equals(status)) {
            holder.reInviteLayout.setVisibility(View.INVISIBLE);
            MyLogger.getLogger(TAG).i("通话中隐藏头像重呼");
            return 0;   //不显示UI
        }

        final String memberNum = member.getUserName();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(0, 0);
        holder.layoutVideo.setLayoutParams(layoutParams);
        holder.layoutVideo.removeAllViews();
        holder.reInviteLayout.setVisibility(View.VISIBLE);
        holder.tvInviteTip.setVisibility(View.INVISIBLE);
        holder.tvMemberStatus.setVisibility(View.INVISIBLE);
        String memberName = "memberName";
        String photoUrl = "";
        setContactLayoutInfo(member.getUserName(), holder.mContactHeadLayout);

        if (("未接听").equals(status) || ("已挂断").equals(status) || ("未登录").equals(status)) {

            MyLogger.getLogger(TAG).i("~~~~~~~~~: " + "未接听 已挂断");
            holder.mContactHeadLayout.setFocusable(true);
//            holder.loadingLayout.setVisibility(View.GONE);
            holder.tvInviteTip.setVisibility(View.VISIBLE);
            holder.tvMemberStatus.setVisibility(View.VISIBLE);
            holder.tvMemberStatus.setText("对方" + status);
            holder.mContactHeadLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> memberList = new ArrayList<>();
                    memberList.add(memberNum);
//                    CMVoIPManager.getInstance().inviteConferenceMembers("057110000010", memberList, new VoIP.CallBack() {
                    CMVoIPManager.getInstance().inviteConferenceMembers(creatorPhone, memberList, new VoIP.CallBack() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(mContext, "重呼成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed(int code, String errorString) {
                            Toast.makeText(mContext, "重呼失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            holder.getTvNumber().setAnimator(false);

        } else {
            holder.mContactHeadLayout.setFocusable(false);
            holder.getTvNumber().setAnimator(true);
            holder.getTvNumber().startAnimation();
//            holder.loadingLayout.setVisibility(View.VISIBLE);
//            holder.reInviteBtn.setVisibility(View.GONE);
//            holder.memberStatusText.setVisibility(View.GONE);
//            holder.loadingText.setText("正在接通( " + status + " )");
            MyLogger.getLogger(TAG).i("显示正在接通...");

        }

        return 1;   //显示UI

    }

    private void setContactLayoutInfo(String phone, ContactHeadLayout layout ) {
        layout.setVisibility(View.VISIBLE);
        Contact contact = ICmccManager.getInstance().queryContactByPhone(phone);
        String avatarUrl,nick;
        if(contact!=null){
            avatarUrl = contact.getAvatar();
            nick = contact.getName();
        } else {
            avatarUrl = "";
            nick = phone;
        }
        layout.setContactInfo(avatarUrl, nick, phone);
    }





}
