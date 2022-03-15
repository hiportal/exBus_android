package com.ex.exbus.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpConnection implements Runnable {
    private static final String TAG = ".BeaconActivity";

    public interface IHttpResult
    {
        void onHttpResult(String url, String xml);
    }

    public static final String URL_EXBUS_HEADER = "http://mvote.ex.co.kr/exBus/";
    public static final String URL_BEACON_SEND_TAG = "insertBeaconInfo.jsp";
    public static final String URL_USER_INFO = "checkUser.jsp";
    public static final String URL_USER_INFO2 = "checkUser2.jsp";
    public static final String URL_USER_LOG = "insertUserLog.jsp";
    public static final String URL_USER_EVENT = "exbusUserEvent.jsp";
    public static final String URL_EXBUS_VERSION = "exbusVersion.jsp";
    public static final String URL_EXBUS_VERSION_EXUSER = "exbusVersionExUser.jsp";
    public static final String URL_EXBUS_CLOUDPUSH = "http://travel.ex.co.kr/cloudpush/";
    public static final String URL_EXBUS_MERGECLIENTINFO = "mergeClientInfo.do";
    public static final String URL_DRIVER_SENDLOCATION = "exbusInsertLocation.jsp";

    public static class HttpRequest
    {
        public String url_header;
        public String url_tail;
        public String service_key = "";
        public ArrayList<String> arg = new ArrayList<String>();
        public ArrayList<HashMap<String,String>> argMap = new ArrayList<HashMap<String,String>>();
        public ArrayList<String> file = new ArrayList<String>();
        public IHttpResult listener;
    }

    private HttpRequest mRequest;
    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            Log.d(TAG, "handleMessage() - msg : "+msg);

            if ( mRequest != null && mRequest.listener != null )
            {
                String xml = (String)msg.obj;
                mRequest.listener.onHttpResult(mRequest.url_tail, xml);
            }
            return true;
        }
    });

    private boolean mISFile = false;
    String mPushUrl = "";

    public HttpConnection(HttpRequest request)
    {
        Log.d(TAG, "HttpConnection() - : request : " + request);
        mRequest = request;
    }

    public HttpConnection(HttpRequest request, boolean isFile)
    {
        mRequest = request;
        mISFile  = isFile;
    }

    public HttpConnection(HttpRequest request, String pushUrl)
    {
        mRequest = request;
        mPushUrl  = pushUrl;
    }

    public void request()
    {
        Log.d(TAG, "request()~!");
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        try
        {
            Log.d(TAG, "run()~!");
            String xml = "";

            if(!mPushUrl.equals("")){
                xml = __push(mPushUrl);
            }else{
                if ( mISFile )
                {
                    xml = __upload();
                }
                else
                {
                    xml = __connect();
                }
            }

            Message msg = Message.obtain();
            msg.obj = xml;

            Log.d(TAG, "msg :"+ msg);


            mHandler.sendMessage(msg);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Message msg = Message.obtain();
            msg.obj = "";

            mHandler.sendMessage(msg);
        }
    }

    private String __push(String mPushUrl) throws IOException{

        String xml = "";

        URL url = new URL(mPushUrl);

        Log.d(TAG, "__push() - url : "+url);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        conn.setDefaultUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");

        OutputStreamWriter request = new OutputStreamWriter(conn.getOutputStream());
        String data = "";
        ArrayList<String> msg = mRequest.arg;
        for ( int i = 0 ; i < msg.size() ; i+=2 )
        {
            Log.d(TAG, "__connect() - msg : "+msg.get(i).toString());
            if ( i > 0 )
            {
                data = data + "&";
            }
            try
            {
                data = data + URLEncoder.encode(msg.get(i), "UTF-8") + "=" + URLEncoder.encode(msg.get(i+1), "UTF-8");
            }
            catch(Exception e)
            {

            }
        }

        request.write(data);
        request.flush();
        request.close();

        BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        String line = null;
        StringBuffer responseXml = new StringBuffer();

        while ((line = response.readLine()) != null) {
            responseXml.append(line + "\n");
        }

        xml = responseXml.toString();
        Log.d(TAG, "xml : "+xml);

        conn.disconnect();
        return xml;
    }

    private String __connect() throws IOException
    {
        String xml = "";

        String header = mRequest.url_header;

        URL url = new URL(header + mRequest.url_tail);

        Log.d(TAG, "__connect() - url : "+url);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        conn.setDefaultUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        OutputStreamWriter request = new OutputStreamWriter(conn.getOutputStream());
        String data = "";
        ArrayList<String> msg = mRequest.arg;

        for ( int i = 0 ; i < msg.size() ; i+=2 )
        {
            Log.d(TAG, "__connect() - msg : "+msg.get(i).toString());
            if ( i > 0 )
            {
                data = data + "&";
            }
            try
            {
                data = data + URLEncoder.encode(msg.get(i), "UTF-8") + "=" + URLEncoder.encode(msg.get(i+1), "UTF-8");
                Log.d(TAG, "__connect() - data : "+data);
            }
            catch(Exception e)
            {

            }
        }

        Log.d(TAG, "_connect() == " + header + mRequest.url_tail + "?" + data);

        request.write(data);
        request.flush();
        request.close();

        BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        String line = null;
        StringBuffer responseXml = new StringBuffer();

        while ((line = response.readLine()) != null) {
            responseXml.append(line + "\n");
        }

        xml = responseXml.toString();
        Log.d(TAG, "_connect() - xml : "+xml);

        conn.disconnect();
        return xml;
    }

    private String __upload() throws Exception
    {
        String xml = "";

        String header = URL_EXBUS_HEADER;//URL_HEADER;
        URL url = new URL(header + mRequest.url_tail);

        Log.d(TAG, "upload : " + header + mRequest.url_tail);

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDefaultUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

        for ( int i = 0 ; i < mRequest.arg.size() ; i+=2 )
        {
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + mRequest.arg.get(i) + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(new String(mRequest.arg.get(i + 1).getBytes(), "ISO-8859-1"));
            dos.writeBytes(lineEnd);
        }

        for ( int i = 0 ; i < mRequest.file.size() ; i+=3 )
        {
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + mRequest.file.get(i) + "\";filename=\"" + new String(mRequest.file.get(i+1).getBytes(), "ISO-8859-1") + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            FileInputStream fis = new FileInputStream(new File(mRequest.file.get(i+2)));

            int bytesAvailable = fis.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            byte[] buffer = new byte[bufferSize];
            int bytesRead = fis.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                dos.write(buffer, 0, bytesRead);
                bytesAvailable = fis.available();
                bytesRead = fis.read(buffer, 0, bufferSize);
            }

            fis.close();
            dos.writeBytes(lineEnd);
        }

        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        dos.flush();

        BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        String line = null;
        StringBuffer responseXml = new StringBuffer();

        while ((line = response.readLine()) != null) {
            responseXml.append(line + "\n");
        }

        xml = responseXml.toString();
        Log.d(TAG, "xml : "+xml);

        return xml;
    }

    public static String direct_connect(HttpRequest httpRequest) throws IOException
    {
        String xml = "";

        String header = httpRequest.url_header;

        URL url = new URL(header + httpRequest.url_tail);

        Log.d(TAG, "param.url = " + httpRequest.url_tail);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDefaultUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        OutputStreamWriter request = new OutputStreamWriter(conn.getOutputStream());

        String data = "";
        ArrayList<String> msg = httpRequest.arg;
        for ( int i = 0 ; i < msg.size() ; i+=2 )
        {
            if ( i > 0 )
            {
                data = data + "&";
            }
            try
            {
                data = data + URLEncoder.encode(msg.get(i), "UTF-8") + "=" + URLEncoder.encode(msg.get(i+1), "UTF-8");
            }
            catch(Exception e)
            {

            }
        }

        Log.d(TAG, "connect = " + header + httpRequest.url_tail + "?" + data);

        request.write(data);
        request.flush();
        request.close();

        BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        String line = null;
        StringBuffer responseXml = new StringBuffer();

        while ((line = response.readLine()) != null) {
            responseXml.append(line + "\n");
        }

        xml = responseXml.toString();
        Log.d(TAG, "xml : "+xml);

        conn.disconnect();
        return xml;
    }

    public static String connectHttp(String u) throws Exception
    {
        URL url = new URL(u);

        Log.d(TAG, "param.url = " + u);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDefaultUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        InputStream in =  null;

        Log.d(TAG, "status : " + status);
        BufferedReader response = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        String line = null;
        StringBuffer responseXml = new StringBuffer();

        while ((line = response.readLine()) != null) {
            responseXml.append(line + "\n");
        }

        String xml = responseXml.toString();
        Log.d(TAG, xml);

        conn.disconnect();

        return xml;
    }
}
