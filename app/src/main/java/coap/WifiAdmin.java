package coap;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;

public class WifiAdmin {
    // 瀹氫箟WifiManager瀵硅薄   
    private WifiManager mWifiManager;
    // 瀹氫箟WifiInfo瀵硅薄     
    private WifiInfo mWifiInfo;
    // 鎵弿鍑虹殑缃戠粶杩炴帴鍒楄〃     
    private List<ScanResult> mWifiList;
    // 缃戠粶杩炴帴鍒楄〃     
    private List<WifiConfiguration> mWifiConfiguration;
    // 瀹氫箟涓�涓猈ifiLock     
    WifiLock mWifiLock;

    // 鏋勯�犲櫒     
    public WifiAdmin(Context context) {
        // 鍙栧緱WifiManager瀵硅薄     
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 鍙栧緱WifiInfo瀵硅薄     
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    // 鎵撳紑WIFI   
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
//        while (mWifiManager.getWifiState() == WIFI_STATE_ENABLING) {
//            break;
//        }
//        return true;
    }

    // 鍏抽棴wifi
    public void closeWifi(Context context) {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        } else if (mWifiManager.getWifiState() == 1) {
            Toast.makeText(context, "浜诧紝Wifi宸茬粡鍏抽棴锛屼笉鐢ㄥ啀鍏充簡", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 0) {
            Toast.makeText(context, "浜诧紝Wifi姝ｅ湪鍏抽棴锛屼笉鐢ㄥ啀鍏充簡", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "璇烽噸鏂板叧闂�", Toast.LENGTH_SHORT).show();
        }
    }

    // 妫�鏌ュ綋鍓峎IFI鐘舵��     
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    // 閿佸畾WifiLock     
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 瑙ｉ攣WifiLock     
    public void releaseWifiLock() {
        // 鍒ゆ柇鏃跺�欓攣瀹�     
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 鍒涘缓涓�涓猈ifiLock     
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    // 寰楀埌閰嶇疆濂界殑缃戠粶     
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    // 鎸囧畾閰嶇疆濂界殑缃戠粶杩涜杩炴帴     
    public void connectConfiguration(int index) {
        // 绱㈠紩澶т簬閰嶇疆濂界殑缃戠粶绱㈠紩杩斿洖     
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 杩炴帴閰嶇疆濂界殑鎸囧畾ID鐨勭綉缁�     
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }

    public void startScan(Context context) {
        mWifiManager.startScan();
        //寰楀埌鎵弿缁撴灉   
        List<ScanResult> results = mWifiManager.getScanResults();
        // 寰楀埌閰嶇疆濂界殑缃戠粶杩炴帴
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        if (results == null) {
            if (mWifiManager.getWifiState() == 3) {
                Toast.makeText(context, "褰撳墠鍖哄煙娌℃湁鏃犵嚎缃戠粶", Toast.LENGTH_SHORT).show();
            } else if (mWifiManager.getWifiState() == 2) {
                Toast.makeText(context, "wifi姝ｅ湪寮�鍚紝璇风◢鍚庢壂鎻�", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "WiFi娌℃湁寮�鍚�", Toast.LENGTH_SHORT).show();
            }
        } else {
            mWifiList = new ArrayList();
            for (ScanResult result : results) {
                if (result.SSID == null || result.SSID.length() == 0
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                Log.i("WifiDetails", "result= " + result.SSID + " capabilities= " + result.capabilities);
                for (ScanResult item : mWifiList) {
                    Log.i("WifiDetails", "item= " + item.SSID + " capabilities=" + item.capabilities);
                    if (item.SSID.equals(result.SSID) && item.capabilities.equals(result.capabilities)) {
                        Log.i("WifiDetails", "found true");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mWifiList.add(result);
                }
            }
        }
    }

    // 寰楀埌缃戠粶鍒楄〃   
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    // 鏌ョ湅鎵弿缁撴灉   
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder
                    .append("Index_" + new Integer(i + 1).toString() + ":");
            // 灏哠canResult淇℃伅杞崲鎴愪竴涓瓧绗︿覆鍖�     
            // 鍏朵腑鎶婂寘鎷細BSSID銆丼SID銆乧apabilities銆乫requency銆乴evel    
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }

    // 寰楀埌MAC鍦板潃  
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 寰楀埌鎺ュ叆鐐圭殑BSSID   
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    // 寰楀埌杩炴帴鐨処P  
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 寰楀埌杩炴帴鐨処D   
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 寰楀埌WifiInfo鐨勬墍鏈変俊鎭寘   
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    // 娣诲姞涓�涓綉缁滃苟杩炴帴   
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        System.out.println("a--" + wcgID);
        System.out.println("b--" + b);
    }

    // 鏂紑鎸囧畾ID鐨勭綉缁�   
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public void removeWifi(int netId) {
        disconnectWifi(netId);
        mWifiManager.removeNetwork(netId);
    }

//鍒涘缓wifi鐑偣鐨勩��

    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
//        if (openWifi()) {
            System.out.println("openWifi");
            WifiConfiguration config = new WifiConfiguration();
            config.allowedAuthAlgorithms.clear();
            config.allowedGroupCiphers.clear();
            config.allowedKeyManagement.clear();
            config.allowedPairwiseCiphers.clear();
            config.allowedProtocols.clear();
            config.SSID = "\"" + SSID + "\"";
            config.hiddenSSID = true;
//
            WifiConfiguration tempConfig = this.IsExsits(SSID);
            if (tempConfig != null) {
                mWifiManager.removeNetwork(tempConfig.networkId);
//              mWifiManager.saveConfiguration();
            }

            if (Type == 1) //WIFICIPHER_NOPASS
            {
//              config.wepKeys[0] = "\"" + "\"";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//              config.wepTxKeyIndex = 0;
                int wifiId = mWifiManager.addNetwork(config);
                System.out.println("wifiId:" + wifiId);
                mWifiManager.enableNetwork(wifiId, true);
            }
//          if(Type == 2) //WIFICIPHER_WEP
//          {
//              config.hiddenSSID = true;
//              config.wepKeys[0]= "\""+Password+"\"";
//              config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//              config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//              config.wepTxKeyIndex = 0;
//          }
            if (Type == 3) //WIFICIPHER_WPA
            {
                config.preSharedKey = "\"" + Password + "\"";
                config.hiddenSSID = false;
                config.status = WifiConfiguration.Status.ENABLED;
                int wifiId = mWifiManager.addNetwork(config);
                mWifiManager.enableNetwork(wifiId, true);
            }
            return config;
//        }

//        return null;
    }

    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }
}