package com.ex.exbus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ex.exbus.util.CommonUtil;
import com.ex.exbus.util.HttpUtil;
import com.ex.exbus.util.XMLData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIIDService";
    String tag = "FirebaseFCM ----- ";
    Context context;
    String token;

    @Override
    public void onTokenRefresh() {
        Log.d(TAG, tag+"onTokenRefresh() 호출됨");

        context = getApplicationContext();
        token = FirebaseInstanceId.getInstance().getToken();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, tag+"onTokenRefresh() - refreshedToken : "+refreshedToken);


        if(token != null && !("").equals(token)){
            //sendToken(context, token);
        }

    }


    public void sendToken(final Context context, String token){
        Log.d(TAG, tag+"sendToken() - packageName : "+context.getPackageName());
        Log.d(TAG, tag+"sendToken() - mdn : "+ getMdn(context));
        Log.d(TAG, tag+"sendToken() - imei : "+getImei(context));
        String url = "http://travel.ex.co.kr/cloudpush/mergeClientInfo.do?app_info="+context.getPackageName()+"&device_id="+getMdn(context)+"&device_imei="+getImei(context)+"&device_token="+token;
        Log.d(TAG, tag+"sendToken() - url : "+url);

        if(token!=null && !("").equals(token)){
            Log.d(TAG, tag+"sendToken() TOKEN : "+token);


            HttpUtil.request(context, url, new HttpUtil.HttpCallback() {

                @Override
                public void onResponse(String response) {
                    // TODO Auto-generated method stub

                    try{
                        Log.d(TAG, tag+"sendToken() - response : "+response);
                        XMLData responseXML =  new XMLData(response);
                        if("OK".equals(responseXML.get("resStatus"))){
                            Log.d(TAG, tag+"sendToken() - fcm success : "+responseXML.get("resStatus"));
                            SharedPreferences userPref = context.getSharedPreferences("PUSH", MODE_PRIVATE);
                            SharedPreferences.Editor editor = userPref.edit();
                            editor.putString("fcmDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                            editor.apply();
                            Log.d(TAG, tag+"sendToken() - update token success ..");
                            Log.i(TAG, tag+"sendToken() - fcm date : "+context.getSharedPreferences("PUSH", MODE_PRIVATE).getString("fcmDate", ""));
                        }else{
                            Log.d(TAG, tag+"sendToken() - update token failed ..");
                        }
                    }catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    // 전화번호 가져오기
    @SuppressLint("MissingPermission")
    public static String getMdn(Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String mdn = tm.getLine1Number();
        if(mdn != null){
            if(mdn.indexOf("+82") == 0){
                mdn = mdn.replaceFirst("\\+82","0");
            }
        }else{
            mdn = "";
        }
        return mdn;
    }

    // imei값 가져오기
    @SuppressLint("MissingPermission")
    public static String getImei(Context context){
        TelephonyManager mTelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getDeviceId();
    }
}
