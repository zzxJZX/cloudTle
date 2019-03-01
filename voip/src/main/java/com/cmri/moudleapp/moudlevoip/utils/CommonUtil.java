package com.cmri.moudleapp.moudlevoip.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kaven on 2017/2/20.
 */

public class CommonUtil {

    public static boolean isTvNum(String num) {
        if (TextUtils.isEmpty(num)) {
            return false;
        }
        if (num.length() == 11 || num.length() == 12) {
            return num.startsWith("0");
        }
        return false;
    }


    public static boolean isPhoneNum(String num) {
        if (TextUtils.isEmpty(num)) {
            return false;
        }
        if (num.length() == 11) {
            return num.startsWith("1");
        }
        return false;
    }


    public static String getNativeVersionName(Context context) {
        String versionName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = null;
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }


    /**
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }


    /**
     * 获取无线mac地址
     * @param context
     * @return
     */
    public static String getWifiMacAddress(Context context){

        String macSerial  = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/eth0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            String line;
            while ((line = input.readLine()) != null) {
                macSerial += line.trim();
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macSerial;
    }


    /**
     * 去掉汉语
     * @param chin
     * @return
     */
    public static String filterChinese(String chin){
        return chin.replaceAll("[\\u4e00-\\u9fa5]", "");
    }
}
