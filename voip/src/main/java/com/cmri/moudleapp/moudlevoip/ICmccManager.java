package com.cmri.moudleapp.moudlevoip;

import android.app.Activity;
import android.content.Context;

import com.cmri.moudleapp.moudlevoip.bean.CallRecord;
import com.cmri.moudleapp.moudlevoip.bean.Contact;
import com.cmri.moudleapp.moudlevoip.manager.IVoipManager;

import java.util.ArrayList;

/**
 * Created by anderson on 2017/11/17.
 */

public class ICmccManager {
    private static ICmccManager instance;

    public static ICmccManager getInstance(){
        if(instance == null){
            instance = new ICmccManager();
        }
        return instance;
    }

    /**
     * 初始化
     * @param context
     * @param useDebugModule    是否调试环境
     */
    public void initCmcc(Context context, boolean useDebugModule){
        IVoipManager.getInstance().initVoip(context, useDebugModule);
    }

    /**d
     * ott登录
     * @param context
     * @param phoneNum
     */
    public void loginCmccOtt(Context context, String phoneNum) {
        IVoipManager.getInstance().loginVoip(context, "", "", 0, "", phoneNum, "", "", 0 , false);
    }

    /**
     *
     * @param context
     * @param token
     * @param domain ims.sd.chinamobile.com
     * @param sbc 211.137.192.165
     * @param iPort 5060
     * @param userName +8653158098616
     * @param imsNum +8653158098616
     * @param authName +8653158098616@ims.sd.chinamobile.com
     * @param password xxxxxxx
     */
    public void loginCmccIms(final Context context, String token, String domain, String sbc, int iPort,
                          String userName, final String imsNum, String authName, String password , boolean wakeUp){
        IVoipManager.getInstance().loginVoip(context, domain, sbc, iPort, userName, imsNum, authName, password, 1 , wakeUp);
    }

    /**
     * 登出
     * @param context
     */
    public void logoutCmcc(Context context){
        IVoipManager.getInstance().logoutVoip(context);
    }

    /**
     * 销毁
     */
    public void destroyCmcc(){
        IVoipManager.getInstance().destroyVoip();
    }

    /**
     * 拨打IMS
     * @param context
     * @param num
     */
    public void actionStartImsAudio(Context context, String num) {
        IVoipManager.getInstance().actionStartImsCallAudio(context, num, num);
    }

    /**
     * 拨打1v1视频
     * @param context
     * @param number
     */
    public void actionStartVideo1V1(Context context, String number){
        IVoipManager.getInstance().actionStartVideo1V1(context, number, number);
    }

    /**
     * 拨打1v1音频
     * @param context
     * @param number
     */
    public void actionStartAudio1V1(Context context, String number) {
        IVoipManager.getInstance().actionStartAudio1V1(context, number, number);

    }

    /**
     * 拨打多方
     * @param context
     * @param members
     */
    public void actionStartVideoConf(Context context, ArrayList<String> members){
        IVoipManager.getInstance().actionStartVideoConf(context, members);
    }

    /**
     * 查询联系人
     * @param phoneNum
     * @return
     */
    public Contact queryContactByPhone(String phoneNum) {
        //TODO:由甲方接入
        return null;
    }

    /**
     * 插入通话记录
     * @param callRecord
     */
    public void insertCallRecord(CallRecord callRecord) {
        //TODO:由甲方接入
    }

    public void actionChoiceContact(Activity activity, int requestCode, String confNum,
                                    ArrayList<String> unEnabledList,
                                    ArrayList<String> checkedList,
                                    int maxSelectNum, int minSelectNum) {
//        Intent intent = new Intent(activity, ChoiceContactActivity.class);
//        intent.putStringArrayListExtra(ChoiceContactActivity.INTENT_UNENABLE_LIST, unEnabledList);
//        intent.putStringArrayListExtra(ChoiceContactActivity.INTENT_CHECKED_LIST, checkedList);
//        intent.putExtra(ChoiceContactActivity.INTENT_CONF_NUMBER, confNum);
//        intent.putExtra(ChoiceContactActivity.INTENT_MAX_SELECTNUM, maxSelectNum);
//        intent.putExtra(ChoiceContactActivity.INTENT_MIN_SELECTNUM, minSelectNum);
//        activity.startActivityForResult(intent, requestCode);
    }
}
