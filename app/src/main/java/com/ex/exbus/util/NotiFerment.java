package com.ex.exbus.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.ex.exbus.IntroActivity;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class NotiFerment {
    final static String TAG = "NotiFerment";
    Context mContext;
    Handler handler;

    String t_targetNsCode = "";
    String t_rung_yn 		= "";
    String t_targetStCd 	= "";
    String t_targetStNm 	= "";
    String t_targetStX 	= "";
    String t_targetStY 	= "";
    String t_userID 		= "";
    String lat_x = "0.0";
    String lng_y = "0.0";

    double lat = 0.0;
    double lng = 0.0;

    public static Timer timer;
    public static int TIMER_DELAY_Y = 7000;

    public NotiFerment(Context context){
        this.mContext = context;
    }

    //이 메소드는 운행 시 한번 종료시 한번 호출
    public void locationTransaction(String targetNsCode, String rung_yn, String targetStCd, String targetStNm, String targetStX, String targetStY, String userID){
        t_targetNsCode = targetNsCode.trim();
        t_rung_yn = rung_yn.trim();
        t_targetStCd = targetStCd.trim();
        t_targetStNm = targetStNm.trim();
        t_targetStX = targetStX.trim();
        t_targetStY = targetStY.trim();
        t_userID = userID.trim();

        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
        }else{
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                //String provider = location.getProvider();
                lng = location.getLongitude();
                lat = location.getLatitude();
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,gpsLocationListener);
        }

        if(t_rung_yn.equals("Y")){
            re_transaction();
        }else if(t_rung_yn.equals("N")){
            timer.cancel();
            insertLocation(lat_x, lng_y, t_targetNsCode, t_rung_yn, t_targetStCd, t_targetStNm,  t_targetStX, t_targetStY,  t_userID);
        }
    }

    final LocationListener gpsLocationListener = new LocationListener() {

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Log.println(Log.ASSERT,"TAG","onProvider Disalbed"+provider);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.println(Log.ASSERT,"TAG","onProviderEnabled"+ provider);
        }

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public void onLocationChanged(Location location) {
            lng = location.getLongitude();
            lat = location.getLatitude();
            lat_x = Double.toString(lat);
            lng_y = Double.toString(lng);
            Log.println(Log.ASSERT,"TAG","onLocation Changed"+"("+lng+","+lat+")");
            CommonUtil.setPrefString(mContext, "lat_x", lat_x);
            CommonUtil.setPrefString(mContext, "lng_y", lng_y);
            CommonUtil.setPrefString(mContext, "targetStCd", t_targetStCd);
            CommonUtil.setPrefString(mContext, "targetStNm", t_targetStNm);
        }
    };


    public void re_transaction(){
        timer = new Timer();
        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                try {
                    insertLocation(lat_x, lng_y, t_targetNsCode, t_rung_yn, t_targetStCd, t_targetStNm,  t_targetStX, t_targetStY,  t_userID);

                }catch (Exception e){

                }
            }
        };
        timer.schedule(timertask, 0, TIMER_DELAY_Y);// 7초마다 시작
    }

    //내 위치 전송 메소드
    public void insertLocation(String lat_x, String lng_y, String targetNsCode, String rung_yn, String targetStCd, String targetStNm, String targetStX, String targetStY, String userID){
            Log.d(TAG, TAG+">>insertLocation()~!");

            final HttpConnection.HttpRequest request = new HttpConnection.HttpRequest();
            request.url_header = HttpConnection.URL_EXBUS_HEADER;
            request.url_tail = HttpConnection.URL_DRIVER_SENDLOCATION;

            if(rung_yn.equals("Y")){
                request.arg.add("lat_x");
                request.arg.add(lat_x);
                request.arg.add("lng_y");
                request.arg.add(lng_y);
                request.arg.add("targetNsCode");
                request.arg.add(targetNsCode);
                request.arg.add("rung_yn");
                request.arg.add(rung_yn);
                request.arg.add("targetStCd");
                request.arg.add(targetStCd);
                request.arg.add("targetStNm");
                request.arg.add(targetStNm);
                request.arg.add("targetStX");
                request.arg.add(targetStX);
                request.arg.add("targetStY");
                request.arg.add(targetStY);
                request.arg.add("userID");
                request.arg.add(userID);
            }else{
                lat_x = CommonUtil.getPrefString(mContext,"lat_x");
                lng_y = CommonUtil.getPrefString(mContext,"lng_y");
                targetStCd = CommonUtil.getPrefString(mContext,"targetStCd");
                targetStNm = CommonUtil.getPrefString(mContext,"targetStNm");
                request.arg.add("lat_x");
                request.arg.add(lat_x);
                request.arg.add("lng_y");
                request.arg.add(lng_y);
                request.arg.add("targetNsCode");
                request.arg.add(targetNsCode);
                request.arg.add("rung_yn");
                request.arg.add(rung_yn);
                request.arg.add("targetStCd");
                request.arg.add(targetStCd);
                request.arg.add("targetStNm");
                request.arg.add(targetStNm);
                request.arg.add("targetStX");
                request.arg.add(targetStX);
                request.arg.add("targetStY");
                request.arg.add(targetStY);
                request.arg.add("userID");
                request.arg.add(userID);
            }

            request.listener = new HttpConnection.IHttpResult() {
                    @Override
                    public void onHttpResult(String url, String json) {
                        try {
                            JSONObject jsonobject = new JSONObject(json.trim());

                        } catch (Exception e) {
                            Log.d(TAG,"exception: "+e.toString());
                            e.printStackTrace();
                        }
                    }
            };
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //통신
                    HttpConnection conn = new HttpConnection(request);
                    conn.request();
                }
            },0);
    }

}
