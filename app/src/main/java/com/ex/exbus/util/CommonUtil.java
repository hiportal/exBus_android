package com.ex.exbus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CommonUtil {

    public static SharedPreferences mPref;

    public static void setPrefString(Context context, String key, String value){
        if(mPref == null){
            mPref = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String getPrefString(Context context, String key){
        if(mPref == null){
            mPref = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return mPref.getString(key,"");
    }

    public static void removePrefString(Context context, String key){
        if(mPref == null){
            mPref = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     */
    //String 값 null값 처리.
    public static final String nullCheck(String nullStr) {
        if (nullStr != null && !nullStr.equals("")) {
            if("null".equals(nullStr)){
                return "";
            }
            return nullStr;
        }
        return "";
    }

    /**
     * 파라미터 null 체크
     * @param mdn, mac, imei
     * @return 값이 null인 param 정보
     */
    public static String nullCheckParams(String mdn, String mac, String imei){
        String errorMessage = "";

        if(!(mdn != null && mdn.length() > 0)){
            errorMessage = "전화번호";
        }

        if(!(mac != null && mdn.length() > 0)){
            if(errorMessage.length() > 0){
                errorMessage = errorMessage + ", Mac Address";
            }else{
                errorMessage = errorMessage + "Mac Address";
            }

        }

        if(!(imei != null && mdn.length() > 0)){
            if(errorMessage.length() > 0){
                errorMessage = errorMessage + ", IMEI";
            }else{
                errorMessage = errorMessage + "IMEI";
            }
        }

        if(errorMessage.length() > 0){
            if(mac == null){
                errorMessage = errorMessage + " 값이 없습니다. Wi-Fi를 켠 후 다시 시도 해 주세요.";
            }
            else{
                errorMessage = errorMessage + " 값이 없습니다. 확인해 주세요.";
            }
            return errorMessage;
        }
        return "";
    }

}
