package coap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;

public class AndLinkServer extends Service {

    private String localip = null;

    private CoapServer coapServer = new CoapServer();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!getlocalip().equals("未连接wifi")){
            String localip2 = getlocalip();
//            System.out.println("onstart:"+localip2);
//            System.out.println("onstart:"+localip);
//            if (!localip2.equals(localip)){
                localip = localip2;
                System.out.println("onstart:"+localip);
                coapServer.addEndpoint(new CoapEndpoint(new InetSocketAddress(localip, 5683)));
                System.out.println("222");

                coapServer.add(new CoapResource("qlink") {
                    @Override
                    public void handlePOST(CoapExchange exchange) {
//                super.handlePOST(exchange);
                        System.out.println("qlink");
//                Toast.makeText(getApplicationContext(),"qlink",Toast.LENGTH_SHORT).show();
                    }
                }.add(new CoapResource("searchdevice") {
                    @Override
                    public void handlePOST(CoapExchange exchange) {
                        JSONObject object = new JSONObject();
                        try {
                            object.put("searchAck", "ANDLINK-DEVICE");
                            object.put("andlinkVersion", "V3");
                            object.put("deviceType", "222");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        exchange.respond(object.toString());
                        System.out.println("searchdevice:" + exchange.getRequestText());
                        super.handlePOST(exchange);
                    }
                }).add(new CoapResource("searchack") {
                    @Override
                    public void handlePOST(CoapExchange exchange) {
                        JSONObject object = new JSONObject();
                        try {
                            object.put("respCode", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        exchange.respond(object.toString());
                        super.handlePOST(exchange);

                        Intent intent = new Intent();
                        intent.setAction("ANDLINK.searchack");
                        intent.putExtra("gwIP", exchange.getSourceAddress().getHostAddress());
                        sendBroadcast(intent);

                        System.out.println("searchack:" + exchange.getRequestText());
                    }
                }).add(new CoapResource("netinfo") {
                    @Override
                    public void handlePOST(CoapExchange exchange) {
//                super.handlePOST(exchange);
                        JSONObject object = new JSONObject();
                        try {
                            object.put("respCode", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        exchange.respond(object.toString());
                        super.handlePOST(exchange);

                        SharedPreferences sp = getSharedPreferences("AndLink",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("WiFiConfig",exchange.getRequestText());
                        editor.putString("gwIP",exchange.getSourceAddress().getHostAddress());
                        editor.commit();

                        Intent intent = new Intent();
                        intent.setAction("ANDLINK.netinfo");
                        intent.putExtra("WiFiConfig", exchange.getRequestText());
                        intent.putExtra("gwIP", exchange.getSourceAddress().getHostAddress());
                        sendBroadcast(intent);

                        System.out.println("netinfo:" + exchange.getRequestText());
//                Toast.makeText(getApplicationContext(),"netinfo",Toast.LENGTH_SHORT).show();
                    }
                }));

                System.out.println("333");
                coapServer.start();
                System.out.println("444");
//            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        System.out.println("111" + getlocalip());
//        if (!getlocalip().equals("未连接wifi")) {
//            localip = getlocalip();
//            System.out.println("onCreate:"+localip);
//            CoapServer coapServer = new CoapServer();
//            coapServer.addEndpoint(new CoapEndpoint(new InetSocketAddress(localip, 5683)));
//            System.out.println("222");
//
//            coapServer.add(new CoapResource("qlink") {
//                @Override
//                public void handlePOST(CoapExchange exchange) {
////                super.handlePOST(exchange);
//                    System.out.println("qlink");
////                Toast.makeText(getApplicationContext(),"qlink",Toast.LENGTH_SHORT).show();
//                }
//            }.add(new CoapResource("searchdevice") {
//                @Override
//                public void handlePOST(CoapExchange exchange) {
//                    JSONObject object = new JSONObject();
//                    try {
//                        object.put("searchAck", "ANDLINK-DEVICE");
//                        object.put("andlinkVersion", "V3");
//                        object.put("deviceType", "222");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    exchange.respond(object.toString());
//                    System.out.println("searchdevice:" + exchange.getRequestText());
//                    super.handlePOST(exchange);
//                }
//            }).add(new CoapResource("searchack") {
//                @Override
//                public void handlePOST(CoapExchange exchange) {
//                    JSONObject object = new JSONObject();
//                    try {
//                        object.put("respCode", 1);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    exchange.respond(object.toString());
//                    super.handlePOST(exchange);
//
//                    Intent intent = new Intent();
//                    intent.setAction("ANDLINK.searchack");
//                    intent.putExtra("gwIP", exchange.getSourceAddress().getHostAddress());
//                    sendBroadcast(intent);
//
//                    System.out.println("searchack:" + exchange.getRequestText());
//                }
//            }).add(new CoapResource("netinfo") {
//                @Override
//                public void handlePOST(CoapExchange exchange) {
////                super.handlePOST(exchange);
//                    JSONObject object = new JSONObject();
//                    try {
//                        object.put("respCode", 1);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    exchange.respond(object.toString());
//                    super.handlePOST(exchange);
//
//                    Intent intent = new Intent();
//                    intent.setAction("ANDLINK.netinfo");
//                    intent.putExtra("WiFiConfig", exchange.getRequestText());
//                    intent.putExtra("gwIP", exchange.getSourceAddress().getHostAddress());
//                    sendBroadcast(intent);
//
//                    System.out.println("netinfo:" + exchange.getRequestText());
////                Toast.makeText(getApplicationContext(),"netinfo",Toast.LENGTH_SHORT).show();
//                }
//            }));
//
//            System.out.println("333");
//            coapServer.start();
//            System.out.println("444");
//        }

    }

    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress == 0) return "未连接wifi";
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }
}
