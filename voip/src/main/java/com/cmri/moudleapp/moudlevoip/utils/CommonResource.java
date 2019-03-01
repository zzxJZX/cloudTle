package com.cmri.moudleapp.moudlevoip.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Anderson on 2017/4/5.
 */

public class CommonResource {
    private final static String RCS_PREFERENCES = "rcs_preferences";
    private static CommonResource resource;
    private Context context;

    public final static String ACCOUNT_INFO = "accountInfo";
    public final static String STB_INFO = "stbInfo";
    public final static String ADAPTER_PARAMS = "adapterParams";
    public final static String TV_IMS_STATUS = "tvImsStatus";
    public final static String TV_HAS_OWNERS = "tvHasOwners";
    public final static String VOIP_ID = "voipId";
    public final static String IS_AUTO_ANSWER = "is_auto_answer";
    public final static String VIDEO_CONFIG = "video_config";
    public final static String IS_VERSION_UPDATE_ING = "is_version_update_ing";

    //add by cll
    private final static Lock DESEncryptLock = new ReentrantLock();

    public static CommonResource getInstance() {
        if (resource == null) {
            resource = new CommonResource();
        }
        return resource;
    }

    public SharedPreferences getSp() {
        return context.getSharedPreferences(RCS_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getAppContext() {
        return this.context;
    }

    public int getInt(String key, int defValue) {
        return getSp().getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return getSp().getLong(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return getSp().getBoolean(key, defValue);
    }

    public String getString(String key, String defValue) {
        String value = getSp().getString(key, defValue);
        if (value == null || value.equals(defValue)) {
            return defValue;
        } else {
            String mingStr;
            try {
                DESEncryptLock.lock();
                mingStr = DESEncrypt.decrypt(value);
//                DESEncryptLock.unlock();
            } catch (Exception e) {
                e.printStackTrace();
                mingStr = "";
            } finally {
                DESEncryptLock.unlock();
            }
            //MyLogger.getLogger("CommonResource").d("getString key:"+key+", mingStr :"+mingStr+", value :"+value);

            return mingStr;
        }
    }

    public Set<String> getStringSet(String key, Set<String> defValues){
        return getSp().getStringSet(key, defValues);
    }

    public HashMap<String,String> getStringHashMap(String key, HashMap<String,String> defValue){
        HashMap<String,String> hashMap = null;
        Set<String> stringSet = getStringSet(key,null);
        if(stringSet==null){
            hashMap = new HashMap<String, String>(0);
        } else {
            hashMap = new HashMap<String, String>(stringSet.size());
            int dividerIndex;
            String mapKey,mapValue;
            for (String strItem: stringSet) {
                if(!TextUtils.isEmpty(strItem)){
                    dividerIndex = strItem.indexOf("@#@");
                    mapKey = strItem.substring(0, dividerIndex);
                    try {
                        mapKey = new String(DESEncrypt.hexStr2ByteArr(mapKey),"UTF-8");
                    } catch (Exception e) {
                    }
                    mapValue = strItem.substring(dividerIndex + 3);
                    hashMap.put(mapKey, mapValue);
                }
            }
        }
        return hashMap;
    }

    public void clear(){
        getSp().edit().clear().commit();
    }

    /**
     *
     * Set an int value in the preferences file.
     *
     * @param key
     *            The name of the preference to modify.
     * @param value
     *            The new value for the preference.
     *  @return
     */
    public boolean putInt(String key, int value) {
        return getSp().edit().putInt(key, value).commit();
    }

    public boolean putLong(String key, Long value) {
        return getSp().edit().putLong(key, value).commit();
    }

    public boolean putBoolean(String key, boolean value) {
        return getSp().edit().putBoolean(key, value).commit();
    }

    /**
     * Set a string value in the preferences file.
     *
     * @param key
     *            The name of the preference to modify.
     * @param value
     *            The new value for the preference.
     * @return
     */
    public boolean putString(String key, String value) {
        String miStr;
        try {
            DESEncryptLock.lock();
            miStr = DESEncrypt.encrypt(value);
        } catch (Exception e) {
            miStr = value;
        } finally {
            DESEncryptLock.unlock();
        }
        return getSp().edit().putString(key, miStr).commit();
    }

    public boolean deleteString(String key) {
        return getSp().edit().remove(key).commit();
    }

    public boolean putStringSet(String key, Set<String> value){
        return getSp().edit().putStringSet(key, value).commit();
    }

    public boolean  putStringHashMap(String key, HashMap<String,String> value){
        try {
            Set<String> stringSet = new HashSet<String>(value.size());
            Iterator<Map.Entry<String,String>> iter = value.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,String> entry =  iter.next();
                String mapKey = DESEncrypt.byteArr2HexStr(entry.getKey().getBytes("UTF-8"));
                String mapValue = entry.getValue();
                stringSet.add(mapKey  + "@#@" + mapValue);
            }
            return putStringSet(key, stringSet);
        } catch (Exception e) {
            return false;
        }
    }

    public String[] getStringArray(String key, String[] values) {
        String arrayString = getString(key,"");
        if(TextUtils.isEmpty(arrayString))
            return values;
        return arrayString.split("#");
    }

    public boolean putStringArray(String key, String[] values) {
        StringBuilder sb = new StringBuilder();
        for(String value : values){
            sb.append(value).append("#");
        }
        sb.deleteCharAt(sb.length()-1);
        return putString(key, sb.toString());
    }

}
