package com.ex.exbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ex.exbus.util.CommonUtil;
import com.ex.exbus.util.HttpConnection;
import com.ex.exbus.util.HttpUtil;
import com.ex.exbus.util.XMLData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class IntroActivity extends AppCompatActivity {
    final static String TAG = "IntroActivity";
    final static int permissionReqCode = 2;
    final static int CONN_TIMEOUT = 5;
    final static int READ_TIMEOUT = 5;
    final static String METHOD_TYPE = "GET";

    ImageView iv_bus;
    Animation animation;
    Handler handler;

    private ProgressBar pg_login;
    private EditText et_login_id, et_login_pwd;
    private String m_id;
    private String m_pwd;
    private String m_name;
    private String m_mobile;
    private String m_type;
    private String m_rgstTrgtClssCd;
    private String m_mdn;
    private String m_result;
    private String logoutYn;

    String[]permissions = {
            Manifest.permission.CALL_PHONE
            ,Manifest.permission.READ_PHONE_STATE
            ,Manifest.permission.ACCESS_COARSE_LOCATION
            ,Manifest.permission.ACCESS_FINE_LOCATION
            ,Manifest.permission.READ_SMS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Intent intent = getIntent();
        String logoutYN = intent.getStringExtra("logoutYN");
        if(logoutYN == "" || logoutYN == null){
            logoutYN = "N";
        }
        Log.d(TAG,TAG+">> onCreate() - logoutYN:"+logoutYN);

        //logoutYN 이 Y 이면 로그아웃 진행
        if(logoutYN.equals("Y")){
            CommonUtil.removePrefString(IntroActivity.this, "id");
            CommonUtil.removePrefString(IntroActivity.this, "name");
            CommonUtil.removePrefString(IntroActivity.this, "mobile");
            CommonUtil.removePrefString(IntroActivity.this, "userType");
            CommonUtil.removePrefString(IntroActivity.this, "rgstTrgtClssCd");
            CommonUtil.removePrefString(IntroActivity.this, "mdn");
            Log.d(TAG, "----------========== id (logout) : "+CommonUtil.getPrefString(IntroActivity.this, "id"));
        }

        permissionCheck();
    }

    private void permissionCheck(){
        if(checkSelfPermission(Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, TAG+">> onCreate() - permissions : "+permissions);
            requestPermissions(permissions, permissionReqCode);
        }else{
            Log.d(TAG, TAG+">> onCreate() - permissions 2 : "+permissions);
            getUser();
        }
    }

    public boolean hasAllPermissionGranted(int[] grantResults){
        Log.d(TAG, TAG+">>hasAllPermissionGranted()~!");
        for(int result : grantResults){
            if(result == PackageManager.PERMISSION_DENIED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, TAG+">>onRequestPermissionsResult()~!");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case permissionReqCode :
                if(hasAllPermissionGranted(grantResults)){
                    getUser();
                    Log.i(TAG, "hasAllGranted : true");
                }
                else{
                    Log.i(TAG, "hasAllGranted : false");
                    Log.i(TAG, "CALL_PHONE permission : "+checkSelfPermission(permissions[0]));
                    Log.i(TAG, "READ PHONE STATE permission : "+checkSelfPermission(permissions[1]));
                    Log.i(TAG, "ACCESS_FINE_LOCATION permission : "+checkSelfPermission(permissions[2]));
                    Toast.makeText(IntroActivity.this, R.string.permission_grant, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:"+ getApplicationContext().getPackageName()));
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    public boolean checkNetwork(){
        boolean netWorkStatus = false;
        ConnectivityManager cm = (ConnectivityManager) IntroActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetWork = cm.getActiveNetworkInfo();
        if(activeNetWork != null){
            if(activeNetWork.getType() == ConnectivityManager.TYPE_WIFI && activeNetWork.isConnected()){
                netWorkStatus = true;
            }else if(activeNetWork.getType() == ConnectivityManager.TYPE_MOBILE && activeNetWork.isConnected()){
                netWorkStatus = true;
            }
        }
        return netWorkStatus;
    }

    //앱버전 체크(쓰레드)
    Runnable runner = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(IntroActivity.this, VersionActivity.class);
            intent.putExtra("id", m_id);
            intent.putExtra("name", m_name);
            intent.putExtra("mobile", m_mobile);
            intent.putExtra("userType", m_type);
            intent.putExtra("rgstTrgtClssCd",m_rgstTrgtClssCd);
            intent.putExtra("mdn",m_mdn);
            startActivity(intent);
            finish();
        }
    };


    public void getUser(){
        if(!checkNetwork()){
            AlertDialog.Builder alert = new AlertDialog.Builder(IntroActivity.this);
            alert.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                    finish();
                }
            });
        }else{
            pg_login = (ProgressBar)findViewById(R.id.progressBar1);
            String swbeonho = CommonUtil.getPrefString(IntroActivity.this,"id");    // 사번이 있는 경우 바로 유저 정보를 가져온다.
            if(swbeonho.length() > 0){
                getUserInfo(swbeonho);
            }else{
                pg_login.setVisibility(View.GONE);
                showDialog(this,"Login");
            }
        }

    }

    public void insertUserLog(){
        Log.d(TAG, TAG+">>insertUserLog()~!");
        HttpConnection.HttpRequest request = new HttpConnection.HttpRequest();
        request.url_header = HttpConnection.URL_EXBUS_HEADER;
        request.url_tail = HttpConnection.URL_USER_LOG;

        request.arg.add("REG_ID");
        request.arg.add(m_id);

        request.listener = new HttpConnection.IHttpResult() {

            @Override
            public void onHttpResult(String url, String json) {
                try {
                    JSONObject jsonobject = new JSONObject(json);
                } catch (ParseException | JSONException e) {
                    e.printStackTrace();
                }
                handler = new Handler();
                handler.postDelayed(runner, 100);
            }
        };

        HttpConnection conn = new HttpConnection(request);
        conn.request();
    }

    /*사용자 정보 insert*/
    public void getUserInfo(String swbeonho, String password){
        Log.d(TAG, TAG+">>getUserInfo()~!");
        //Log.d(TAG, TAG+">>getUserInfo() - swbeonho : "+swbeonho+", password : "+password);
        if(swbeonho != null && swbeonho.length() > 7){
            //사용자 정보 비교
            final HttpConnection.HttpRequest request = new HttpConnection.HttpRequest();
            request.url_header = HttpConnection.URL_EXBUS_HEADER;
            request.url_tail = HttpConnection.URL_USER_INFO2;
            request.arg.add("USER_ID");
            request.arg.add(swbeonho);
            request.arg.add("ENC_PWD");
            request.arg.add(password);

            m_id = swbeonho;
            m_pwd = password;

            request.listener = new HttpConnection.IHttpResult() {
                @Override
                public void onHttpResult(String url, String json) {
                    try {
                        String stArr = json.trim();
                        JSONObject jsonobject = new JSONObject(json.trim());

                        //로그인 성공
                        if(jsonobject.get("result").equals("1")){
                            if(stArr.contains("userNm")){
                                Context context = getApplicationContext();
                                m_name = (String)jsonobject.get("userNm");
                                m_mobile = (String)jsonobject.get("mobile");
                                m_type = (String)jsonobject.get("userType");
                                m_rgstTrgtClssCd = (String)jsonobject.get("rgstTrgtClssCd");
                                m_mdn = getMdn(context);
                                CommonUtil.setPrefString(IntroActivity.this, "id", m_id);
                                CommonUtil.setPrefString(IntroActivity.this, "name", m_name);
                                CommonUtil.setPrefString(IntroActivity.this, "mobile", m_mobile);
                                CommonUtil.setPrefString(IntroActivity.this, "userType", m_type);
                                CommonUtil.setPrefString(IntroActivity.this, "rgstTrgtClssCd", m_rgstTrgtClssCd);
                                CommonUtil.setPrefString(IntroActivity.this, "mdn", m_mdn);
                                Log.d(TAG, "----------========== id (재진입시) : "+CommonUtil.getPrefString(IntroActivity.this, "id"));
                                insertUserLog();
                            }
                            //비밀번호 오류
                        }else if(jsonobject.get("result").equals("0")){
                            showToast("비밀번호를 확인하여 주십시오.");
                        }else{
                            showToast("정보가 존재하지 않습니다.");
                        }

                    } catch (ParseException e) {
                        showToast("사원번호 또는 비밀번호를 확인하여 주십시오.");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            getToken();
            HttpConnection conn = new HttpConnection(request);
            conn.request();
        }
    }

    /*사용자 정보 update*/
    public void getUserInfo(String swbeonho){
        Log.d(TAG, TAG+">>getUserInfo()~!(정보 update)");
        Log.d(TAG, TAG+">>getUserInfo() - swbeonho : "+swbeonho);
        if(swbeonho != null && swbeonho.length() > 7){

            //사용자 정보 비교
            final HttpConnection.HttpRequest request = new HttpConnection.HttpRequest();
            request.url_header = HttpConnection.URL_EXBUS_HEADER;
            request.url_tail = HttpConnection.URL_USER_INFO;
            request.arg.add("USER_ID");
            request.arg.add(swbeonho);
            m_id = swbeonho;

            request.listener = new HttpConnection.IHttpResult() {

                @Override
                public void onHttpResult(String url, String json) {
                    Log.d(TAG, TAG+">>getUserInfo() - url === "+url);
                    try {
                        JSONObject jsonobject = new JSONObject(json.trim());
                        if(jsonobject != null && jsonobject.get("result").equals("1")){
                            Context context = getApplicationContext();
                            m_name = (String)jsonobject.get("userNm");
                            m_mobile = (String)jsonobject.get("mobile");
                            m_type = (String)jsonobject.get("userType");
                            m_rgstTrgtClssCd = (String)jsonobject.get("rgstTrgtClssCd");
                            m_mdn = getMdn(context);
                            CommonUtil.setPrefString(IntroActivity.this, "id", m_id);
                            CommonUtil.setPrefString(IntroActivity.this, "name", m_name);
                            CommonUtil.setPrefString(IntroActivity.this, "mobile", m_mobile);
                            CommonUtil.setPrefString(IntroActivity.this, "userType", m_type);
                            CommonUtil.setPrefString(IntroActivity.this, "rgstTrgtClssCd", m_rgstTrgtClssCd);
                            CommonUtil.setPrefString(IntroActivity.this, "mdn", m_mdn);
                            Log.d(TAG, "----------========== id (재진입시2) : "+CommonUtil.getPrefString(IntroActivity.this, "id"));
                            insertUserLog();

                        }else{
                            showToast("사원번호를 확인하여 주십시오.");
                            CommonUtil.removePrefString(IntroActivity.this, "id");
                            CommonUtil.removePrefString(IntroActivity.this, "name");
                            CommonUtil.removePrefString(IntroActivity.this, "mobile");
                            CommonUtil.removePrefString(IntroActivity.this, "userType");
                            CommonUtil.removePrefString(IntroActivity.this, "rgstTrgtClssCd");
                            CommonUtil.removePrefString(IntroActivity.this, "mdn");
                            Log.d(TAG, "----------========== id (사원번호 확인해 주십시오) : "+CommonUtil.getPrefString(IntroActivity.this, "id"));
                            getUser();
                        }

                    } catch (ParseException | JSONException e) {
                        showToast("사원번호 또는 비밀번호를 확인하여 주십시오.");
                    }
                }
            };

            getToken();
            HttpConnection conn = new HttpConnection(request);
            conn.request();
        }
    }


    public void showDialog(Activity activity, String msg){
        Log.d(TAG, TAG+">>showDialog()~!");
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_login);

        et_login_id = (EditText) dialog.findViewById(R.id.et_login_id);
        et_login_id.requestFocus();

        et_login_pwd = (EditText) dialog.findViewById(R.id.et_login_pwd);

        //취소버튼 추가
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_login);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String swbeonho = et_login_id.getText().toString();
                String password = et_login_pwd.getText().toString();

                if(swbeonho != null && swbeonho.length() > 0){
                    if(swbeonho.length() > 7){
                        if(password != null && password.length() > 0){
                            getUserInfo(swbeonho, password);
                        }else{
                            showToast("비밀번호를 입력해 주십시오.");
                        }
                    }else{
                        showToast("사원번호를 확인하여 주십시오.");
                    }
                }else{
                    showToast("사원번호를 입력해 주십시오.");
                }
            }
        });

        dialog.show();

        et_login_id.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, TAG+">>et_login_id.onKey() - event.getAction() : "+event.getAction());
                Log.d(TAG, TAG+">>et_login_id.onKey() - keyCode : "+keyCode);
                Log.d(TAG, TAG+">>et_login_id.onKey() - KeyEvent.ACTION_DOWN : "+KeyEvent.ACTION_DOWN);
                Log.d(TAG, TAG+">>et_login_id.onKey() - KeyEvent.KEYCODE_ENTER : "+KeyEvent.KEYCODE_ENTER);

                //확인 버튼 터치 시 진행
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return true;
                }
                return false;
            }
        });

        et_login_pwd.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, TAG+">>et_login_pwd.onKey() - event.getAction() : "+event.getAction());
                Log.d(TAG, TAG+">>et_login_pwd.onKey() - keyCode : "+keyCode);
                Log.d(TAG, TAG+">>et_login_pwd.onKey() - KeyEvent.ACTION_DOWN : "+KeyEvent.ACTION_DOWN);
                Log.d(TAG, TAG+">>et_login_pwd.onKey() - KeyEvent.KEYCODE_ENTER : "+KeyEvent.KEYCODE_ENTER);

                //확인 버튼 터치 시 진행
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Log.d(TAG, TAG+">>et_login_pwd.onKey() - AAA");
                    String swbeonho = et_login_pwd.getText().toString();
                    String password = et_login_pwd.getText().toString();

                    return true;
                }
                return false;
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_login_id.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(et_login_pwd.getWindowToken(), 0);

                moveTaskToBack(true); // 태스크를 백그라운드로 이동
                finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료

            }
        });
    }

    /**
     * kbr 2022.04.04
     */
    public Task<String> getToken() {
        return FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                String token = task.getResult();
                sendToken(token);
            }
        });
    }

