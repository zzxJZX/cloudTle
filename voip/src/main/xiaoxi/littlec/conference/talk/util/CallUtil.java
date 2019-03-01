package littlec.conference.talk.util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;

import com.cmri.moudleapp.moudlevoip.manager.IVoipManager;
import com.mobile.voip.sdk.api.utils.MyLogger;

import java.util.ArrayList;

import littlec.conference.talk.activity.BaseCallActivity;
import littlec.conference.talk.activity.CallVideoActivity;
import littlec.conference.talk.activity.ConferenceVideoActivity;

/**
 * Created by caizhibiao on 2016/4/20.
 */
public class CallUtil {
    private static final MyLogger sLogger = MyLogger.getLogger("CallUtil");


    public static void jump2CallPage(final Service context, final int session, final int callType, final boolean isComing,
                                     final int callstate, final String callNumber, final String showName,
                                     final boolean onGoing, final boolean mute, final boolean speakon, final int count, final boolean isAutoAnswer) {

        MyLogger.getLogger("CallUtil").e("makeVideoCall ooo getSimpleName CALLVIDEO ：" + IVoipManager.getInstance().CALLVIDEO+", CONFERENCE:"+IVoipManager.getInstance().CONFERENCE);
        if(IVoipManager.getInstance().CALLVIDEO || IVoipManager.getInstance().CONFERENCE){

            MyLogger.getLogger("CallUtil").e("makeVideoCall ooo");

            new Handler(context.getMainLooper()).postDelayed(new Runnable(){
                public void run() {
                    //execute the task
                    jump2CallPage(context,session,callType,isComing,callstate,callNumber,showName,onGoing,mute,speakon,count,isAutoAnswer);
                }
            }, 500);

            return;
        }

        Intent intent = new Intent(context, CallVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra(BaseCallActivity.Key.CALL_TYPE, callType);
        intent.putExtra(BaseCallActivity.Key.CALL_INCOMING, isComing);
        intent.putExtra(BaseCallActivity.Key.CALL_STATE, callstate);
        intent.putExtra(BaseCallActivity.Key.CALL_NUMBER, callNumber);
        intent.putExtra(BaseCallActivity.Key.CALL_SHOW_NAME, showName);
        intent.putExtra(BaseCallActivity.Key.CALL_SPEAKER_ON, speakon);
        intent.putExtra(BaseCallActivity.Key.CALL_SESSION, session);
        intent.putExtra(BaseCallActivity.Key.CALL_ONGOING, onGoing);
        intent.putExtra(BaseCallActivity.Key.CALL_MUTE, mute);
        intent.putExtra(BaseCallActivity.Key.CALL_COUNT, count);
//        intent.putExtra(BaseCallActivity.Key.IS_AUTO_ANSWER, isAutoAnswer);
        context.startActivity(intent);

    }

    public static void jump2ConferencePage(final Service context, final int session, final int callType, final boolean isComing, final int callstate, final String phoneNumber, final String showName, final boolean isCreate, final boolean isAutoAnswer, final ArrayList<String> con_member) {
//        Context mContext = AppManager.getAppManager().currentActivity();
//        if (mContext == null) {
//            mContext = context;
//        }

        MyLogger.getLogger("CallUtil").e("makeConferenceCall ooo getSimpleName CALLVIDEO ：" + IVoipManager.getInstance().CALLVIDEO+", CONFERENCE:"+IVoipManager.getInstance().CONFERENCE);
        if(IVoipManager.getInstance().CALLVIDEO || IVoipManager.getInstance().CONFERENCE){

            MyLogger.getLogger("CallUtil").e("makeConferenceCall ooo");
            new Handler(context.getMainLooper()).postDelayed(new Runnable(){
                public void run() {
                    //execute the task
                    jump2ConferencePage(context,session,callType,isComing,callstate,phoneNumber,showName,isCreate,isAutoAnswer,con_member);
                }
            }, 500);

            return;
        }

        if (callType == 3) {
            Intent intent = new Intent(context, ConferenceVideoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(BaseCallActivity.Key.CALL_TYPE, callType);
            intent.putExtra(BaseCallActivity.Key.CALL_INCOMING, isComing);
            intent.putExtra(BaseCallActivity.Key.CALL_STATE, callstate);
            intent.putExtra(BaseCallActivity.Key.CALL_NUMBER, phoneNumber);
            intent.putExtra(BaseCallActivity.Key.CALL_SHOW_NAME, showName);
            intent.putExtra(BaseCallActivity.Key.CALL_SPEAKER_ON, false);
            intent.putExtra(BaseCallActivity.Key.CALL_SESSION, session);
            intent.putExtra(BaseCallActivity.Key.CALL_ONGOING, false);
            intent.putExtra(BaseCallActivity.Key.CALL_MUTE, false);
            intent.putExtra(BaseCallActivity.Key.CALL_COUNT, 0);
            intent.putExtra(BaseCallActivity.Key.CALL_CREATE, isCreate);//是否是主动创建会议
//            intent.putExtra(BaseCallActivity.Key.IS_AUTO_ANSWER, isAutoAnswer);
            intent.putStringArrayListExtra(BaseCallActivity.Key.CALL_MEMBERS, con_member);

            context.startActivity(intent);
        }
    }
}
