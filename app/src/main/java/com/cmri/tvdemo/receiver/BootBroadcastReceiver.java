package com.cmri.tvdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cmri.tvdemo.SmartInteractive;
import com.cmri.tvdemo.TvApp;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2018/3/20.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BootBroadcastReceiver";

    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Boot this system , BootBroadcastReceiver onReceive()");


        if (intent.getAction().equals(ACTION_BOOT)) {
            EventBus.getDefault().post("fff");
        }
    }
}