//        public void sendToken(){
    public void sendToken(String token){
        final Context context = getApplicationContext();
//        String token = FirebaseMessagingService.token;
        Log.d(TAG, "sendToken() - packageName : "+context.getPackageName());
        Log.d(TAG, "sendToken() - mdn : "+ getMdn(context));
        Log.d(TAG, "sendToken() - imei : "+getImei(context));
        String url = "http://mvote.ex.co.kr/exBus/merge_client.jsp?app_info="+context.getPackageName()+"&device_id="+ getMdn(context)
                +"&device_imei="+getImei(context)+"&device_token="+token+"&device_cd=A";
        Log.d(TAG, "sendToken() - url : "+url);

        if(token!=null && !("").equals(token)){
            Log.d(TAG, "sendToken() TOKEN : "+token);
            HttpUtil.request(context, url, new HttpUtil.HttpCallback() {
                @Override
                public void onResponse(String response) {
                    // TODO Auto-generated method stub

                    try{
                        Log.d(TAG, "sendToken() - response : "+response);
                        XMLData responseXML =  new XMLData(response);
                        if("OK".equals(responseXML.get("resStatus"))){
                            Log.d(TAG, "sendToken() - fcm success : "+responseXML.get("resStatus"));
                            CommonUtil.setPrefString(IntroActivity.this, "fcmDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                            Log.d(TAG, "sendToken() - update token success ..");
                            Log.i(TAG, "sendToken() - fcm date : "+CommonUtil.getPrefString(IntroActivity.this, "fcmDate"));
                            Log.d(TAG, "----------========== fcm date (PUSH) : "+CommonUtil.getPrefString(IntroActivity.this, "fcmDate"));
                        }else{
                            Log.d(TAG, "sendToken() - update token failed ..");
                        }
                    }catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //토스트 메시지 표출
    public void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
//        TelephonyManager mTelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

}