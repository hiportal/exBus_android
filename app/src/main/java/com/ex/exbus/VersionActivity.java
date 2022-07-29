package com.ex.exbus;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.ex.exbus.util.HttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class VersionActivity extends Activity {
    final static String TAG = "VersionActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        String mobile = intent.getStringExtra("mobile");
        String userType = intent.getStringExtra("userType");
        String rgstTrgtClssCd = intent.getStringExtra("rgstTrgtClssCd");
        String mdn = intent.getStringExtra("mdn");
        versionChk(userType);
    }

    public void versionChk(String userType){ // version이 맞으면 true return
        Log.d(TAG, "===== getEXBUSAppVer() =====");
        Log.d(TAG, "===== getEXBUSAppVer() - userType : "+userType);

        HttpConnection.HttpRequest request = new HttpConnection.HttpRequest();
        request.url_header = HttpConnection.URL_EXBUS_HEADER;

        if(userType.equals("EX")){
            request.url_tail = HttpConnection.URL_EXBUS_VERSION_EXUSER;
        }else if(userType.equals("ETC")){
            request.url_tail = HttpConnection.URL_EXBUS_VERSION;
        }

        request.listener = new HttpConnection.IHttpResult() {
            @Override
            public void onHttpResult(String url, String json) {
                try {
                    JSONObject jsonobject = new JSONObject(json);
                    Log.i(TAG, "getEXBUSAppVer() - jsonobject Result : "+jsonobject.toString());
                    if(jsonobject != null){
                        String dbVer = jsonobject.get("app_ver").toString();
                        checkAppVersion(dbVer);
                    }else{
                        Log.d(TAG, "getEXBUSAppVer() appVer data 없음 ");
                    }
                } catch (ParseException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        HttpConnection conn = new HttpConnection(request);
        conn.request();
    }

    public void checkAppVersion(String dbVer){
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo("com.ex.exbus", PackageManager.GET_META_DATA);
        } catch (Exception e) {
        }

        String appVer = pInfo.versionName;

        Log.d(TAG, "[versionName : "+pInfo.versionName+"][versionCode : "+pInfo.versionCode+"]");

        if(!dbVer.equals(appVer)){
            //팝업창 띄우기
            AlertDialog.Builder alert = new AlertDialog.Builder(VersionActivity.this);
            //'확인' 클릭 시 앱 종료
            alert.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            //'업데이트' 클릭 시 ex-스토어 로 이동
            alert.setNegativeButton("업데이트", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean tf = getInstallApp("com.ex.group.store");
                    if(tf == true) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.ex.group.store");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://store.ex.co.kr"));
                        startActivity(intent);
                        finish();
                    }
                }
            });
            alert.setMessage("앱이 업데이트 되었습니다.설치 후 사용하시기 바랍니다.");
            alert.show();
        }else{
            Intent intent = getIntent();
            String id = intent.getStringExtra("id");
            String name = intent.getStringExtra("name");
            String mobile = intent.getStringExtra("mobile");
            String userType = intent.getStringExtra("userType");
            String rgstTrgtClssCd = intent.getStringExtra("rgstTrgtClssCd");
            String mdn = intent.getStringExtra("mdn");
            Log.d(TAG, "m_id : "+id);
            Log.d(TAG, "m_name : "+name);
            Log.d(TAG, "m_mobile : "+mobile);
            Log.d(TAG, "m_type : "+userType);
            Log.d(TAG, "m_rgstTrgtClssCd : "+rgstTrgtClssCd);
            Log.d(TAG, "m_mdn : "+mdn);

            goMainActivity(id, name, mobile, userType, rgstTrgtClssCd, mdn);
        }
    }

    public void goMainActivity(String id, String name, String mobile, String userType, String rgstTrgtClssCd, String mdn){
        Intent intent = new Intent(VersionActivity.this, MainActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("mobile", mobile);
        intent.putExtra("userType", userType);
        intent.putExtra("rgstTrgtClssCd",rgstTrgtClssCd);
        intent.putExtra("mdn",mdn);
        startActivity(intent);
        finish();
    }

    public boolean getInstallApp(String packageName) {
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
        if(intent == null) {
            return false;
        }else {
            return true;
        }
    }
}
