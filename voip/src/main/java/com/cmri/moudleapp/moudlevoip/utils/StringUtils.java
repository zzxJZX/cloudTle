package com.cmri.moudleapp.moudlevoip.utils;

import java.util.regex.Pattern;

public class StringUtils {
    /**
     * 判断是否为数字
     * @param str 传入的字符串
     * @return
     */
    final static Pattern NUM_PATTERN = Pattern.compile("[0-9]*");
    public static boolean isNumeric(String str){
        return NUM_PATTERN.matcher(str).matches();
    }

    final static Pattern MOBILE_PATTERN = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[0-9]))\\d{0,8}$");
    public static boolean isChinaMobileNum(String mobiles){
        return MOBILE_PATTERN.matcher(mobiles).matches() && (mobiles.length() == 11);
    }

}
