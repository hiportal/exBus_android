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

import org.json.JSONException;
import org.json.JSONObject;

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
        //wide viewport ?????? - setLoadWithOverviewMode ??? ?????? ?????????
        webSettings.setLoadWithOverviewMode(true);
        //WebView ??????????????? ???????????? ?????? - setUseWideViewPort ??? ?????? ?????????
        webSettings.setUseWideViewPort(true);
        //?????? ?????? ??????
        webSettings.setAppCacheEnabled(false);
        //?????? ??????
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //????????????
        webView.clearHistory();

        webView.addJavascriptInterface(new AndroidBridge(),"bridge");

        try {
            PackageInfo ver = this.getPackageManager().getPackageInfo(this.getPackageName(),0);
            version = ver.versionName;
        }catch(PackageManager.NameNotFoundException e){
        }

        if(!rgstTrgtClssCd.equals("D")){
            //?????????
            webView.loadUrl(launchUrl+"?Key="+id+"="+name+"="+mobile+"="+userType+"="+version+"=");
        }else{
            //???????????????
            webView.loadUrl(DriverUrl+"?Key="+id+"="+name+"="+mobile+"="+userType+"="+rgstTrgtClssCd+"="+version+"=");
        }
    }

    @Override
    public void onBackPressed() {
        webView = (WebView) findViewById(R.id.webView);
        Log.e("????????????", webView.getUrl());

        // ???????????? ?????? > back ?????? ??? > ????????? main
        String click_url = webView.getUrl();

        if (click_url.equals("http://mvote.ex.co.kr/exBus/newExbus/checkTicket.html")
                || click_url.equals("http://mvote.ex.co.kr/exBus/newExbus/trainCheckTicket.html") ) {

            if(!rgstTrgtClssCd.equals("D")){
                //?????????
                webView.loadUrl(launchUrl+"?Key="+id+"="+name+"="+mobile+"="+userType+"="+version+"=");
            }else{
                //???????????????
                webView.loadUrl(DriverUrl+"?Key="+id+"="+name+"="+mobile+"="+userType+"="+rgstTrgtClssCd+"="+version+"=");
            }
        }

        if (webView.canGoBack()
                && (!click_url.equals("http://mvote.ex.co.kr/exBus/newExbus/checkTicket.html")
                &&  !click_url.equals("http://mvote.ex.co.kr/exBus/newExbus/trainCheckTicket.html")
                && !(click_url.indexOf("http://mvote.ex.co.kr/exBus/newExbus/index.html") > -1))) {

            webView.goBack();

        } else {

            if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "???????????? ????????? ?????? ??? ???????????? ???????????????.", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            //??? ???????????? ????????????
            if((click_url.indexOf("http://mvote.ex.co.kr/exBus/newExbus/index.html") > -1) && (System.currentTimeMillis() <= backKeyPressedTime + 2500)){
                moveTaskToBack(true); // ???????????? ?????????????????? ??????
                finishAndRemoveTask(); // ???????????? ?????? + ????????? ??????????????? ?????????
                android.os.Process.killProcess(android.os.Process.myPid()); // ??? ???????????? ??????
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

        @JavascriptInterface
        public String getGps() throws JSONException {
            String gpsParam = "";

            GpsTracker gpsTracker = new GpsTracker(MainActivity.this);
            double latitude = gpsTracker.getLatitude(); // ??????
            double longitude = gpsTracker.getLongitude(); //??????

            Log.d(TAG,"latitude= " + latitude);
            Log.d(TAG,"longitude= " + longitude);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("latitude", latitude);
            jsonObj.put("longitude", longitude);

            if(jsonObj == null){
                gpsParam = "noneData";
            }else{
                gpsParam = jsonObj.toString();
            }

            return gpsParam;
        }

    }

}