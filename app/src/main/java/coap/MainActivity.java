package coap;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cmri.tvdemo.R;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyRep;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity{

//    private WifiInfo wifiInfo;
//    private DhcpInfo dhcpInfo;
//
//    private WifiManager my_wifiManager;
//    private String gwIP;
//
//    private String SSID;
//    private String password;
//
//    private WifiAdmin wifiAdmin;
//
//    private TextView text1,text2,text3;
//
//    private ANDLINKBroadcastReceiver mANDLINK = new ANDLINKBroadcastReceiver();
//
//    private Timer mReshDeviceListTimer;
//
//    private String WifiConfig = "";
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//
//        wifiAdmin = new WifiAdmin(MainActivity.this);
//
//        my_wifiManager = ((WifiManager) getSystemService("wifi"));
//
//        wifiAdmin.openWifi();
//
//        dhcpInfo = my_wifiManager.getDhcpInfo();
//        wifiInfo = my_wifiManager.getConnectionInfo();
//
//        text1 = (TextView) findViewById(R.id.text1);
//        text2 = (TextView) findViewById(R.id.text2);
//        text3 = (TextView) findViewById(R.id.text3);
//        SharedPreferences sp = getSharedPreferences("AndLink",Context.MODE_PRIVATE);
//
//        WifiConfig = sp.getString("WiFiConfig","");
//
//        gwIP = sp.getString("gwIP","");
//
////        if (!WifiConfig.equals("")){
////
////        }
//
////        System.out.println(intToIp(dhcpInfo.ipAddress));
////        System.out.println(intToIp(dhcpInfo.netmask));
////        System.out.println(intToIp(dhcpInfo.gateway));
////        System.out.println(intToIp(dhcpInfo.serverAddress));
////        System.out.println(intToIp(dhcpInfo.dns1));
////        System.out.println(intToIp(dhcpInfo.dns2));
////        System.out.println(dhcpInfo.leaseDuration);
//
////        System.out.println("mac:");
//
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("ANDLINK.searchack");
//        intentFilter.addAction("ANDLINK.netinfo");
//        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        registerReceiver(mANDLINK,intentFilter);
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unregisterReceiver(mANDLINK);
//    }
//
//    public static boolean isServiceRunning(Context context, String ServiceName) {
//        if (("").equals(ServiceName) || ServiceName == null)
//            return false;
//        ActivityManager myManager = (ActivityManager) context
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
//                .getRunningServices(30);
//        for (int i = 0; i < runningService.size(); i++) {
//            if (runningService.get(i).service.getClassName().toString()
//                    .equals(ServiceName)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
////    private String intToIp(int paramInt) {
////        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
////                + (0xFF & paramInt >> 24);
////    }
//
//    public void onClick(View v) {
//        switch (v.getId()) {
//
//            case R.id.request:
//                WifiConfig = "";
//                SSID = "";
//
//                SharedPreferences sp = getSharedPreferences("AndLink",Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sp.edit();
//                editor.putString("WiFiConfig","");
//                editor.putString("gwIP","");
//                editor.commit();
//
//                Toast.makeText(MainActivity.this,"开始连接CMCC-QLINK",Toast.LENGTH_SHORT).show();
//
//                if (mReshDeviceListTimer == null) {
//                    mReshDeviceListTimer = new Timer();
//                    mReshDeviceListTimer.schedule(new TimerTask() {
//
//                        @Override
//                        public void run() {
//                            System.out.println("获取wifi："+getlocalip() + my_wifiManager.getConnectionInfo().getSSID().replace("\"",""));
//                            if (my_wifiManager.getConnectionInfo().getSSID().replace("\"","").equals("CMCC-QLINK") && !getlocalip().equals("未连接wifi")){
//                                mReshDeviceListTimer.cancel();
//                                mReshDeviceListTimer = null;
//                                System.out.println("关闭");
//                                Intent intnet = new Intent(MainActivity.this, AndLinkServer.class);
//                                if (isServiceRunning(MainActivity.this,"com.zhb.coap.AndLinkServer")){
//                                    System.out.println("isServiceRunning");
////                                    stopService(intnet);
//                                }
//
//                                startService(intnet);
//
//                                String broadCast2 = null;
//
//                                try {
//                                    broadCast2 = getBroadcast();
//                                } catch (SocketException e) {
//                                    e.printStackTrace();
//                                }
//
//                                if (broadCast2 != null){
//                                    JSONObject object = new JSONObject();
//
//                                    try {
//                                        object.put("searchKey", "ANDLINK-DEVICE");
//                                        object.put("andlinkVersion", "V3");
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    System.out.println("coap://" + broadCast2 + ":5683/qlink/searchgw");
//
//                                    CoapClient client = new CoapClient("coap://" + broadCast2 + ":5683/qlink/searchgw");
//
//                                    client.useNONs();
//
//                                    client.post(object.toString(), MediaTypeRegistry.APPLICATION_JSON, 0);
//                                }
//
//                            } else {
//                                System.out.println("循环连接");
//                                wifiAdmin.CreateWifiInfo("CMCC-QLINK","",1);
//
//                            }
//
//                        }
//                    }, 300, 10000);
//                }
//
////			send(new CmdPlay())
//
////			printIpAddressAndSubnettest();
////                String broadCast1 = null;
////                try {
////                    broadCast1 = getBroadcast();
////                } catch (SocketException e) {
////                    e.printStackTrace();
////                }
//
//
//
//                break;
//
//            case R.id.SearchGW:
//
//                WifiConfig = "";
//                SSID = "";
//
//                SharedPreferences sp2 = getSharedPreferences("AndLink",Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor2 = sp2.edit();
//                editor2.putString("WiFiConfig","");
//                editor2.putString("gwIP","");
//                editor2.commit();
//
////                Intent intnet = new Intent(MainActivity.this, AndLinkServer.class);
////                if (isServiceRunning(MainActivity.this,"com.zhb.coap.AndLinkServer")){
////                    System.out.println("isServiceRunning");
//////                                    stopService(intnet);
////                }
////
////                startService(intnet);
//
//                break;
//
//            case R.id.ServicePlay:
//                break;
//        }
//
//    }
//
//    private String getMacAddressFromIp(Context context) {
//        String mac_s= "";
//        StringBuilder buf = new StringBuilder();
//        try {
//            byte[] mac;
//            NetworkInterface ne=NetworkInterface.getByInetAddress(InetAddress.getByName(getIpAddress(context)));
//            mac = ne.getHardwareAddress();
//            for (byte b : mac) {
//                buf.append(String.format("%02X:", b));
//            }
//            if (buf.length() > 0) {
//                buf.deleteCharAt(buf.length() - 1);
//            }
//            mac_s = buf.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return mac_s;
//    }
//
//    private String getIpAddress(Context context){
//        NetworkInfo info = ((ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//        if (info != null && info.isConnected()) {
//            // 3/4g网络
//            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
//                try {
//                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
//                        NetworkInterface intf = en.nextElement();
//                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                            InetAddress inetAddress = enumIpAddr.nextElement();
//                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
//                                return inetAddress.getHostAddress();
//                            }
//                        }
//                    }
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
//
//            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
//                //  wifi网络
//                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
//                return ipAddress;
//            }  else if (info.getType() == ConnectivityManager.TYPE_ETHERNET){
//                // 有限网络
//                return getLocalIp();
//            }
//        }
//        return null;
//    }
//
//    private static String intIP2StringIP(int ip) {
//        return (ip & 0xFF) + "." +
//                ((ip >> 8) & 0xFF) + "." +
//                ((ip >> 16) & 0xFF) + "." +
//                (ip >> 24 & 0xFF);
//    }
//
//
//    // 获取有限网IP
//    private static String getLocalIp() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface
//                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf
//                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress()
//                            && inetAddress instanceof Inet4Address) {
//                        return inetAddress.getHostAddress();
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//
//        }
//        return "0.0.0.0";
//
//    }
//
//    class ANDLINKBroadcastReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals("ANDLINK.searchack")) {
//                gwIP = intent.getStringExtra("gwIP");
//
//                System.out.println("ANDLINK.searchack:"+gwIP);
//
//                text1.setText(gwIP);
//
//                JSONObject object = new JSONObject();
//
//                try {
//                    object.put("deviceMac", ""+getMacAddressFromIp(MainActivity.this));
//                    object.put("deviceType", "30103");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                System.out.println("object:" + object.toString());
//
//                System.out.println("coap://"+gwIP+":5683/qlink/request");
//
//                CoapClient client = new CoapClient("coap://"+gwIP+":5683/qlink/request");
//
//                client.useCONs();
//
//                client.post(new CoapHandler() {
//                    @Override
//                    public void onLoad(CoapResponse coapResponse) {
//                        System.out.println("request:" + coapResponse.getResponseText());
//                    }
//
//                    @Override
//                    public void onError() {
//                        System.out.println("失败");
//                    }
//                }, object.toString(), MediaTypeRegistry.APPLICATION_JSON);
//            } else if (intent.getAction().equals("ANDLINK.netinfo")){
//                String WiFiConfig = intent.getStringExtra("WiFiConfig");
//                text2.setText(WiFiConfig);
//                gwIP = intent.getStringExtra("gwIP");
//                System.out.println("ANDLINK.netinfo:"+WiFiConfig);
//                if (!my_wifiManager.isWifiEnabled()){
//                    wifiAdmin.openWifi();
//                }
//
//                try {
//                    JSONObject object = new JSONObject(WiFiConfig);
//                    SSID = object.optString("SSID");
//                    password = object.optString("password");
//                    String encrypt = object.optString("encrypt");
//                    int type = 99;
//                    if (encrypt.indexOf("WPA") != -1){
//                        type = 3;
//                    } else if (encrypt.indexOf("OPEN") != -1){
//                        type = 1;
//                    }
//                    wifiAdmin.CreateWifiInfo(SSID,password,type);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            ConnectivityManager manager = (ConnectivityManager) context
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            NetworkInfo wifiInfo = manager
//                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//            if (wifiInfo!=null) {
//                WifiManager wifiManager = (WifiManager) context
//                        .getSystemService(Context.WIFI_SERVICE);
//                String wifiSSID = wifiManager.getConnectionInfo().getSSID().replace("\"","");
//                System.out.println("getSSID:"+wifiSSID +","+SSID);
//                if (wifiSSID.equals(SSID)){
//                    System.out.println("SSIDTrue");
//                    Message message = new Message();
//                    Bundle data = new Bundle();
//                    data.putInt("config",1);
//                    message.setData(data);
//                    handler.sendMessage(message);
//                }
//
//                if (!WifiConfig.equals("") && !wifiSSID.equals(SSID)){
//                    try {
//                        JSONObject object = new JSONObject(WifiConfig);
//                        SSID = object.optString("SSID");
//                        password = object.optString("password");
//                        String encrypt = object.optString("encrypt");
//                        int type = 99;
//                        if (encrypt.indexOf("WPA") != -1){
//                            type = 3;
//                        } else if (encrypt.indexOf("OPEN") != -1){
//                            type = 1;
//                        }
//                        wifiAdmin.CreateWifiInfo(SSID,password,type);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//
//        }
//    }
//
//    Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Bundle data = msg.getData();
//            int val = data.getInt("config");
//            Log.i(TAG,"请求结果:" + val);
//            if (val == 1){
//
//                JSONObject object = new JSONObject();
//
//                try {
//                    object.put("deviceMac", "8A:A3:51:34:04:F4");
//                    object.put("deviceType", "30103");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                if (gwIP != null && !gwIP.equals("") && !gwIP.equals("null")){
//                    System.out.println("object:" + object.toString());
//
//                    System.out.println("coap://"+gwIP+":5683/qlink/success");
//
//                    CoapClient client = new CoapClient("coap://"+gwIP+":5683/qlink/success");
//
//                    gwIP = "";
//
//                    client.useCONs();
//
//                    client.post(new CoapHandler() {
//                        @Override
//                        public void onLoad(CoapResponse coapResponse) {
////                            text3.setText("连接AndLink成功");
//                            System.out.println("success:" + coapResponse.getResponseText());
//                            if (isServiceRunning(MainActivity.this,"com.zhb.coap.AndLinkServer")){
//                                System.out.println("isServiceRunning");
//                            } else {
//                                System.out.println("ServiceNotRunning");
//                                Intent intent = new Intent(MainActivity.this,AndLinkServer.class);
//                                startService(intent);
//                            }
//                        }
//
//                        @Override
//                        public void onError() {
//                            System.out.println("失败");
//                        }
//                    }, object.toString(), MediaTypeRegistry.APPLICATION_JSON);
//                }
//
//            }
//        }
//    };
//
//
//    public static String getBroadcast() throws SocketException {
//        System.setProperty("java.net.preferIPv4Stack", "true");
//        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements(); ) {
//            NetworkInterface ni = niEnum.nextElement();
//            if (!ni.isLoopback()) {
//                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
//                    if (interfaceAddress.getBroadcast() != null) {
//                        return interfaceAddress.getBroadcast().toString().substring(1);
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//
//    private String getlocalip() {
//        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        int ipAddress = wifiInfo.getIpAddress();
//        if (ipAddress == 0) return "未连接wifi";
//        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
//                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
//    }

}
