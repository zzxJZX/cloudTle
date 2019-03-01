package com.cmri.tvdemo;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.provider.Settings;
import android.widget.Toast;

import com.cmri.moudleapp.moudlevoip.ICmccManager;

public class ImsCall1Activity extends Activity implements OnClickListener {
    private static String TAG = "MainActivity";
    TextView tv;
    Button but0;
    ImageButton but1;
    ImageButton but2;
    ImageButton but3;
    ImageButton but4;
    ImageButton but5;
    ImageButton but6;
    ImageButton but7;
    ImageButton but8;
    ImageButton but9;
    ImageButton but10;
    ImageButton but11;
    ImageButton but12;
    ImageButton but13;
    ImageButton but14;
    ImageButton but15;
    private static final int DTMF_DURATION_MS = 120; // 声音的播放时间
    private Object mToneGeneratorLock = new Object(); // 监视器对象锁
    private ToneGenerator mToneGenerator;             // 声音产生器
    private static boolean mDTMFToneEnabled;         // 系统参数“按键操作音”标志位
    private Handler handler = new Handler();

    private boolean login = false;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("panhouye")) {
                login = true;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        //按键声音播放设置及初始化
        try {
            // 获取系统参数“按键操作音”是否开启
            mDTMFToneEnabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
            synchronized (mToneGeneratorLock) {
                if (mDTMFToneEnabled && mToneGenerator == null) {
                    mToneGenerator = new ToneGenerator(
                            AudioManager.STREAM_DTMF, 80); // 设置声音的大小
                    setVolumeControlStream(AudioManager.STREAM_DTMF);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            mDTMFToneEnabled = false;
            mToneGenerator = null;
        }

        tv = (TextView) findViewById(R.id.tv);
//		but0 = (Button)findViewById(R.id.but0);
        but1 = (ImageButton) findViewById(R.id.but1);
        but2 = (ImageButton) findViewById(R.id.but2);
        but3 = (ImageButton) findViewById(R.id.but3);
        but4 = (ImageButton) findViewById(R.id.but4);
        but5 = (ImageButton) findViewById(R.id.but5);
        but6 = (ImageButton) findViewById(R.id.but6);
        but7 = (ImageButton) findViewById(R.id.but7);
        but8 = (ImageButton) findViewById(R.id.but8);
        but9 = (ImageButton) findViewById(R.id.but9);
        but10 = (ImageButton) findViewById(R.id.but10);
        but11 = (ImageButton) findViewById(R.id.but11);
        but12 = (ImageButton) findViewById(R.id.but12);
        but13 = (ImageButton) findViewById(R.id.but13);
        but14 = (ImageButton) findViewById(R.id.but14);
        but15 = (ImageButton) findViewById(R.id.but15);
//		but0.setOnClickListener(this);
        but1.setOnClickListener(this);
        but2.setOnClickListener(this);
        but3.setOnClickListener(this);
        but4.setOnClickListener(this);
        but5.setOnClickListener(this);
        but6.setOnClickListener(this);
        but7.setOnClickListener(this);
        but8.setOnClickListener(this);
        but9.setOnClickListener(this);
        but10.setOnClickListener(this);
        but11.setOnClickListener(this);
        but12.setOnClickListener(this);
        but13.setOnClickListener(this);
        but14.setOnClickListener(this);
        but15.setOnClickListener(this);
        //设置长按删除键，触发删除全部
        but15.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                tv.setText("");
                return false;
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println(but14.getWidth() + "but14width");
            }
        }, 3500);
        tv.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                //文本变化中
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
                //文本变化前
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction("panhouye");
//			filter.addAction("ANDLINK.searchack");
//			filter.addAction("ANDLINK.netinfo");
        registerReceiver(broadcastReceiver, filter);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//			case R.id.but0:
//				break;
            case R.id.but1:
                playTone(ToneGenerator.TONE_DTMF_1);
                change("1");
                break;
            case R.id.but2:
                playTone(ToneGenerator.TONE_DTMF_2);
                change("2");
                break;
            case R.id.but3:
                playTone(ToneGenerator.TONE_DTMF_3);
                change("3");
                break;
            case R.id.but4:
                playTone(ToneGenerator.TONE_DTMF_4);
                change("4");
                break;
            case R.id.but5:
                playTone(ToneGenerator.TONE_DTMF_5);
                change("5");
                break;
            case R.id.but6:
                playTone(ToneGenerator.TONE_DTMF_6);
                change("6");
                break;
            case R.id.but7:
                playTone(ToneGenerator.TONE_DTMF_7);
                change("7");
                break;
            case R.id.but8:
                playTone(ToneGenerator.TONE_DTMF_8);
                change("8");
                break;
            case R.id.but9:
                playTone(ToneGenerator.TONE_DTMF_9);
                change("9");
                break;
            case R.id.but10:
                playTone(ToneGenerator.TONE_DTMF_S);
                change("*");
                break;
            case R.id.but11:
                playTone(ToneGenerator.TONE_DTMF_0);
                change("0");
                break;
            case R.id.but12:
                playTone(ToneGenerator.TONE_DTMF_P);
                change("#");
                break;
            case R.id.but13:
                //call();
                if (login) {
                    ICmccManager.getInstance().actionStartImsAudio(this, "0" + tv.getText().toString());
                } else {
                    Toast.makeText(ImsCall1Activity.this, "IMS未登录成功，请稍后重试！", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.but14:
                if (login) {
                    Intent intent = new Intent();
                    intent.setClass(this, SmartInteractive.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ImsCall1Activity.this, "IMS未登录成功，请稍后重试！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.but15:
                delete();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * 播放按键声音
     */
    private void playTone(int tone) {
        if (!mDTMFToneEnabled) {
            return;
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_SILENT
                || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            // 静音或者震动时不发出声音
            return;
        }
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "playTone: mToneGenerator == null, tone: " + tone);
                return;
            }
            mToneGenerator.startTone(tone, DTMF_DURATION_MS);   //发出声音
        }
    }

    private void change(String number) {
        StringBuffer sb = new StringBuffer(tv.getText());
        tv.setText(sb.append(number));
    }

    /**
     * 点击删除按钮删除操作
     */
    private void delete() {
        if (tv.getText() != null && tv.getText().length() > 1) {
            StringBuffer sb = new StringBuffer(tv.getText());
            tv.setText(sb.substring(0, sb.length() - 1));
        } else if (tv.getText() != null && !"".equals(tv.getText())) {
            tv.setText("");
        }
    }

    private void call() {
        /**
         * 打电话需要获取系统权限，需要到AndroidManifest.xml里面配置权限 
         * <uses-permission android:name="android.permission.CALL_PHONE"/> 
         */
        Intent intent = new Intent();
        //设置意图要做的事，这里是打电话  1111111111111
        intent.setAction(Intent.ACTION_CALL);
        //设置参数 Uri请求资源表示符
        intent.setData(Uri.parse("tel:" + tv.getText()));
        startActivity(intent);
    }
}
