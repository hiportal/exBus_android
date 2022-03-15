package com.ex.exbus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ex.exbus.util.NotiFerment;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "MainActivity";
    private String launchUrl = "http://mvote.ex.co.kr/exBus/newExbus/index.html";
    private String DriverUrl = "http://mvote.ex.co.kr/exBus/newExbus/exdriver_lane.html";
    WebView webView = null;
    public static Timer timer;

    String id = "";
    String name = "";
    String mobile = "";
    String userType = "";
    String rgstTrgtClssCd = "";
    String version = "";

    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onCreate() - token : "+FirebaseInstanceId.getInstance().getToken());

        if(FirebaseInstanceId.getInstance().getToken() != null){
            Log.d(TAG, "onCreate() - token2 : "+FirebaseInstanceId.getInstance().getToken());
        }

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");
        mobile = intent.getStringExtra("mobile");
        userType = intent.getStringExtra("userType");
        rgstTrgtClssCd = intent.getStringExtra("rgstTrgtClssCd");

        webView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebContentsDebuggingEnabled(true);
        //WebView 화면크기에 맞추도록 설정 - setUseWideViewPort 와 같이 써야함
        webSettings.setLoadWithOverviewMode(true);
        //wide viewport 설정 - setLoadWithOverviewMode 와 같이 써야함
        webSettings.setUseWideViewPort(true);
        //캐시 사용 여부
        webSettings.setAppCacheEnabled(false);
        //캐시 설정
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //쿠키삭제
        webView.clearHistory();

        webView.addJavascriptInterface(new AndroidBridge(),"bridge");

        try {
            PackageInfo ver = this.getPackageManager().getPackageInfo(this.getPackageName(),0);
            version = ver.versionName;
        }catch(PackageManager.NameNotFoundException e){
        }

        if(!rgstTrgtClssCd.equals("D")){
            //사용자
            webView.loadUrl(launchUrl+"?Key="+id+"="+name+"="+mobile+"="+userType+"="+version+"=");
        }else{
            //버스운전자
            webView.loadUrl(DriverUrl+"?Key="+id+"="+name+"="+mobile+"="+userType+"="+rgstTrgtClssCd+"="+version+"=");
        }
    }

    @Override
    public void onBackPressed() {
        webView = (WebView) findViewById(R.id.webView);
        Log.e("뒤로가기", webView.getUrl());

        // 티켓확인 화면 > back 클릭 시 > 무조건 main
        if (webView.getUrl().equals("http://mvote.ex.co.kr/exBus/newExbus/checkTicket.html")) {

            webView.loadUrl(launchUrl);
        }

        if (webView.canGoBack() && !webView.getUrl().equals("http://mvote.ex.co.kr/exBus/newExbus/checkTicket.html")) {
            webView.goBack();

        } else {
            if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
                finish();
                toast.cancel();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(rgstTrgtClssCd.equals("D")){
            stopBasicService();
            //timer.cancel();
        }
        super.onDestroy();
    }

    public void stopBasicService(){
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        stopService(intent);
    }

    public void onStartForegroundService(){
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        intent.setAction("startForeground");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }

    //--------------------------------------------------bridge------------------------------------------------//
    class AndroidBridge{
        @JavascriptInterface
        public void appLogout(){
            Log.d(TAG,">> appLogout()!! ");
            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            intent.putExtra("logoutYN", "Y");
            startActivity(intent);
            finish();
        }

        @JavascriptInterface
        public void btnState(String targetNsCode, String rung_yn, String targetStCd, String targetStNm, String targetStX, String targetStY, String userID){
            NotiFerment nf = new NotiFerment(MainActivity.this);
            if(rung_yn.equals("Y")){
                onStartForegroundService();
            }else{
                stopBasicService();
            }
            if(!rung_yn.equals("") && rung_yn != null){
                nf.locationTransaction(targetNsCode, rung_yn, targetStCd, targetStNm, targetStX, targetStY, userID);
            }
        }

        @JavascriptInterface
        public void call(String driverPhoneNumber){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+driverPhoneNumber));
            startActivity(intent);
        }

    }

}