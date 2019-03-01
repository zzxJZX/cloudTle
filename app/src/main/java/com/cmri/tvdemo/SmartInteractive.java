package com.cmri.tvdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.cmri.moudleapp.moudlevoip.ICmccManager;
import com.cmri.moudleapp.moudlevoip.adapter.ChatAdapter;
import com.cmri.moudleapp.moudlevoip.bean.PersonChat;
import com.cmri.moudleapp.moudlevoip.view.VoiceLineView;
import com.cmri.tvdemo.tools.Tools;
import com.cmri.tvdemo.ui.BlAlert;
import com.google.gson.JsonArray;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;


/**
 * Created by Administrator on 2017/12/12.
 */

public class SmartInteractive extends Activity implements Runnable, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    public static boolean WakeUp = false;
    private ArrayList<TrainInstance> trainlist;
    private ArrayList<FightInstance> fightlist;
    private ArrayList<TrainInstance1> trainInstance1s;
    private  ArrayList<Person> personlist;

    private MediaRecorder mMediaRecorder;
    private MediaPlayer mediaPlayer;
    private SpeechSynthesizer mTts;
    private boolean isAlive = true;
    private VoiceLineView voiceLineView;
    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            if(mMediaRecorder==null) return;
//            double ratio = (double) mMediaRecorder.getMaxAmplitude() / 100;
//            double db = 0;// 分贝
//            //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
//            //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
//            //同时，也可以配置灵敏度sensibility
//            if (ratio > 1)
//                db = 20 * Math.log10(ratio);
//            //只要有一个线程，不断调用这个方法，就可以使波形变化
//            //主要，这个方法必须在ui线程中调用
//            System.out.println("smartdb:"+db);
            voiceLineView.setVolume((int) (DB));
        }
    };
    private ImageButton smart_back;
    private ChatAdapter chatAdapter;
    private ListView lv_chat_dialog;
    private int mAIUIState = AIUIConstant.STATE_IDLE;
    private List<PersonChat> personChats = new ArrayList<PersonChat>();
    private boolean isfind = true;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int what = msg.what;
            switch (what) {
                case 1:
                    lv_chat_dialog.setSelection(personChats.size());
                    break;
                case 3:
                    String songid = (String) msg.obj;
                    playmusic("http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=" + songid);
                    break;
                case 4:
                    stopVoiceNlp();
                    if (musictype == 1) {
                        play_url = (String) msg.obj;
                        speakAndDisplay(my_text, "马上为您播放" + value);
                    } else if (musictype == 2) {
                        play_url = (String) msg.obj;
                        speakAndDisplay(my_text, "马上为您播放" + artist + "的" + value);
                    } else if (musictype == 3) {
                        speakAndDisplay(my_text, "抱歉，没有找到歌曲");
                    } else {
                        play_url = (String) msg.obj;
                        speakAndDisplay(my_text, "马上为您播放");
                    }

                    // playUrl(play_url);
                    break;


                case 5:
                    final TrainInstance1 trainInstance1= (TrainInstance1) msg.obj;
                    RequestParams requestParams=new RequestParams();
                    requestParams.put("stationName",trainInstance1.getStart_station());
                    requestParams.put("key","6fc046bbd910ca16c8039ae3cfc52cf5");
                    asyncHttpClient.get(SmartInteractive.this, "http://op.juhe.cn/trainTickets/cityCode", requestParams, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String s=new String(responseBody);
                            try {
                                JSONObject jsonObject=new JSONObject(s);
                                startcode = jsonObject.optJSONObject("result").optString("code");
                                Message message=Message.obtain();
                                message.what=6;
                                message.obj=trainInstance1;
                                handler.sendMessage(message);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                    break;
                case 6:
                    final TrainInstance1 trainInstance2= (TrainInstance1) msg.obj;
                    RequestParams requestParams1=new RequestParams();
                    requestParams1.put("stationName",trainInstance2.getEnd_station());
                    requestParams1.put("key","6fc046bbd910ca16c8039ae3cfc52cf5");
                    asyncHttpClient.get(SmartInteractive.this, "http://op.juhe.cn/trainTickets/cityCode", requestParams1, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String s=new String(responseBody);
                            try {
                                JSONObject jsonObject=new JSONObject(s);
                                endcode = jsonObject.optJSONObject("result").optString("code");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                    break;
                case 7:
                    System.out.println("xinxitrain_date:"+starttime+"from_station_code:"+startcode+"to_station_code:"+endcode+"checi:"+checi+"passportseno:"+numbercode+"price:"+trainprice+"zwcode:"+zuoxicode+"zwname:"+zuoyi+"passengersename"+TickName);
                    RequestParams requestParams2=new RequestParams();
                    requestParams2.put("key","6fc046bbd910ca16c8039ae3cfc52cf5");
                    requestParams2.put("user_orderid","12345678");
                    requestParams2.put("train_date",starttime);
                    requestParams2.put("from_station_code",startcode);
                    requestParams2.put("to_station_code",endcode);
                    requestParams2.put("checi",checi);
                    JSONArray jsonArray=new JSONArray();
                    JSONObject jsonObject=new JSONObject();
                    try {
                        jsonObject.put("passengerid",1);
                        jsonObject.put("passengersename",TickName);
                        jsonObject.put("piaotype","1");
                        jsonObject.put("piaotypename","成人票");
                        jsonObject.put("passporttypeseid","1");
                        jsonObject.put("passporttypeseidname","二代身份证");
                        jsonObject.put("passportseno",numbercode);
                        jsonObject.put("price",trainprice);
                        jsonObject.put("zwcode",zuoxicode);
                        jsonObject.put("zwname",zuoyi);
                        jsonArray.put(jsonObject);
                        System.out.println("jjjfsongk:"+jsonArray.toString());
                        requestParams2.put("passengers",jsonArray.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String sss="key="+"6fc046bbd910ca16c8039ae3cfc52cf5"+"&user_orderid="+"12345678"+"&train_date="+starttime+"&from_station_code="+startcode+"&to_station_code="+endcode+"&checi="+checi;
                    System.out.println("jjjfsongk1:"+sss);
                    asyncHttpClient.get(SmartInteractive.this, "http://op.juhe.cn/trainTickets/submit", requestParams2, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String s=new String(responseBody);

                            System.out.println("订票："+s);

                            try {
                                JSONObject jsonObject=new JSONObject(s);
                                int errorcode = jsonObject.optInt("error_code");
                                if (errorcode==0){
                                    orderid = jsonObject.optJSONObject("result").optString("orderid");
                                    speakAndDisplayForNext("", "订单已提交，正在请求出票,请稍后 ", "订单已提交，正在请求出票,订单号是"+ orderid);
                                    Message message=Message.obtain();
                                    message.what=8;
                                    //  handler.sendMessage(message);
                                    handler.sendMessageDelayed(message,60000);
                                }else{
                                    String reason=jsonObject.optString("reason");
                                    speakAndDisplayForNext("", reason, reason);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                    break;
                case 8:

                    RequestParams requestParams3=new RequestParams();
                    requestParams3.put("key","6fc046bbd910ca16c8039ae3cfc52cf5");
                    requestParams3.put("orderid",orderid);
                    System.out.println("orderiiidd:"+orderid);
                    asyncHttpClient.get(SmartInteractive.this, "http://op.juhe.cn/trainTickets/pay", requestParams3, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String s=new String(responseBody);
                            try {
                                JSONObject jsonObject=new JSONObject(s);
                                int errorcode = jsonObject.optInt("error_code");
                                if(errorcode==0){
                                    String  result = jsonObject.optString("result");
                                    speakAndDisplayForNext("", result, result);
                                }else{
                                    String  reason = jsonObject.optString("reason");
                                    speakAndDisplayForNext("", reason, reason);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                    break;

                default:
                    break;
            }
        }
    };
    private Button voice_button;

    private AIUIAgent mAIUIAgent = null;

    private int DB = 0;

    private boolean voice = true;


    private int[] mWinds;

    /**
     * 开关
     **/
    private final int SWITCH = 0;
    /**
     * 模式
     **/
    private final int MODE = 1;
    /**
     * 温度加
     **/
    private final int TEM_ADD = 2;
    /**
     * 温度减
     **/
    private final int TEM_RED = 3;
    /**
     * 风速加
     **/
    private final int WIND_ADD = 4;
    /**
     * 风减
     **/
    private final int WIND_RED = 4;

    private String call_phone;

    private String audio_type = "";

    private String play_url = "";
    private TvApp tvApp;
    private String[] welcomeArrays;
    private String my_text = "";
    private int musictype = 0;
    private String value = "";
    private String artist;
    private String songid;
    private String endstate;
    private String startstate;
    private String starttime;
    private String checi;
    private String startcode;
    private String endcode;
    private String zuoyi;
    private String orderid;
    private String zuoxicode;
    private String trainprice;
    private String TickName;
    private String numbercode;

    private TrainInstance1 traininstanse3;

    private AsyncHttpClient asyncHttpClient;


    public String getWellcometips() {
        String wellcomTips = "";
        int id = (int) (Math.random() * (welcomeArrays.length - 1));//随机产生一个index索引
        wellcomTips = welcomeArrays[id];
        return wellcomTips;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvApp = (TvApp) getApplication();
        welcomeArrays = this.getResources().getStringArray(R.array.randomserch);
        trainlist = new ArrayList<>();
        fightlist = new ArrayList<>();
        //getcontactlist();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.smart_interactive);

        asyncHttpClient=new AsyncHttpClient();

        trainInstance1s=new ArrayList<>();
        personlist=new ArrayList<>();

        Setting.setLocationEnable(true);

        WakeUp = true;
        mTts = SpeechSynthesizer.createSynthesizer(SmartInteractive.this, mTtsInitListener);
        voice_button = (Button) findViewById(R.id.voice_button);
        voice_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (voice) {
                    if (mTts != null && mTts.isSpeaking()) {
                        mTts.stopSpeaking();
                    }
//                    initial();
//                    try {
//                        mMediaRecorder.prepare();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    mMediaRecorder.start();
                    //新增
                    tvApp.stop_wake();
                    startVoiceNlp();
                    voice = false;
                    voice_button.setBackgroundResource(R.drawable.btn_stoprecord);

                    pause();
                } else {
//                    if(mMediaRecorder!=null) {
//                        mMediaRecorder.stop();
//                        mMediaRecorder.release();
//                    }
//                    mMediaRecorder=null;
                    stopVoiceNlp();
                    voice = true;
                    voice_button.setBackgroundResource(R.drawable.btn_startrecord);
                    tvApp.start_Wake();
                }
                isjennry = false;
            }
        });
        smart_back = (ImageButton) findViewById(R.id.smart_back);
        smart_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lv_chat_dialog = (ListView) findViewById(R.id.lv_chat_dialog);
        voiceLineView = (VoiceLineView) findViewById(R.id.voicLine);

        Thread thread = new Thread(this);
        thread.start();


//        for (int i = 0; i <= 3; i++) {
        PersonChat personChat = new PersonChat();
        personChat.setMeSend(false);
        personChat.setChatMessage("对我说点什么吧！");
        personChats.add(personChat);
//        }
        lv_chat_dialog = (ListView) findViewById(R.id.lv_chat_dialog);
        /**
         *setAdapter
         */
        chatAdapter = new ChatAdapter(this, personChats);
        lv_chat_dialog.setAdapter(chatAdapter);


        checkAIUIAgent();

        initMedia();
        // playmusic();
        //readContacts();

        //postNameForAIUI();

        if (WakeUp) {
            tvApp.stop_wake();
            if (mTts != null && mTts.isSpeaking()) {
                mTts.stopSpeaking();
            }
            FlowerCollector.onEvent(SmartInteractive.this, "tts_play");
            setParam("xiaoyan");
            mTts.startSpeaking("我在呢", mTtsForWakeListener);
            stopVoiceNlp();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.EXTRA_REASON);
        filter.addAction("HangUp");
        registerReceiver(WakeUpReceiver, filter);

    }

    private void postNameForAIUI() {
//        postCZName();
//        postDPName();
//        postTVName();
//        postACName();
        postPhoneNumber();
    }


    private void postPhoneNumber() {
        byte[] syncData = null;
        try {
            JSONObject syncSchemaJson = new JSONObject();
            JSONObject paramJson = new JSONObject();
            paramJson.put("id_name", "uid");
            paramJson.put("res_name", "IFLYTEK.telephone_contact");
            syncSchemaJson.put("param", paramJson);
            String allName = "";
//            for (int i = 0; i < contacts.size(); i++) {
//                JSONObject object = new JSONObject();
//                object.put("name",contacts.get(i).get("name"));
//                object.put("phoneNumber",contacts.get(i).get("phone"));
//                allName = allName + object.toString() + "\n";
//            }
            Iterator it = contacts.iterator();
            System.out.println("NlpDemo888:" + contacts.toString());
            while (it.hasNext()) {
                PhoneList phoneList = (PhoneList) it.next();
                System.out.println("NlpDemo888:" + phoneList.getName() + phoneList.getPhone());
                if (phoneList.getName() != null && phoneList.getPhone() != null) {
                    JSONObject object = new JSONObject();
                    object.put("name", phoneList.getName());
                    object.put("phoneNumber", phoneList.getPhone());
                    allName = allName + object.toString() + "\n";
                }
            }
            allName = allName.trim();
            System.out.println("NlpDemoAllNamePhone:" + allName);
            System.out.println("NlpDemoNamePhone64:" + android.util.Base64.encodeToString(allName.getBytes("utf-8"), android.util.Base64.NO_WRAP));
            syncSchemaJson.put("data", android.util.Base64.encodeToString(allName.getBytes("utf-8"), android.util.Base64.NO_WRAP));
            System.out.println("NlpDemoPhone:" + syncSchemaJson.toString());
            syncData = syncSchemaJson.toString().getBytes("utf-8");
            System.out.println("NlpDemoPhone:" + syncData.toString());

            JSONObject paramJson2 = new JSONObject();
            paramJson2.put("tag", "sync-tag");


            AIUIMessage syncAthenaMessage = new AIUIMessage(AIUIConstant.CMD_SYNC,
                    AIUIConstant.SYNC_DATA_SCHEMA, 0, paramJson2.toString(), syncData);
            if (mAIUIAgent != null) {
                mAIUIAgent.sendMessage(syncAthenaMessage);
            }
//            mAIUIAgent.sendMessage(syncAthenaMessage);
        } catch (Exception e) {

        }
    }


    private void initMedia() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(SmartInteractive.this);
        mediaPlayer.setOnPreparedListener(SmartInteractive.this);


    }

    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void playUrl(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String TAG = SmartInteractive.class.getSimpleName();


    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    private boolean checkAIUIAgent() {
        if (null == mAIUIAgent) {
            mAIUIAgent = AIUIAgent.createAgent(this, getAIUIParams(), mAIUIListener);
            AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
            if (mAIUIAgent != null) {
                mAIUIAgent.sendMessage(startMsg);
            }
//            mAIUIAgent.sendMessage(startMsg);
        }
        if (null == mAIUIAgent) {
            final String strErrorTip = "创建 AIUI Agent 失败！";
        }

        return null != mAIUIAgent;
    }

    private void startVoiceNlp() {
        Log.i("smartinteran", "startVoiceNlp");

        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
        if (AIUIConstant.STATE_WORKING != this.mAIUIState) {
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        // 打开AIUI内部录音机，开始录音
        String params = "sample_rate=16000,data_type=audio";
        AIUIMessage writeMsg = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null);
        mAIUIAgent.sendMessage(writeMsg);
    }

    private void stopVoiceNlp() {
        Log.i("smartinteran", "stopVoiceNlp");
        // 停止录音
        String params = "sample_rate=16000,data_type=audio";
        AIUIMessage stopWriteMsg = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null);
//        AIUIMessage syncAthenaMessage = new AIUIMessage();
        mAIUIAgent.sendMessage(stopWriteMsg);

    }

    private boolean ismusic = false;
    private boolean issleep = false;
    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {

            switch (event.arg1) {
                case AIUIConstant.CMD_SYNC:
                    int dtype = event.data.getInt("sync_dtype");
                    System.out.println("NlpDemo:同步中");
                    //arg2表示结果
                    if (0 == event.arg2) {// 同步成功
                        if (AIUIConstant.SYNC_DATA_SCHEMA == dtype) {
                            final String mSyncSid = event.data.getString("sid");
                            String tag = event.data.getString("tag");
                            System.out.println("NlpDemo:同步成功" + mSyncSid + "," + tag);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject paramsJson = new JSONObject();
                                    try {
                                        paramsJson.put("sid", mSyncSid);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AIUIMessage querySyncMsg = new AIUIMessage(AIUIConstant.CMD_QUERY_SYNC_STATUS, AIUIConstant.SYNC_DATA_SCHEMA, 0, paramsJson.toString(), null);
                                    if (mAIUIAgent != null) {
                                        mAIUIAgent.sendMessage(querySyncMsg);
                                    }
                                }
                            }, 3000);
                        }
                    } else {
                        if (AIUIConstant.SYNC_DATA_SCHEMA == dtype) {
                            String mSyncSid = event.data.getString("sid");
                            System.out.println("NlpDemo:同步失败");
                        }
                    }
                    break;

                case AIUIConstant.CMD_SET_PARAMS:

                    break;

                case AIUIConstant.CMD_QUERY_SYNC_STATUS:
                    int syncType = event.data.getInt("sync_dtype");

                    if (AIUIConstant.SYNC_DATA_QUERY == syncType) {
                        String result = event.data.getString("result");

                        if (0 == event.arg2) {

                            JSONObject params = new JSONObject();
                            JSONObject audioParams = new JSONObject();
                            try {
                                audioParams.put("pers_param", "{\"uid\":\"\"}");
                                params.put("audioparams", audioParams);
                                byte[] s = new byte[0];
                                AIUIMessage setMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, params.toString(), s);
//                                mAIUIAgent.sendMessage(setMsg);
                                if (mAIUIAgent != null) {
                                    mAIUIAgent.sendMessage(setMsg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


//                            Toast.makeText(SmartInteractive.this,"查询结果：" + result,Toast.LENGTH_LONG).show();
                        } else {
//                            Toast.makeText(SmartInteractive.this,"schema数据状态查询出错：" + event.arg2 + ", result:" + result,Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }

            switch (event.eventType) {
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                    Log.e("conecttionserver", "EVENT_CONNECTED_TO_SERVER");
                    readContacts();

                    break;
                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    Log.e("conecttionserver", "EVENT_SERVER_DISCONNECTED");
                    break;
                case AIUIConstant.EVENT_ERROR: {
                    Log.i(TAG, "on eventerrrotttt: " + event.eventType + "错误: " + event.arg1 + "\n" + event.info);
//                    mNlpText.append("\n");
//                    mNlpText.append("错误: " + event.arg1 + "\n" + event.info);
                }
                break;

                case AIUIConstant.EVENT_VAD: {
                    if (AIUIConstant.VAD_BOS == event.arg1) {
//                        showTip("找到vad_bos");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
//                        showTip("找到vad_eos");
                    } else {
//                        showTip("" + event.arg2);
                        DB = event.arg2;
                    }
                }
                break;

                case AIUIConstant.EVENT_START_RECORD: {
                    Log.i(TAG, "on eventstartrecode: " + event.eventType);
//                    showTip("开始录音");
                }
                break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType);
//                    showTip("停止录音");
                }
                break;

                case AIUIConstant.EVENT_STATE: {    // 状态事件
                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
//                        showTip("STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
//                        showTip("STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
//                        showTip("STATE_WORKING");
                    }
                }
                break;

                case AIUIConstant.EVENT_SLEEP:
                    mAIUIState = event.arg1;
                    if (AIUIConstant.TYPE_AUTO == mAIUIState) {
                        Log.i(TAG, "on event: 交互超时");
                        stopVoiceNlp();
                        voice = true;
                        voice_button.setBackgroundResource(R.drawable.btn_startrecord);

                        tvApp.start_Wake();
                    } else if (AIUIConstant.TYPE_COMPEL == mAIUIState) {
                        Log.i(TAG, "on event: 强制休眠");
                    }
                    break;

                case AIUIConstant.EVENT_CMD_RETURN: {
                    if (AIUIConstant.CMD_UPLOAD_LEXICON == event.arg1) {
//                        showTip("上传" + (0 == event.arg2 ? "成功" : "失败"));
                    }
                }
                break;

                case AIUIConstant.EVENT_WAKEUP:
                    Log.i(TAG, "on eventwakeup: " + event.eventType);
//                    showTip("进入识别状态");
                    break;

                case AIUIConstant.EVENT_RESULT:
                    System.out.println("EVENT_RESULT1111");
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);
                        Log.i("smartinteran22", bizParamJson.toString());
                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            Log.i("smartinteran11", new String(event.data.getByteArray(cnt_id), "utf-8"));
                            if (!new String(event.data.getByteArray(cnt_id), "utf-8").equals("")) {
                                JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                                String sub = params.optString("sub");
                                if ("nlp".equals(sub)) {
                                    boolean local = false;
                                    String resultStr = cntJson.optString("intent");
                                    JSONObject intent = new JSONObject(resultStr);
                                    String category = intent.optString("category");
                                    System.out.println("SmartInteractive22:" + cntJson.optString("intent"));
                                    String service1 = intent.optString("service");
                                    if (service1.equals("") && (!isjennry)) {
                                        System.out.println("isjennry11");
                                        String text = intent.optString("text");
                                        if (!text.equals("")) {
                                            if (text.equals("睡觉") || text.equals("睡眠") || text.equals("关闭") || text.equals("关机")) {
                                                issleep = true;
                                                speakAndDisplayForNext(text, "再见", "再见");
                                            } else {
                                                stopVoiceNlp();
                                                speakAndDisplayForNext(text, "我听不懂你说什么", "我听不懂你说什么");
                                            }

                                        } else {

                                        }

                                    } else {
                                        final JSONObject reply = new JSONObject(cntJson.optString("intent"));
                                        JSONObject dataPhone = reply.optJSONObject("data");
                                        String service = reply.optString("service");
                                        System.out.println("smartService:" + service);
                                        if (category.equals("")) {
                                            //计算1
                                            if (service.equals("calc")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //餐馆2
                                            else if (service.equals("restaurantSearch")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                JSONObject jsonObject = reply.optJSONObject("data").optJSONArray("result").getJSONObject(0);
                                                String city = jsonObject.optString("city");
                                                String area = jsonObject.optString("area");
                                                String address = jsonObject.optString("address");
                                                String name = jsonObject.optString("name");
                                                speakAndDisplayForNext(my_text, "为你找到" + city + area + address + name, "为你找到" + city + area + address + name);
                                            }
                                            //酒店3
                                            else if (service.equals("hotelSearch")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                JSONObject jsonObject = reply.optJSONObject("data").optJSONArray("result").getJSONObject(0);
                                                String city = jsonObject.optString("city");
                                                String area = jsonObject.optString("area");
                                                String address = jsonObject.optString("address");
                                                String name = jsonObject.optString("name");
                                                speakAndDisplayForNext(my_text, "为你找到" + city + area + address + name, "为你找到" + city + area + address + name);
                                            }
                                            //停车场4
                                            else if (service.equals("parkingLot")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                JSONObject jsonObject = reply.optJSONObject("data").optJSONArray("result").getJSONObject(0);
                                                String city = jsonObject.optString("city");
                                                String area = jsonObject.optString("area");
                                                String address = jsonObject.optString("address");
                                                String name = jsonObject.optString("name");
                                                speakAndDisplayForNext(my_text, "为你找到" + city + area + address + name, "为你找到" + city + area + address + name);
                                            }
                                            //加油站5
                                            else if (service.equals("gasStation")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                JSONObject jsonObject = reply.optJSONObject("data").optJSONArray("result").getJSONObject(0);
                                                String city = jsonObject.optString("city");
                                                String area = jsonObject.optString("area");
                                                String address = jsonObject.optString("address");
                                                String name = jsonObject.optString("name");
                                                speakAndDisplayForNext(my_text, "为你找到" + city + area + address + name, "为你找到" + city + area + address + name);
                                            }
                                            //火车6
                                            else if (service.equals("train")){
                                                stopVoiceNlp();
                                                final String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                final String answer_text = answer.optString("text");
                                                JSONObject jsonObject = reply.optJSONObject("data");

                                                if (jsonObject==null){
                                                    speakAndDisplayForNext(my_text,answer_text,answer_text);
                                                }else{
                                                    //dingpiao
                                                    JSONArray semantic= reply.optJSONArray("semantic");
                                                    JSONArray slots=semantic.getJSONObject(0).getJSONArray("slots");
                                                    String code=slots.getJSONObject(0).getString("name");
                                                    if (code.equals("code")){
                                                        if (slots.getJSONObject(4).getString("name").equals("seat")){
                                                            zuoyi = slots.getJSONObject(4).getString("value");
                                                            switch (zuoyi){
                                                                case "硬座":
                                                                    zuoxicode="1";
                                                                    break;
                                                                case "硬卧":
                                                                    zuoxicode="3";
                                                                    break;
                                                                case "软卧":
                                                                    zuoxicode="4";
                                                                    break;
                                                                case "二等座":
                                                                    zuoxicode="O";
                                                                    break;
                                                                case "一等座":
                                                                    zuoxicode="M";
                                                                    break;
                                                                case "商务座":
                                                                    zuoxicode="9";
                                                                    break;
                                                            }
                                                            if (traininstanse3!=null){
                                                                ArrayList<TrainPrice> trainPrices = traininstanse3.getTrainPrices();
                                                                for (int m=0;m<trainPrices.size();m++){
                                                                    if (trainPrices.get(m).getPrice_type().equals(zuoyi)){
                                                                        trainprice=trainPrices.get(m).getPrice();

                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            speakAndDisplayForNext(zuoyi, "已为您找到" + zuoyi + ",请选择订票人","已为您找到" + zuoyi + ",请选择订票人" );
                                                      /*  final AlertDialog.Builder  builder=new AlertDialog.Builder(SmartInteractive.this);
                                                        builder.setTitle("请输入姓名");
                                                        View view=View.inflate(SmartInteractive.this,R.layout.lianmai,null);
                                                        final EditText editText= (EditText) view.findViewById(R.id.edit);
                                                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                                TickName=editText.getText().toString();
                                                                speakAndDisplayForNext(TickName,  "请说出订票人身份证号码","请说出订票人身份证号码" );
                                                                isIdentity=true;

                                                            }
                                                        });
                                                      *//*  builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });*//*
                                                        builder.setView(view);
                                                        builder.show();*/
                                                            asyncHttpClient.get("http://www.daiyong77.com/interfaceHH/userList.php", new RequestParams(), new AsyncHttpResponseHandler() {
                                                                @Override
                                                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                                    String s=new String(responseBody);
                                                                    try {
                                                                        JSONObject jsonObject1=new JSONObject(s);
                                                                        int status = jsonObject1.optInt("status");
                                                                        if (status==1){
                                                                            JSONArray jsonArray = jsonObject1.optJSONArray("data");
                                                                            for (int j=0;j<jsonArray.length();j++){
                                                                                String name = jsonArray.getJSONObject(j).optString("name");
                                                                                String idcard = jsonArray.getJSONObject(j).optString("idcard");
                                                                                personlist.add(new Person(name,idcard));
                                                                            }
                                                                            BlAlert.showAlert(SmartInteractive.this, personlist, new BlAlert.OnAlertSelectId1() {
                                                                                @Override
                                                                                public void onClick(Person person) {
                                                                                    TickName=person.getName();
                                                                                    numbercode=person.getIdcard();
                                                                                    speakAndDisplayForNext(TickName+"  "+numbercode, "正在提交订单","正在提交订单");
                                                                                    Message message=Message.obtain();
                                                                                    message.what=7;
                                                                                    handler.sendMessage(message);
                                                                                }
                                                                            }, null);
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                                                }
                                                            });

                                                        }else {
                                                            System.out.println("traincode");
                                                            boolean isfind=false;
                                                            endstate = slots.getJSONObject(2).getString("value");
                                                            startstate = slots.getJSONObject(6).getString("value");
                                                            checi = slots.getJSONObject(0).getString("value").toUpperCase();
                                                            for (int i = 0; i < trainInstance1s.size(); i++) {
                                                                System.out.println("traincode1");
                                                                if (trainInstance1s.get(i).getTrain_no().equals(checi.toUpperCase())) {
                                                                    traininstanse3=trainInstance1s.get(i);
                                                                    System.out.println("traincode2");
                                                                    isfind=true;
                                                                    speakAndDisplayForNext(checi, "已为您找到" + checi + ",请选择坐席", traininstanse3.toString());
                                                                    Message message = Message.obtain();
                                                                    message.what = 5;
                                                                    message.obj = trainInstance1s.get(i);
                                                                    handler.sendMessage(message);

                                                                    break;
                                                                }
                                                            }
                                                            if (!isfind){
                                                                speakAndDisplayForNext(checi, "没有为您找到" + checi + ",请重新说明","没有为您找到" + checi + ",请重新说明" );
                                                            }
                                                        }
                                                    }else {

                                                        endstate = slots.getJSONObject(1).getString("value");
                                                        startstate = slots.getJSONObject(5).getString("value");
                                                        String s1 =slots.getJSONObject(3).getString("normValue").replace("\\","");
                                                        JSONObject jsonObject1=new JSONObject(s1);
                                                        starttime = jsonObject1.optString("datetime");
                                                        System.out.println("starttimee:"+ starttime);

                                                        System.out.println("lijkjjsl:" + startstate + ".." + endstate);
                                                        RequestParams requestParams = new RequestParams();
                                                        requestParams.put("start", startstate);
                                                        requestParams.put("end", endstate);
                                                        requestParams.put("key", "64dfb9315698270444df76e73bdec42f");
                                                        asyncHttpClient.get(SmartInteractive.this, "http://apis.juhe.cn/train/s2swithprice", requestParams, new AsyncHttpResponseHandler() {

                                                            @Override
                                                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                                trainInstance1s.clear();
                                                                try {
                                                                    String s = new String(responseBody);
                                                                    System.out.println("dingpaiohfui:" + s);
                                                                    JSONObject jsonObject1 = new JSONObject(s);
                                                                    JSONArray jsonArray = jsonObject1.optJSONObject("result").optJSONArray("list");
                                                                    if (jsonArray !=null && !jsonArray.toString().equals("null") && jsonArray.length() >0){
                                                                        for (int j = 0; j < jsonArray.length(); j++) {
                                                                            JSONObject jsonObject2 = jsonArray.getJSONObject(j);
                                                                            ArrayList<TrainPrice> trainPrices = new ArrayList<TrainPrice>();
                                                                            JSONArray jsonArray1 = jsonObject2.optJSONArray("price_list");
                                                                            for (int m = 0; m < jsonArray1.length(); m++) {
                                                                                JSONObject jsonObject3 = jsonArray1.getJSONObject(m);
                                                                                TrainPrice trainPrice = new TrainPrice(jsonObject3.getString("price_type"), jsonObject3.getString("price"));
                                                                                trainPrices.add(trainPrice);
                                                                            }
                                                                            TrainInstance1 trainInstance1 = new TrainInstance1(jsonObject2.getString("train_no"), jsonObject2.getString("start_station"), jsonObject2.getString("end_station"), jsonObject2.getString("start_time"), jsonObject2.getString("end_time"), jsonObject2.getString("run_time"), trainPrices);
                                                                            trainInstance1s.add(trainInstance1);
                                                                        }
                                                                        System.out.println("traintansetlll:" + trainInstance1s);

                                                                        if (trainInstance1s.size() == 0) {
                                                                            speakAndDisplayForNext(my_text, answer_text, answer_text);
                                                                        } else {
                                                                            speakAndDisplayForNext(my_text, "为您找到以下车次,请选择您要乘坐的车次"+trainInstance1s.toString(), "为您找到以下车次,请选择您要乘坐的车次"+trainInstance1s.toString());
                                                                        }
                                                                    } else {
                                                                        String answer_error_text = "未找到您需要的票,请确认目的地是否正确!";
                                                                        speakAndDisplayForNext(my_text, answer_error_text, answer_error_text);
                                                                    }

                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }


                                                            @Override
                                                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                                speakAndDisplayForNext(my_text, "很抱歉，没有找到", "很抱歉，没有找到");
                                                            }


                                                        });
                                                    }
                                                    //
                                               /* JSONArray jsonArray = jsonObject.optJSONArray("result");
                                                for(int i=0;i<jsonArray.length();i++){
                                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                    String originStation = jsonObject1.optString("originStation");
                                                    String terminalStation = jsonObject1.optString("terminalStation");
                                                    String trainNo = jsonObject1.optString("trainNo");
                                                    String startTime = jsonObject1.optString("startTime");
                                                    trainlist.add(new TrainInstance(startTime,originStation,terminalStation,trainNo));
                                                }
                                                System.out.println("trainlistsize"+trainlist.size());
                                                if(trainlist.size()==0){
                                                    speakAndDisplayForNext(my_text,answer_text,answer_text);
                                                }else{
                                                    speakAndDisplayForNext(my_text,answer_text,trainlist.toString());
                                                }*/
                                                }
                                            }
                                            //航班7
                                            else if (service.equals("flight")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                JSONObject jsonObject = reply.optJSONObject("data");

                                                if (jsonObject == null) {
                                                    speakAndDisplayForNext(my_text, answer_text, answer_text);
                                                } else {
                                                    JSONArray jsonArray = jsonObject.optJSONArray("result");
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                        String takeOffTime = jsonObject1.optString("takeOffTime");
                                                        String dPort = jsonObject1.optString("dPort");
                                                        String aPort = jsonObject1.optString("aPort");
                                                        String flight = jsonObject1.optString("flight");
                                                        fightlist.add(new FightInstance(takeOffTime, dPort, aPort, flight));
                                                    }
                                                    System.out.println("flightlistsize" + fightlist.size());
                                                    if (fightlist.size() == 0) {
                                                        speakAndDisplayForNext(my_text, answer_text, answer_text);
                                                    } else {
                                                        speakAndDisplayForNext(my_text, answer_text, fightlist.toString());
                                                    }
                                                }
                                            }
                                            //成语
                                            else if (service.equals("idiom")) {
                                                String my_text = reply.optString("text");
                                                JSONObject re_data = reply.optJSONObject("data");
                                                JSONObject result0 = re_data.optJSONArray("result").getJSONObject(0);
                                                String interpretation = result0.optString("interpretation");
                                                stopVoiceNlp();
                                                speakAndDisplayForNext(my_text, interpretation, interpretation);

                                            }
                                            //菜谱8
                                            else if (service.equals("cookbook")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                JSONObject re_data = reply.optJSONObject("data");
                                                JSONObject result0 = re_data.optJSONArray("result").getJSONObject(0);
                                                String accessory = result0.optString("accessory");
                                                String ingredient = result0.optString("ingredient");
                                                String steps = result0.optString("steps");
                                                String answer_text = answer.optString("text") + "\n" + accessory + "," + ingredient + "。" + steps;
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //翻译9
                                            else if (service.equals("translation")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject re_data = reply.optJSONObject("data");
                                                JSONObject result0 = re_data.optJSONArray("result").getJSONObject(0);
                                                String answer_text = result0.optString("translated");
                                                System.out.println("smartText:" + answer_text);
                                                speakEnAndDisplay(my_text, answer_text);
                                            }
                                            //诗词10
                                            else if (service.equals("poetry")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
//                                    JSONObject re_data = reply.optJSONObject("data");
//                                    JSONObject result0 = re_data.optJSONArray("result").getJSONObject(0);
//                                    String author = result0.optString("author");
//                                    String showContent = result0.optString("showContent");
//                                    String title = result0.optString("title");
//                                    String answer_text = title +"\n"+ author +"\n"+ showContent;
                                                if (answer_text.indexOf("k") != -1) {
                                                    answer_text = answer_text.substring(4, answer_text.length() - 4);
                                                }
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //天气11
                                            else if (service.equals("weather")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                if (answer_text.indexOf("k") != -1) {
                                                    answer_text = answer_text.substring(4, answer_text.length() - 4);
                                                }
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //同反义词12
                                            else if (service.equals("wordFinding")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //星座13
                                            else if (service.equals("constellation")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //百科(需要百科两个关键字)14
                                            else if (service.equals("baike")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //时间15
                                            else if (service.equals("datetime")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //问答库16
                                            else if (service.equals("openQA")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplayForNext(my_text, answer_text, answer_text);
                                            }
                                            //音乐17
                                            else if (service.equals("musicX")) {

                                                JSONObject music_semantic0 = reply.optJSONArray("semantic").optJSONObject(0);
                                                String music_intent = music_semantic0.optString("intent");
                                                JSONArray music_slots = music_semantic0.optJSONArray("slots");
                                                my_text = reply.optString("text");
                                                if (music_intent.equals("PLAY")) {
                                                    audio_type = "music";
                                                    if (music_slots.length() == 1) {
                                                        musictype = 1;
                                                        if (music_slots.getJSONObject(0).optString("name").equals("artist")) {
                                                            value = music_slots.getJSONObject(0).optString("value");
                                                        }
                                                        findmusic("http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.6.5.0&method=baidu.ting.search.catalogSug&format=json&query=" + value);
                                                    } else if (music_slots.length() > 1) {
                                                        artist = music_slots.getJSONObject(0).optString("value");
                                                        value = music_slots.getJSONObject(1).optString("value");
                                                        findmoremusic("http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.6.5.0&method=baidu.ting.search.catalogSug&format=json&query=" + artist, value);
                                                    }
                                                } else if (music_intent.equals("RANDOM_SEARCH")) {
                                                    audio_type = "music";
                                                    musictype = 0;
                                                    String songid = getWellcometips();
                                                    Message msg = Message.obtain();
                                                    msg.obj = songid;
                                                    msg.what = 3;
                                                    handler.sendMessage(msg);
                                                } else if (music_intent.equals("INSTRUCTION")) {
                                                    if (music_slots.length() > 0) {
                                                        String value = music_slots.getJSONObject(0).optString("value");
                                                        if (value.equals("pause")) {
                                                            pause();
                                                            startVoiceNlp();
                                                        } else if (value.equals("replay")) {
                                                            play();
                                                            startVoiceNlp();
                                                        } else if (value.equals("sleep")) {
                                                            issleep = true;
                                                            speakAndDisplayForNext(my_text, "再见", "再见");
                                                        }
                                                    }
                                                }
                                  /*      JSONObject music_data = reply.optJSONObject("data");
                                        JSONObject used_state = reply.optJSONObject("used_state");
                                        String state = used_state.optString("state");
                                        JSONObject music_semantic0 = reply.optJSONArray("semantic").optJSONObject(0);
                                        String music_intent = music_semantic0.optString("intent");
                                        JSONArray music_slots = music_semantic0.optJSONArray("slots");
                                        String my_text = reply.optString("text");
                                        System.out.println("smartText:" + my_text);
                                        JSONObject answer = reply.optJSONObject("answer");
                                        String answer_text = answer.optString("text");
                                        System.out.println("smartText:" + answer_text);
                                        if (music_intent.equals("RANDOM_SEARCH")) {
                                            JSONObject music_result0 = music_data.optJSONArray("result").optJSONObject(0);
                                            final String audiopath = music_result0.optString("audiopath");
                                            audio_type = "music";
                                            play_url = audiopath;
//                                            new Thread(new Runnable() {
//
//                                                @Override
//                                                public void run() {
//                                                    playUrl(audiopath);
//                                                }
//                                            }).start();
                                        } else if (music_intent.equals("INSTRUCTION")) {
                                            if (music_slots.length() > 0) {
                                                String value = music_slots.getJSONObject(0).optString("value");
                                                if (value.equals("pause")) {
                                                    pause();
                                                } else if (value.equals("replay")) {
                                                    play();
                                                }
                                            }
                                        }
                                        speakAndDisplay(my_text, answer_text);*/
                                            }
                                            //笑话18
                                            else if (service.equals("joke")) {
                                                stopVoiceNlp();
                                                JSONObject joke_data = reply.optJSONObject("data");
                                                JSONObject joke_result0 = joke_data.optJSONArray("result").optJSONObject(0);
                                                final String mp3Url = joke_result0.optString("mp3Url");
                                                if (mp3Url.equals("")) {
                                                    String my_text = reply.optString("text");
                                                    String joke_content = joke_result0.optString("content");
                                                    speakAndDisplayForNext(my_text, joke_content, joke_content);
                                                } else {
                                                    audio_type = "joke";
                                                    play_url = mp3Url;
                                                    String my_text = reply.optString("text");
                                                    System.out.println("smartText:" + my_text);
                                                    JSONObject answer = reply.optJSONObject("answer");
                                                    String answer_text = answer.optString("text");
                                                    System.out.println("smartText:" + answer_text);
                                                    speakAndDisplay(my_text, answer_text);
//                                            new Thread(new Runnable() {
//
//                                                @Override
//                                                public void run() {
//                                                    playUrl(mp3Url);
//                                                    String my_text = reply.optString("text");
//                                                    System.out.println("smartText:"+my_text);
//                                                    JSONObject answer = reply.optJSONObject("answer");
//                                                    String answer_text = answer.optString("text");
//                                                    System.out.println("smartText:"+answer_text);
//                                                    speakAndDisplay(my_text,answer_text);
//                                                }
//                                            }).start();
                                                }
                                            }
                                            //新闻19
                                            else if (service.equals("news")) {
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                JSONObject news_data = reply.optJSONObject("data");
                                                JSONObject news_result0 = news_data.optJSONArray("result").optJSONObject(0);
                                                JSONObject news_semantic0 = reply.optJSONArray("semantic").optJSONObject(0);
                                                String news_intent = news_semantic0.optString("intent");
                                                JSONArray news_slots = news_semantic0.optJSONArray("slots");
                                                if (news_intent.equals("PLAY")) {
                                                    final String url = news_result0.optString("url");
                                                    audio_type = "news";
                                                    play_url = url;
//                                            new Thread(new Runnable() {
//
//                                                @Override
//                                                public void run() {
//                                                    playUrl(play_url);
//                                                }
//                                            }).start();
                                                } else {
                                                    if (news_slots.length() > 0) {
                                                        JSONObject news_slots0 = news_slots.optJSONObject(0);
                                                        String value = news_slots0.optString("value");
                                                        if (value.equals("PLAY")) {
                                                            play();
                                                            answer_text = "已为您继续播放新闻";
                                                        } else if (value.equals("PAUSE")) {
                                                            pause();
                                                        } else if (value.equals("NEXT")) {
                                                            final String url = news_result0.optString("url");
                                                            audio_type = "news";
                                                            play_url = url;
//                                                    new Thread(new Runnable() {
//
//                                                        @Override
//                                                        public void run() {
//                                                            playUrl(url);
//                                                        }
//                                                    }).start();
                                                        }
                                                    }
                                                }
                                                speakAndDisplay(my_text, answer_text);
                                            }
                                            //故事20
                                            else if (service.equals("story")) {
                                                stopVoiceNlp();
                                                JSONObject story_data = reply.optJSONObject("data");
                                                JSONObject story_result0 = story_data.optJSONArray("result").optJSONObject(0);
                                                final String playUrl = story_result0.optString("playUrl");
                                                audio_type = "story";
                                                play_url = playUrl;
//                                        new Thread(new Runnable() {
//
//                                            @Override
//                                            public void run() {
//                                                playUrl(playUrl);
//                                            }
//                                        }).start();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplay(my_text, answer_text);
                                            }
                                            //广播21
                                            else if (service.equals("radio")) {
                                                stopVoiceNlp();
                                                JSONObject radio_data = reply.optJSONObject("data");
                                                JSONObject radio_result0 = radio_data.optJSONArray("result").optJSONObject(0);
                                                final String url = radio_result0.optString("url");
                                                audio_type = "radio";
                                                play_url = url;
//                                        new Thread(new Runnable() {
//
//                                            @Override
//                                            public void run() {
//                                                playUrl(url);
//                                            }
//                                        }).start();
                                                String my_text = reply.optString("text");
                                                System.out.println("smartText:" + my_text);
                                                JSONObject answer = reply.optJSONObject("answer");
                                                String answer_text = answer.optString("text");
                                                System.out.println("smartText:" + answer_text);
                                                speakAndDisplay(my_text, answer_text);
                                            }
                                            //电话
                                            else if (service.equals("telephone")) {
                                                stopVoiceNlp();

                                                String my_text = reply.optString("text");
                                                String answer_text = "";
                                                System.out.println("smartText:" + my_text);
                                                if (my_text.equals("睡觉") || my_text.equals("睡眠") || my_text.equals("关闭") || my_text.equals("关机")) {
                                                    issleep = true;
                                                    speakAndDisplayForNext(my_text, "再见", "再见");
                                                } else
                                                if (reply.optJSONObject("answer") != null) {
                                                    JSONObject answer = reply.optJSONObject("answer");
                                                    answer_text = answer.optString("text");
                                                    System.out.println("smartText:" + answer_text);
                                                }
                                                JSONArray semantic = reply.optJSONArray("semantic");
                                                JSONObject semantic0 = semantic.optJSONObject(0);
                                                JSONObject slots0 = semantic0.optJSONArray("slots").optJSONObject(0);
                                                String name = slots0.optString("name");
                                                String value = slots0.optString("value");


                                                System.out.println("smart:" + name + "," + value);
                                                boolean call_b = false;
                                                if (name.equals("name")) {
                                                    String s = reply.optJSONObject("answer").optString("text");
                                                    System.out.println("smartS:" + s);
                                                    if (s.startsWith("没有为您找到")) {
                                                        speakAndDisplayForNext(my_text, answer_text, answer_text);
                                                    } else if (s.indexOf("选择") != -1) {
                                                        JSONArray resultPhone = dataPhone.optJSONArray("result");
                                                        System.out.println("smartPhone:" + resultPhone.toString());
                                                        answer_text = "您好！已为您找到多个" + value + ",分别是：";
                                                        Map<String, String> map = new HashMap<>();
                                                        for (int i = 0; i < resultPhone.length(); i++) {
                                                            String city = "";
                                                            String province = "";
                                                            JSONObject phoneObject = resultPhone.optJSONObject(i);
                                                            JSONObject location = phoneObject.optJSONObject("location");
                                                            if (location != null) {
                                                                city = location.optString("city");
                                                                province = location.optString("province");
                                                            }
                                                            String phoneName = phoneObject.optString("name");
                                                            String phoneNumber = phoneObject.optString("phoneNumber");
                                                            String teleOper = phoneObject.optString("teleOper");
                                                            answer_text = answer_text + "\n" + city + " " + province + " " + teleOper + " " + phoneNumber + " " + phoneName;
                                                        }
                                                        speakAndDisplayForNext(my_text, answer_text, answer_text);
                                                    } else {

                                                        String regEx="[^0-9]";
                                                        Pattern p = Pattern.compile(regEx);
                                                        Matcher m = p.matcher(answer_text);

                                                        call_phone = m.replaceAll("").trim();

//                                                        JSONArray resultPhone = dataPhone.optJSONArray("result");
//                                                        call_phone = resultPhone.optJSONObject(0).optString("phoneNumber");
//                                            for (int i = 0; i < contacts.size(); i++) {
//                                                if (contacts.get(i).get("name").equals(value)) {
//                                                    call_phone = contacts.get(i).get("phone");
//                                                    call_b = true;
//                                                    break;
//                                                }
//                                            }
//                                                        if (!call_phone.equals("")) {
                                                        speakAndDisplayForNext(my_text, answer_text, answer_text);
//                                                        }
                                                    }
                                                } else if (name.equals("code")) {
                                                    call_phone = value;
                                                    speakAndDisplayForNext(my_text, answer_text, answer_text);
                                                } else if (name.equals("insType")) {
                                                    if (value.equals("CONFIRM")) {
                                                        audio_type = "telephone";
                                                        System.out.println("smart拨通:" + call_phone);
                                                        answer_text = "正在接通，请稍后.....";
                                                        speakAndDisplay(my_text, answer_text);
                                                    }
                                                } else if (name.equals("headNum") || name.equals("location")) {
                                                    speakAndDisplayForNext(my_text, answer_text, answer_text);
                                                }
                                            } else if (service.equals("iFlytekQA")){
                                                stopVoiceNlp();
                                                String my_text = reply.optString("text");

                                                if (my_text.equals("关闭")){
                                                    issleep = true;
                                                    speakAndDisplayForNext(my_text, "再见", "再见");
                                                } else{
                                                    System.out.println("smartText:" + my_text);
                                                    JSONObject answer = reply.optJSONObject("answer");
                                                    String answer_text = answer.optString("text");
                                                    System.out.println("smartText:" + answer_text);
                                                    speakAndDisplayForNext(my_text, answer_text, answer_text);
                                                }

                                            } else {
                                                //tvApp.start_Wake();
                                            }

                                        } else {
//

                                        }

                                    }
                                    ismusic = false;
                                    voice = true;
                                    voice_button.setBackgroundResource(R.drawable.btn_startrecord);
                                    if (mMediaRecorder != null) {
                                        mMediaRecorder.stop();
                                        mMediaRecorder.release();
                                    }
                                    mMediaRecorder = null;
                                }
//                            play();
                            }
                        }
                    } catch (JSONException e) {
                        isjennry = false;
                        tvApp.stop_wake();
                        startVoiceNlp();
                        System.out.println("JSONException11");
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        isjennry = false;
                        tvApp.stop_wake();
                        startVoiceNlp();
                        System.out.println("JSONException22");
                        e.printStackTrace();
                    } catch (Exception e) {
                        isjennry = false;
                        tvApp.stop_wake();
                        startVoiceNlp();
                        System.out.println("JSONException33");
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private int getWindIndexByWindCode(final int code) {
        for (int i = 0; i < mWinds.length; i++) {
            if (mWinds[i] == code) {
                return i;
            }
        }
        return 0;
    }

    private boolean isNumber(String cardNum) {
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        Matcher matcher = pattern.matcher((CharSequence) cardNum);
        boolean result = matcher.matches();
        return result;
    }

    private HashMap<String, Long> chineseNumbers = new HashMap<String, Long>() {  // java verbosity. compare this to python's dict
        private final long serialVersionUID = 1L;

        {
            put("零", 0L);
            put("一", 1L);
            put("壹", 1L);
            put("二", 2L);
            put("两", 2L);
            put("貳", 2L);
            put("贰", 2L);
            put("叁", 3L);
            put("參", 3L);
            put("三", 3L);
            put("肆", 4L);
            put("四", 4L);
            put("五", 5L);
            put("伍", 5L);
            put("陸", 6L);
            put("陆", 6L);
            put("六", 6L);
            put("柒", 7L);
            put("七", 7L);
            put("捌", 8L);
            put("八", 8L);
            put("九", 9L);
            put("玖", 9L);
            put("十", 10L);
            put("拾", 10L);
            put("佰", 100L);
            put("百", 100L);
        }
    };

    private long convert(String s) {
        int sLen = s.length();
        if (sLen == 0)
            return 0;
        if (sLen > 1) {
            int pivot = 0; // index of the highest singular character value in the string
            for (int i = 0; i < sLen; i++)   // loop through the characters in the string to get the character with the highest value. That is your pivot
                if (convert(String.valueOf(s.charAt(i))) > convert(String.valueOf(s.charAt(pivot))))
                    pivot = i;
            long value = convert(String.valueOf(s.charAt(pivot)));
            long LHS, RHS;
            LHS = convert(s.substring(0, pivot));  // multiply value with LHS
            RHS = convert(s.substring(pivot + 1));  // add value with RHS
            if (LHS > 0)
                value *= LHS;
            value += RHS;
            return value;
        } else {
            return chineseNumbers.get(s).longValue();
        }
    }


    private Set<PhoneList> contacts;

    class PhoneList {
        String name = "";
        String phone = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        @Override
        public String toString() {
            return name + phone;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o != null && o instanceof PhoneList) {
                if (((PhoneList) o).getName().equals(this.name) && ((PhoneList) o).getPhone().equals(this.phone)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    private void readContacts() {
        // 首先，从raw_contacts中读取联系人的id（"contact_id")
        // 其次，根据contact_id从data表中查询出相应的电话号码和联系人名称
        // 然后，根据mimetype来区分哪个是联系人，哪个是电话号码
        // Uri rawContactsUri = Uri
        // .parse("content://com.android.contacts/raw_contacts");

        contacts = new HashSet<>();
        contacts.hashCode();
        HashSet<String> s = new HashSet<>();
        ContentResolver resolver = getContentResolver();
        // 获取联系人表对应的内容提供者url raw_contacts表和data表
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri datauri = Uri.parse("content://com.android.contacts/data");

        Cursor rawContactsCursor = resolver.query(uri, new String[]{"contact_id"},
                null, null, null);

        if (rawContactsCursor != null) {
            while (rawContactsCursor.moveToNext()) {
                String contactId = rawContactsCursor.getString(0);
                // System.out.println(contactId);
                if (contactId != null) { // 曾经有过，已经删除的联系人在raw_contacts表中记录仍在，但contact_id值为null
                    // 根据contact_id从data表中查询出相应的电话号码和联系人名称
                    // Uri dataUri = Uri
                    // .parse("content://com.android.contacts/data");
                    Cursor dataCursor = getContentResolver().query(
                            datauri,
                            new String[]{"data1", "mimetype"},
                            "contact_id=?", new String[]{contactId}, null);

                    if (dataCursor != null) {
                        PhoneList map = new PhoneList();
                        while (dataCursor.moveToNext()) {

                            String data1 = dataCursor.getString(0);
                            String mimetype = dataCursor.getString(1);
                            System.out.println(data1 + ";" + mimetype);

                            if ("vnd.android.cursor.item/phone_v2"
                                    .equals(mimetype))
//                                map.put("phone", data1);

                                map.setPhone(data1.replace(" ", ""));
                            else if ("vnd.android.cursor.item/name"
                                    .equals(mimetype))
//                                map.put("name", data1);
                                map.setName(data1);

                        }
                        contacts.add(map);
                    }
                    dataCursor.close();
                }
            }
            rawContactsCursor.close();
        }
        getcontactlist();
    }

    private void speakEnAndDisplay(String my_text, String answer_text) {
        PersonChat personChat = new PersonChat();
        //代表自己发送
        personChat.setMeSend(true);
        //得到发送内容
        personChat.setChatMessage("" + my_text);

        PersonChat answerChat = new PersonChat();
        //代表自己发送
        answerChat.setMeSend(false);
        //得到发送内容
        answerChat.setChatMessage("" + answer_text);
        //加入集合
        personChats.add(personChat);
        personChats.add(answerChat);
        //清空输入框
        //刷新ListView
        chatAdapter.notifyDataSetChanged();
        handler.sendEmptyMessage(1);
        FlowerCollector.onEvent(SmartInteractive.this, "tts_play");
        setParam("catherine");
        mTts.startSpeaking(answer_text, NextmTtsListener);
        tvApp.start_Wake();
    }

    private void speakAndDisplayForWake(String my_text, String answer_text) {
        PersonChat personChat = new PersonChat();
        //代表自己发送
        personChat.setMeSend(true);
        //得到发送内容
        personChat.setChatMessage("" + my_text);

        PersonChat answerChat = new PersonChat();
        //代表自己发送
        answerChat.setMeSend(false);
        //得到发送内容
        answerChat.setChatMessage("" + answer_text);
        //加入集合
        personChats.add(personChat);
        personChats.add(answerChat);
        //清空输入框
        //刷新ListView
        chatAdapter.notifyDataSetChanged();
        handler.sendEmptyMessage(1);
        FlowerCollector.onEvent(SmartInteractive.this, "tts_play");
        setParam("xiaoyan");
        mTts.startSpeaking(answer_text, mTtsListener);
    }

    private void speakAndDisplayForNext(String my_text, String answer_text, String showtext) {
        PersonChat personChat = new PersonChat();
        //代表自己发送
        personChat.setMeSend(true);
        //得到发送内容
        personChat.setChatMessage("" + my_text);

        PersonChat answerChat = new PersonChat();
        //代表自己发送
        answerChat.setMeSend(false);
        //得到发送内容
        answerChat.setChatMessage("" + showtext);
        //加入集合
        personChats.add(personChat);
        personChats.add(answerChat);
        //清空输入框
        //刷新ListView
        chatAdapter.notifyDataSetChanged();
        handler.sendEmptyMessage(1);
        FlowerCollector.onEvent(SmartInteractive.this, "tts_play");
        setParam("xiaoyan");
        mTts.startSpeaking(answer_text, NextmTtsListener);
        trainlist.clear();
        fightlist.clear();
        tvApp.start_Wake();
    }


    private void speakAndDisplay(String my_text, String answer_text) {
        PersonChat personChat = new PersonChat();
        //代表自己发送
        personChat.setMeSend(true);
        //得到发送内容
        personChat.setChatMessage("" + my_text);

        PersonChat answerChat = new PersonChat();
        //代表自己发送
        answerChat.setMeSend(false);
        //得到发送内容
        answerChat.setChatMessage("" + answer_text);
        //加入集合
        personChats.add(personChat);
        personChats.add(answerChat);
        //清空输入框
        //刷新ListView
        chatAdapter.notifyDataSetChanged();
        handler.sendEmptyMessage(1);
        FlowerCollector.onEvent(SmartInteractive.this, "tts_play");
        setParam("xiaoyan");
        mTts.startSpeaking(answer_text, mTtsListener);
        tvApp.start_Wake();
    }

    private void Display(String my_text, String answer_text) {
        PersonChat personChat = new PersonChat();
        //代表自己发送
        personChat.setMeSend(true);
        //得到发送内容
        personChat.setChatMessage("" + my_text);

        PersonChat answerChat = new PersonChat();
        //代表自己发送
        answerChat.setMeSend(false);
        //得到发送内容
        answerChat.setChatMessage("" + answer_text);
        //加入集合
        personChats.add(personChat);
        personChats.add(answerChat);
        //清空输入框
        //刷新ListView
        chatAdapter.notifyDataSetChanged();
        handler.sendEmptyMessage(1);
        tvApp.start_Wake();
    }

    private SynthesizerListener mTtsForWakeListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
//            showTip("开始播放");
            System.out.println("smart开始播放mTtsForWakeListener");
        }

        @Override
        public void onSpeakPaused() {
//            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
//            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
//            mPercentForBuffering = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
//            mPercentForPlaying = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            isjennry = false;
            System.out.println("smart播放完成");
            //新增
            tvApp.stop_wake();
            startVoiceNlp();
            voice = false;
            voice_button.setBackgroundResource(R.drawable.btn_stoprecord);
            pause();
//            if (error == null) {
//                showTip("播放完成");
//            } else if (error != null) {
//                showTip(error.getPlainDescription(true));
//            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private SynthesizerListener NextmTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
//            showTip("开始播放");
            System.out.println("smart开始播放NextmTtsListener");
        }

        @Override
        public void onSpeakPaused() {
//            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
//            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
//            mPercentForBuffering = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
//            mPercentForPlaying = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            System.out.println("smart播放完成");
            isjennry = false;
//            mApplication.start_Wake();
            //新增
            tvApp.stop_wake();
            startVoiceNlp();
            voice = false;
            voice_button.setBackgroundResource(R.drawable.btn_stoprecord);
            pause();
            if (issleep) {
                finish();
            }
            issleep = false;
//            if (error == null) {
//                showTip("播放完成");
//            } else if (error != null) {
//                showTip(error.getPlainDescription(true));
//            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private boolean isjennry = false;
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
//            showTip("开始播放");
            System.out.println("smart开始播放mTtsListener");
        }

        @Override
        public void onSpeakPaused() {
//            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
//            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
//            mPercentForBuffering = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
//            mPercentForPlaying = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            System.out.println("smart播放完成");
            tvApp.stop_wake();

            System.out.println("smart：" + call_phone+"|"+audio_type);
            if (audio_type.equals("music") || audio_type.equals("joke") || audio_type.equals("story") || audio_type.equals("radio") || audio_type.equals("news")) {

//                new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (!play_url.equals(""))
                isjennry = true;
                startVoiceNlp();
                voice = false;
                voice_button.setBackgroundResource(R.drawable.btn_stoprecord);

                System.out.println("smarthhhhh：" + play_url);
                if (!play_url.equals("")) {
                    playUrl(play_url);
                }
                musictype = 0;
                value = "";

//                    }
//                }).start();
            } else if (audio_type.equals("telephone")) {
              /*  Intent call = new Intent();
                call.setAction("android.intent.action.CALL");
                call.addCategory(Intent.CATEGORY_DEFAULT);
                call.setData(Uri.parse("tel:" + call_phone));
                startActivity(call);
                call_phone = "";*/
                isjennry = false;
                // startVoiceNlp();
                voice = false;
                voice_button.setBackgroundResource(R.drawable.btn_stoprecord);
                System.out.println("callttphone" + call_phone);
                ICmccManager.getInstance().actionStartImsAudio(SmartInteractive.this, "0" + call_phone.toString());
                call_phone = "";
            }
            audio_type = "";
            play_url = "";
//            mApplication.start_Wake();
//            if (error == null) {
//                showTip("播放完成");
//            } else if (error != null) {
//                showTip(error.getPlainDescription(true));
//            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private String getAIUIParams() {
        String params = "";

        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
            Log.i("paramss", params);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(params);
            JSONObject jsonObject1 = new JSONObject();
            String latitude = getSharedPreferences("latitude", MODE_PRIVATE).getString("latitude", "");
            String longitude = getSharedPreferences("latitude", MODE_PRIVATE).getString("longitude", "");
            jsonObject1.put("msc.lng", longitude);
            jsonObject1.put("msc.lat", latitude);
            jsonObject.put("audioparams", jsonObject1);
            params = jsonObject.toString();
            System.out.println("jsonbojjjedt" + jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params;
    }

    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private void setParam(String name) {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, name);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, "40");
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, "50");
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, "50");
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    private void initial() {
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "hello.log");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        mMediaRecorder.setMaxDuration(1000 * 60 * 10);
    }

    BroadcastReceiver WakeUpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ConnectivityManager.EXTRA_REASON)){
                if (intent.getExtras().getBoolean("wake_up")) {
                    tvApp.stop_wake();
                    if (mTts != null && mTts.isSpeaking()) {
                        mTts.stopSpeaking();
                    }
                    FlowerCollector.onEvent(SmartInteractive.this, "tts_play");
                    setParam("xiaoyan");
                    mTts.startSpeaking("我在呢", mTtsForWakeListener);
                    stopVoiceNlp();
                }
            }

            if (action.equals("HangUp")){
                startVoiceNlp();
            }

        }
    };

    @Override
    protected void onRestart() {
        tvApp.stop_wake();
        startVoiceNlp();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        stop();
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
        isAlive = false;
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        Log.e(TAG, "onDestroy: 3333");
        tvApp.start_Wake();
        if (null != this.mAIUIAgent) {
            AIUIMessage stopMsg = new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, null, null);
            mAIUIAgent.sendMessage(stopMsg);

            this.mAIUIAgent.destroy();
            this.mAIUIAgent = null;
        }
        WakeUp = false;

        unregisterReceiver(WakeUpReceiver);

        super.onDestroy();
    }

    @Override
    public void run() {
        while (isAlive) {

            handler2.sendEmptyMessage(0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        tvApp.stop_wake();
        startVoiceNlp();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        stopVoiceNlp();
        tvApp.start_Wake();
        mp.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void getcontactlist() {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://222.46.28.149:8181/work/yunguhua/interface.php?a=getPhoneList&landline=53158021024")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {//回调的方法执行在子线程。
                    Log.d("kwwl", "获取数据成功了");
                    Log.d("kwwl", "response.code()==" + response.code());
                    String phonelist = response.body().string();
                    //Log.d("kwwl","response.body().string()=="+phonelist);

                    Log.d("kwwllll", "response.code()==" + phonelist);
                    try {
                        JSONObject jsonobject = new JSONObject(phonelist);
                        JSONObject jsonObject1 = jsonobject.optJSONObject("data");
                        JSONArray jsonArray = jsonObject1.optJSONArray("list");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            PhoneList person = new PhoneList();
                            person.setName(jsonObject.optString("name"));
                            person.setPhone(jsonObject.optString("phone"));
                            contacts.add(person);
                        }
                    } catch (Exception e) {
                        System.out.println("e.toooooo" + e.toString());

                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SmartInteractive.this, "获取联系人失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                postNameForAIUI();
            }
        });
    }

    private void findmusic(String url) {
        final String path = url;
        Thread t = new Thread() {
            @Override
            public void run() {
                //使用网址构造url
                URL url;
                try {
                    url = new URL(path);
                    //获取连接对象，做设置
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    //发送请求，获取响应码
                    if (conn.getResponseCode() == 200) {
                        //获取服务器返回的输入流
                        InputStream is = conn.getInputStream();
                        String text = Tools.getTextFromStream(is);
                        Log.e("ffttfang", text);
                        JSONObject jsonObject = new JSONObject(text);
                        String error_code = jsonObject.optString("error_code");
                        if (error_code.equals("22000")) {
                            JSONArray jsonArray = jsonObject.optJSONArray("song");
                            if (jsonArray.length() > 0) {
                                String songid = jsonArray.getJSONObject(0).optString("songid");
                                value = value + "的" + jsonArray.getJSONObject(0).optString("songname");
                                //发送消息至消息队列，主线程会执行handleMessage
                                Message msg = Message.obtain();
                                msg.obj = songid;
                                msg.what = 3;
                                handler.sendMessage(msg);
                            }
                        } else {
                            musictype = 3;
                            Message msg = Message.obtain();
                            msg.what = 4;
                            handler.sendMessage(msg);
                        }

                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void findmoremusic(String url, final String song) {
        final String path = url;
        Thread t = new Thread() {
            @Override
            public void run() {
                //使用网址构造url
                URL url;
                try {
                    url = new URL(path);
                    //获取连接对象，做设置
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    //发送请求，获取响应码
                    if (conn.getResponseCode() == 200) {
                        //获取服务器返回的输入流
                        InputStream is = conn.getInputStream();
                        String text = Tools.getTextFromStream(is);
                        Log.e("ffttfang", text);
                        JSONObject jsonObject = new JSONObject(text);
                        String error_code = jsonObject.optString("error_code");
                        if (error_code.equals("22000")) {
                            JSONArray jsonArray = jsonObject.optJSONArray("song");
                            boolean m = false;
                            if (jsonArray.length() > 0) {
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    String songname = jsonArray.getJSONObject(j).optString("songname");
                                    songid = jsonArray.getJSONObject(j).optString("songid");
                                    if (song.equals(songname)) {
                                        m = true;
                                        break;
                                    }
                                }
                            }
                            if (m) {
                                musictype = 2;
                                Message msg = Message.obtain();
                                msg.obj = songid;
                                msg.what = 3;
                                handler.sendMessage(msg);
                            } else {
                                musictype = 3;
                                Message msg = Message.obtain();
                                msg.what = 4;
                                handler.sendMessage(msg);
                            }
                        } else {
                            musictype = 3;
                            Message msg = Message.obtain();
                            msg.what = 4;
                            handler.sendMessage(msg);
                        }


                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void playmusic(String url) {
        final String path = url;
        Thread t = new Thread() {
            @Override
            public void run() {
                //使用网址构造url
                URL url;
                try {
                    url = new URL(path);
                    //获取连接对象，做设置
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    //发送请求，获取响应码
                    if (conn.getResponseCode() == 200) {
                        //获取服务器返回的输入流
                        InputStream is = conn.getInputStream();
                        String text = Tools.getTextFromStream(is);
                        Log.e("ffttfang22", text);
                        JSONObject jsonObject = new JSONObject(text);
                        JSONObject jsonObject1 = jsonObject.optJSONObject("bitrate");

                        String playurl = jsonObject1.optString("show_link");
                        //发送消息至消息队列，主线程会执行handleMessage
                        Message msg = Message.obtain();
                        msg.obj = playurl;
                        msg.what = 4;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private String getLngAndLat(Context context) {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {  //从gps获取经纬度
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {//当GPS信号弱没获取到位置的时候又从网络获取
                return getLngAndLatWithNetwork();
            }
        } else {    //从网络获取经纬度
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }
        return longitude + "," + latitude;
    }

    //从网络获取经纬度
    public String getLngAndLatWithNetwork() {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        return longitude + "," + latitude;
    }

    LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {

        }

    };
}
