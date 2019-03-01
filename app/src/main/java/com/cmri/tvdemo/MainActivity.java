package com.cmri.tvdemo;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.cmri.moudleapp.moudlevoip.ICmccManager;
import com.cmri.moudleapp.moudlevoip.utils.CommonResource;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import coap.AndLinkServer;
import coap.WifiAdmin;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private RadioGroup radioGroupMode;

    public LocationClient mLocationClient = null;

    private MyLocationListener myListener = new MyLocationListener();

    private WifiManager my_wifiManager;
    private String gwIP;

    private String SSID;
    private String password;

    private WifiAdmin wifiAdmin;

    private Timer mReshDeviceListTimer;

    private Timer configTimer;

    private String WifiConfig = "";

    private boolean config = false;

    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("panhouye")){
                Intent intent1 = new Intent(MainActivity.this, ImsCall1Activity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }

        if (intent.getAction().equals("ANDLINK.searchack")) {
            gwIP = intent.getStringExtra("gwIP");

            System.out.println("ANDLINK.searchack:"+gwIP);

//            text1.setText(gwIP);

            JSONObject object = new JSONObject();

            try {
                object.put("deviceMac", ""+getMacAddressFromIp(MainActivity.this));
                object.put("deviceType", "30103");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println("object:" + object.toString());

            System.out.println("coap://"+gwIP+":5683/qlink/request");

            CoapClient client = new CoapClient("coap://"+gwIP+":5683/qlink/request");

            client.useCONs();

            client.post(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    System.out.println("request:" + coapResponse.getResponseText());
                }

                @Override
                public void onError() {
                    System.out.println("失败");
                }
            }, object.toString(), MediaTypeRegistry.APPLICATION_JSON);
        } else if (intent.getAction().equals("ANDLINK.netinfo")){
            String WiFiConfig = intent.getStringExtra("WiFiConfig");
//            text2.setText(WiFiConfig);
            gwIP = intent.getStringExtra("gwIP");
            System.out.println("ANDLINK.netinfo:"+WiFiConfig);
            if (!my_wifiManager.isWifiEnabled()){
                wifiAdmin.openWifi();
            }

            try {
                JSONObject object = new JSONObject(WiFiConfig);
                SSID = object.optString("SSID");
                password = object.optString("password");
                String encrypt = object.optString("encrypt");
                int type = 99;
                if (encrypt.indexOf("WPA") != -1){
                    type = 3;
                } else if (encrypt.indexOf("OPEN") != -1){
                    type = 1;
                }
                wifiAdmin.CreateWifiInfo(SSID,password,type);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiInfo = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo!=null) {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            String wifiSSID = wifiManager.getConnectionInfo().getSSID().replace("\"","");
            System.out.println("getSSID:"+wifiSSID +","+SSID);
            if (wifiSSID.equals(SSID)){
                System.out.println("SSIDTrue");
                Message message = new Message();
                Bundle data = new Bundle();
                data.putInt("config",1);
                message.setData(data);
                handler.sendMessage(message);
            }

            if (!WifiConfig.equals("") && !wifiSSID.equals(SSID)){
                try {
                    JSONObject object = new JSONObject(WifiConfig);
                    SSID = object.optString("SSID");
                    password = object.optString("password");
                    String encrypt = object.optString("encrypt");
                    int type = 99;
                    if (encrypt.indexOf("WPA") != -1){
                        type = 3;
                    } else if (encrypt.indexOf("OPEN") != -1){
                        type = 1;
                    }
                    wifiAdmin.CreateWifiInfo(SSID,password,type);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }


    }
};


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int val = data.getInt("config");
            Log.i(TAG,"请求结果:" + val);
            if (val == 1){

                JSONObject object = new JSONObject();

                try {
                    object.put("deviceMac", "8A:A3:51:34:04:F4");
                    object.put("deviceType", "30103");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (gwIP != null && !gwIP.equals("") && !gwIP.equals("null")){
                    System.out.println("object:" + object.toString());

                    System.out.println("coap://"+gwIP+":5683/qlink/success");

                    CoapClient client = new CoapClient("coap://"+gwIP+":5683/qlink/success");

                    gwIP = "";

                    client.useCONs();

                    client.post(new CoapHandler() {
                        @Override
                        public void onLoad(CoapResponse coapResponse) {
//                            text3.setText("连接AndLink成功");
                            System.out.println("success:" + coapResponse.getResponseText());
                            if (isServiceRunning(MainActivity.this,"com.zhb.coap.AndLinkServer")){
                                System.out.println("isServiceRunning");
                            } else {
                                System.out.println("ServiceNotRunning");
                                Intent intent = new Intent(MainActivity.this, AndLinkServer.class);
                                startService(intent);
                            }
                        }

                        @Override
                        public void onError() {
                            System.out.println("失败");
                        }
                    }, object.toString(), MediaTypeRegistry.APPLICATION_JSON);
                }

            }
        }
    };


    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }


    private String getMacAddressFromIp(Context context) {
        String mac_s= "";
        StringBuilder buf = new StringBuilder();
        try {
            byte[] mac;
            NetworkInterface ne=NetworkInterface.getByInetAddress(InetAddress.getByName(getIpAddress(context)));
            mac = ne.getHardwareAddress();
            for (byte b : mac) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            mac_s = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mac_s;
    }

    private String getIpAddress(Context context){
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            // 3/4g网络
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                //  wifi网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                return ipAddress;
            }  else if (info.getType() == ConnectivityManager.TYPE_ETHERNET){
                // 有限网络
                return getLocalIp();
            }
        }
        return null;
    }


    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    // 获取有限网IP
    private static String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {

        }
        return "0.0.0.0";

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiAdmin = new WifiAdmin(MainActivity.this);

        my_wifiManager = ((WifiManager) getSystemService("wifi"));

        SharedPreferences sp = getSharedPreferences("AndLink",Context.MODE_PRIVATE);

        WifiConfig = sp.getString("WiFiConfig","");

        gwIP = sp.getString("gwIP","");

        IntentFilter filter = new IntentFilter();
        filter.addAction("panhouye");
        filter.addAction("ANDLINK.searchack");
        filter.addAction("ANDLINK.netinfo");
        registerReceiver(broadcastReceiver,filter);
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        LocationClientOption option=new LocationClientOption();
        option.setIgnoreKillProcess(false);
    //可选，定位SDK内部是一个service，并放到了独立进程。
    //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
    //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5*60*1000);
    //可选，7.2版本新增能力
    //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        option.setEnableSimulateGps(false);
    //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocationClient.setLocOption(option);
        mLocationClient.start();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        System.out.println("ffessssf"+displayMetrics.widthPixels+".."+displayMetrics.heightPixels+".."+displayMetrics.densityDpi+".."+displayMetrics.density+"..."+displayMetrics.xdpi+"..."+displayMetrics.ydpi);

    }

    public void actionInitVoip(View view) {
        ICmccManager.getInstance().initCmcc(this, true);
    }


    public void startOttPage(View view) {
        startActivity(new Intent(this, OttActivity.class));
    }

    public void actionloginin(View view) {
        ICmccManager.getInstance().loginCmccIms(this,
                "",
                "ims.sd.chinamobile.com",
                "223.99.141.165",
                6000 ,
                "+8653158021024" ,
                "+8653158021024",
                "+8653158021024@ims.sd.chinamobile.com",
                "a7d6wU9Jr5LK7SI",false);

    }
    public void actionloginout(View view) {
        ICmccManager.getInstance().logoutCmcc(this);
      // finish();
    }

    public void wifiConfig(View view) {

        config = true;

        WifiConfig = "";
        SSID = "";

        SharedPreferences sp2 = getSharedPreferences("AndLink",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sp2.edit();
        editor2.putString("WiFiConfig","");
        editor2.putString("gwIP","");
        editor2.commit();

        Toast.makeText(MainActivity.this,"开始连接CMCC-QLINK",Toast.LENGTH_SHORT).show();

        if (mReshDeviceListTimer == null) {
            mReshDeviceListTimer = new Timer();
            mReshDeviceListTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    System.out.println("获取wifi："+getlocalip() + my_wifiManager.getConnectionInfo().getSSID().replace("\"",""));
                    if (my_wifiManager.getConnectionInfo().getSSID().replace("\"","").equals("CMCC-QLINK") && !getlocalip().equals("未连接wifi")){
                        mReshDeviceListTimer.cancel();
                        mReshDeviceListTimer = null;
                        System.out.println("关闭");
                        Intent intnet = new Intent(MainActivity.this, AndLinkServer.class);
                        if (isServiceRunning(MainActivity.this,"com.zhb.coap.AndLinkServer")){
                            System.out.println("isServiceRunning");
//                                    stopService(intnet);
                        }

                        startService(intnet);

                        String broadCast2 = null;

                        try {
                            broadCast2 = getBroadcast();
                        } catch (SocketException e) {
                            e.printStackTrace();
                        }

                        if (broadCast2 != null){
                            JSONObject object = new JSONObject();

                            try {
                                object.put("searchKey", "ANDLINK-DEVICE");
                                object.put("andlinkVersion", "V3");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            System.out.println("coap://" + broadCast2 + ":5683/qlink/searchgw");

                            CoapClient client = new CoapClient("coap://" + broadCast2 + ":5683/qlink/searchgw");

                            client.useNONs();

                            client.post(object.toString(), MediaTypeRegistry.APPLICATION_JSON, 0);
                        }

                    } else {
                        System.out.println("循环连接");
                        wifiAdmin.CreateWifiInfo("CMCC-QLINK","",1);

                    }

                }
            }, 300, 10000);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       /* ICmccManager.getInstance().logoutCmcc(this);
        ICmccManager.getInstance().destroyCmcc();*/

        unregisterReceiver(broadcastReceiver);
        ICmccManager.getInstance().logoutCmcc(this);
        System.out.println("onDestroy3");
        System.exit(0);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      /*  unregisterReceiver(broadcastReceiver);
       ICmccManager.getInstance().logoutCmcc(this);*/
      // ICmccManager.getInstance().destroyCmcc();
    }

    public void actionDestroy(View view) {
        ICmccManager.getInstance().destroyCmcc();
    }
     class MyLocationListener extends BDAbstractLocationListener {
         @Override
         public void onReceiveLocation(BDLocation location) {
             //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
             //以下只列举部分获取经纬度相关（常用）的结果信息
             //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

             double latitude = location.getLatitude();    //获取纬度信息
             double longitude = location.getLongitude();    //获取经度信息
             float radius = location.getRadius();    //获取定位精度，默认值为0.0f
             getSharedPreferences("latitude",MODE_PRIVATE).edit().putString("latitude",String.valueOf(latitude)).commit();
             getSharedPreferences("latitude",MODE_PRIVATE).edit().putString("longitude",String.valueOf(longitude)).commit();
            System.out.println(latitude+"..."+longitude+"llffffffffffff");
             String coorType = location.getCoorType();
             //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

             int errorCode = location.getLocType();
             //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
         }
     }

    public static String getBroadcast() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements(); ) {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback()) {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                    if (interfaceAddress.getBroadcast() != null) {
                        return interfaceAddress.getBroadcast().toString().substring(1);
                    }
                }
            }
        }
        return null;
    }


    private String getlocalip() {
        return "";
//        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        int ipAddress = wifiInfo.getIpAddress();
//        if (ipAddress == 0) return "未连接wifi";
//        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
//                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }
}
