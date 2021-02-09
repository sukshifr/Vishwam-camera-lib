package com.jukshio.jwccgateapplib.Networking;

/*
 * Vishwam Corp CONFIDENTIAL

 * Vishwam Corp 2018
 * All Rights Reserved.

 * NOTICE:  All information contained herein is, and remains
 * the property of Vishwam Corp. The intellectual and technical concepts contained
 * herein are proprietary to Vishwam Corp
 * and are protected by trade secret or copyright law of U.S.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vishwam Corp
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.jukshio.jwccgateapplib.ImageAnalysis.ImageAnalysis;
import com.jukshio.jwccgateapplib.R;
import com.jukshio.jwccgateapplib.SDKinitializationCallback;
import com.jukshio.jwccgateapplib.Utils2;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static okhttp3.Protocol.HTTP_1_1;

/**
 * This is helper class used all network operations using okhttp.
 * **/
public class VishwamNetworkHelper {
    private APICompletionCallback onResponseReceived;
    private String regPathFull="", regPath224="", authPath224="", authPathFull="";
    public Context context;
    private int env;
    private String app_id="";
    private String authTok = "", skip_auth="", type = "";
    private String version="", user_id="", delay="", phonemodel ="", regDomainUrl="", authDomainUrl="", secureKey="",org_id="";

    private int user_ref_timeout, reg_timeout, auth_timeout;
    Activity activity;
    ProgressDialog progressDialog;
    public static String loadedModelName = "";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String sdkModelUrl = "", sdkModelPath = "", sdkModelKey = "", sdkModelName = "";
    String assetModelName = "08012021_A.tflite";
    public static String TAG1 = "stepCheck";
    boolean isAndroid = false;
    SDKinitializationCallback sdKinitializationCallback;
    public static ImageAnalysis imageAnalysis;
    File modelfile;
    String checkSumKey = "",sdkDomainUrl = "";
    /**
     * Constructor to initialise with context and string appid and type of environment.
     * This is also used to get auth token by generating jwt token for passing on all network calls.
     * **/
    public VishwamNetworkHelper(int env, Context context1, Activity activity) {
        this.context = context1;
        this.activity = activity;
        this.env = env;
        prefs = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        editor = prefs.edit();
        initLogs(env);
    }

    public void initSDK(int env, String domainUrl, JSONObject apiParams, SDKinitializationCallback sdKinitializationCallback) {

        this.sdkDomainUrl = domainUrl;
        this.env = env;
        this.sdKinitializationCallback = sdKinitializationCallback;

        try {
            if (apiParams.has("app_id")) {
                app_id = apiParams.getString("app_id");
            } else {
                app_id = "";
            }
            if (apiParams.has("device_model")) {
                this.phonemodel = apiParams.getString("device_model");
            } else {
                this.phonemodel = "";
            }
            if (apiParams.has("checksumKey")) {
                this.checkSumKey = apiParams.getString("checksumKey");
            } else {
                this.checkSumKey = context.getResources().getString(R.string.key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new ApiCallForCheckModelUpdate().execute();
      /*  String previousDateString = prefs.getString("date_string", "");
        String currentDate = getDateString();

        if (!previousDateString.equals("")) {
            if (!currentDate.equals(previousDateString)) {
                if (checkInternetConnection()) {
//                    new ApiCallFordownloadModelUpdate().execute();
                    new ApiCallForCheckModelUpdate().execute();
                } else {
//                    Toast.makeText(context, "No Active Internet Connection found", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (checkInternetConnection()) {
//                new ApiCallFordownloadModelUpdate().execute();
                new ApiCallForCheckModelUpdate().execute();
            } else {
//                Toast.makeText(context, "No Active Internet Connection found", Toast.LENGTH_SHORT).show();
            }
        }*/

    }

    public void makeCheckUserRegCall(int env, String domainUrl, JSONObject apiRegParams, APICompletionCallback onResponseReceived) {
        this.onResponseReceived = onResponseReceived;

        try {
            this.env = env;
            this.regDomainUrl = domainUrl;

            if (apiRegParams.has("device_model")) {
                this.phonemodel = apiRegParams.getString("device_model");
            }else{
                this.phonemodel = "";
            }

            if (apiRegParams.has("app_id")) {
                this.app_id = apiRegParams.getString("app_id");
            } else {
                this.app_id = "";
             }

            if (apiRegParams.has("secure_key")) {
                this.secureKey = apiRegParams.getString("secure_key");
            } else {
                this.secureKey = "";
            }

            if (apiRegParams.has("user_id")) {
                this.user_id = apiRegParams.getString("user_id");

            } else {
                this.user_id = "";
            }
            if (apiRegParams.has("timeout_secs")) {
                this.user_ref_timeout = apiRegParams.getInt("timeout_secs");

            } else {
                this.user_ref_timeout = 10;
            }
            if (apiRegParams.has("org_id")) {
                this.org_id = apiRegParams.getString("org_id");

            } else {
                this.org_id = "";
            }

            if (!app_id.equals("") ){
                if (!user_id.equals("")){
                    if (!secureKey.equals("")){
                        if (checkInternetConnection()) {
                            new ApiCallForCheckUserReg().execute();
                        } else {
                            logNoInternetEvent("UserRefApi_102",user_id);
                            this.onResponseReceived.onResponse("102", new VishwamError(VishwamError.NO_INTERNET, "No Active Internet Connection found"), null, null);
                        }
                    }else {
                        logApiErrorEvent(user_id,"UserRef_100_securekey not found");
                        this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_SECURE_KEY, "securekey not found"), null,null);

                    }
                }else {
                    logApiErrorEvent(user_id,"UserRef_100_user_id not found");
                    this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_USER_ID, "user_id not found"), null, null);

                }
            }else {
                logApiErrorEvent(user_id,"UserRef_100_app_id not found");
                this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_APP_ID, "app_id not found"), null, null);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Method used making ocr read document network call by passing  different parameters and APICompletionCallback to handover the response to other activity.
     * **/
    public void makeRegCall(int env, String domainUrl, String imgPath224, String imgPathFull, JSONObject apiRegParams, APICompletionCallback onResponseReceived) {
        this.onResponseReceived = onResponseReceived;

        try {

            this.env = env;
            this.regPath224 = imgPath224;
            this.regPathFull = imgPathFull;
            this.regDomainUrl = domainUrl;
//            phonemodel = Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE;

            if (apiRegParams.has("version")) {
                this.version = apiRegParams.getString("version");
            }else{
                this.version = "";
            }

            if (apiRegParams.has("skip_auth")) {
                this.skip_auth = apiRegParams.getString("skip_auth");
            }else{
                this.skip_auth = "1";
            }

            if (apiRegParams.has("type")) {
                this.type = apiRegParams.getString("type");
            }else{
                this.type = "0";
            }

            if (apiRegParams.has("device_model")) {
                this.phonemodel = apiRegParams.getString("device_model");
            }else{
                this.phonemodel = "";
            }

            if (apiRegParams.has("app_id")) {
                this.app_id = apiRegParams.getString("app_id");
            } else {
                this.app_id = "";
            }

            if (apiRegParams.has("timeout_secs")) {
                this.reg_timeout = apiRegParams.getInt("timeout_secs");

            } else {
                this.reg_timeout = 10;
            }

            if (apiRegParams.has("secure_key")) {
                this.secureKey = apiRegParams.getString("secure_key");
            } else {
                this.secureKey = "";
            }

            if (apiRegParams.has("user_id")) {
                this.user_id = apiRegParams.getString("user_id");
            } else {
                this.user_id = "";
            }
            if (apiRegParams.has("org_id")) {
                this.org_id = apiRegParams.getString("org_id");

            } else {
                this.org_id = "";
            }

            if (!app_id.equals("") ){
                if (!user_id.equals("")){
                    if (!secureKey.equals("")){
                        if (checkInternetConnection()) {
                            new ApiCallForReg().execute();
                        } else {
                            logNoInternetEvent("RegApi_102",user_id);
                            this.onResponseReceived.onResponse("102", new VishwamError(VishwamError.NO_INTERNET, "No Active Internet Connection found"), null, null);
                        }
                    }else {
                        logApiErrorEvent(user_id,"Reg_100_securekey not found");
                        this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_SECURE_KEY, "securekey not found"), null,null);

                    }
                }else {
                    logApiErrorEvent(user_id,"Reg_100_user_id not found");
                    this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_USER_ID, "user_id not found"), null, null);

                }
            }else {
                logApiErrorEvent(user_id,"Reg_100_app_id not found");
                this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_APP_ID, "app_id not found"), null, null);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * Method used making face match network call by passing  different parameters and APICompletionCallback to handover the response to other activity.
     * **/
    public void makeFaceLookupCall(int env, String domainUrl, String authPath224, String authPathFull, JSONObject apiAuthParams, APICompletionCallback onResponseReceived) {
        this.onResponseReceived = onResponseReceived;

        try {

            this.env = env;
            this.authPath224 = authPath224;
            this.authPathFull = authPathFull;
            this.authDomainUrl = domainUrl;
//            phonemodel = Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE;

            if (apiAuthParams.has("version")) {
                this.version = apiAuthParams.getString("version");
            }else{
                this.version = "";
            }

            if (apiAuthParams.has("device_model")) {
                this.phonemodel = apiAuthParams.getString("device_model");
            }else{
                this.phonemodel = "";
            }

            if (apiAuthParams.has("delay")) {
                this.delay = apiAuthParams.getString("delay");
            }else {
                this.delay = "";
            }

            if (apiAuthParams.has("app_id")) {
                this.app_id = apiAuthParams.getString("app_id");
            } else {
                this.app_id = "";
            }

            if (apiAuthParams.has("timeout_secs")) {
                this.auth_timeout = apiAuthParams.getInt("timeout_secs");

            } else {
                this.auth_timeout = 10;
            }

            if (apiAuthParams.has("secure_key")) {
                this.secureKey = apiAuthParams.getString("secure_key");
            } else {
                this.secureKey = "";
            }

            if (apiAuthParams.has("user_id")) {
                this.user_id = apiAuthParams.getString("user_id");
            } else {
                this.user_id="";
            }
            if (apiAuthParams.has("org_id")) {
                this.org_id = apiAuthParams.getString("org_id");

            } else {
                this.org_id = "";
            }

            if (!app_id.equals("") ){
//                if (!user_id.equals("")){
                    if (!secureKey.equals("")){
                        if (checkInternetConnection()) {
//                            new ApiCallForAuthentication().execute();
                            new ApiCallForFaceLookup().execute();
                        } else {
                            logNoInternetEvent("AuthApi_102",user_id);
                            this.onResponseReceived.onResponse("102", new VishwamError(VishwamError.NO_INTERNET, "No Active Internet Connection found"), null, null);
                        }
//                    }else {
//                        logApiErrorEvent(user_id,"Auth_100_securekey not found");
//                        this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_SECURE_KEY, "securekey not found"), null, null);
//                    }
                }else {
                    logApiErrorEvent(user_id,"Auth_100_user_id not found");
                    this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_APP_ID, "user_id not found"), null, null);
                }
            }else {
                logApiErrorEvent(user_id,"Auth_100_app_id not found");
                this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_APP_ID, "app_id not found"), null, null);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used making face match network call by passing  different parameters and APICompletionCallback to handover the response to other activity.
     * **/
    public void makeVerifyFRCall(int env, String domainUrl, String authPath224, String authPathFull, JSONObject apiAuthParams, APICompletionCallback onResponseReceived) {
        this.onResponseReceived = onResponseReceived;

        try {

            this.env = env;
            this.authPath224 = authPath224;
            this.authPathFull = authPathFull;
            this.authDomainUrl = domainUrl;
//            phonemodel = Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE;

            if (apiAuthParams.has("version")) {
                this.version = apiAuthParams.getString("version");
            }else{
                this.version = "";
            }

            if (apiAuthParams.has("device_model")) {
                this.phonemodel = apiAuthParams.getString("device_model");
            }else{
                this.phonemodel = "";
            }

            if (apiAuthParams.has("delay")) {
                this.delay = apiAuthParams.getString("delay");
            }else {
                this.delay = "";
            }

            if (apiAuthParams.has("app_id")) {
                this.app_id = apiAuthParams.getString("app_id");
            } else {
                this.app_id = "";
            }

            if (apiAuthParams.has("timeout_secs")) {
                this.auth_timeout = apiAuthParams.getInt("timeout_secs");

            } else {
                this.auth_timeout = 10;
            }

            if (apiAuthParams.has("secure_key")) {
                this.secureKey = apiAuthParams.getString("secure_key");
            } else {
                this.secureKey = "";
            }

            if (apiAuthParams.has("user_id")) {
                this.user_id = apiAuthParams.getString("user_id");
            } else {
                this.user_id="";
            }
            if (apiAuthParams.has("org_id")) {
                this.org_id = apiAuthParams.getString("org_id");

            } else {
                this.org_id = "";
            }

            if (!app_id.equals("") ){
                if (!user_id.equals("")){
                if (!secureKey.equals("")){
                    if (checkInternetConnection()) {
                            new ApiCallForAuthentication().execute();
                    } else {
                        logNoInternetEvent("AuthApi_102",user_id);
                        this.onResponseReceived.onResponse("102", new VishwamError(VishwamError.NO_INTERNET, "No Active Internet Connection found"), null, null);
                    }
                    }else {
                        logApiErrorEvent(user_id,"Auth_100_securekey not found");
                        this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_SECURE_KEY, "securekey not found"), null, null);
                    }
                }else {
                    logApiErrorEvent(user_id,"Auth_100_user_id not found");
                    this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_APP_ID, "user_id not found"), null, null);
                }
            }else {
                logApiErrorEvent(user_id,"Auth_100_app_id not found");
                this.onResponseReceived.onResponse("100", new VishwamError(VishwamError.NO_APP_ID, "app_id not found"), null, null);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ApiCallForCheckUserReg extends AsyncTask<String, String, CrashProof> {

        @Override
        protected CrashProof doInBackground(String... strings) {
            apiCallLog("CheckUserReg",user_id);

            String url = null, server = regDomainUrl;
            url = "https://" + server + "/v1/check_user_reg";


//            authTok = JwtGenerator.getToken(app_id, user_id, secureKey);
            //            Log.e("prefesAuthTOken1", authTok);

            CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add(server, "sha256/" + context.getString(R.string.certificateKey))
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(user_ref_timeout, TimeUnit.SECONDS)
                    .writeTimeout(user_ref_timeout, TimeUnit.SECONDS)
                    .readTimeout(user_ref_timeout, TimeUnit.SECONDS)
                    .protocols(Collections.singletonList(HTTP_1_1))
                    .certificatePinner(certificatePinner)
                    .connectionPool(new ConnectionPool(0,1, TimeUnit.NANOSECONDS))
                    .build();

            MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
            multipartBodyBuilder.setType(MultipartBody.FORM); //this may not be needed
//            multipartBodyBuilder.addFormDataPart("type", "0");
            multipartBodyBuilder.addFormDataPart("app_id", app_id);
            multipartBodyBuilder.addFormDataPart("deviceModel",phonemodel);
            multipartBodyBuilder.addFormDataPart("deviceOs", "A");
            multipartBodyBuilder.addFormDataPart("org_id",org_id);
//            multipartBodyBuilder.addFormDataPart("skip_auth", "1");

            Request request = new Request.Builder()
                    .header("Authorization", "Bearer " + authTok)
                    .url(url)
                    .post(multipartBodyBuilder.build())
                    .build();

            Response regResponse;
            CrashProof crashProof;

            try {
                regResponse = client.newCall(request).execute();
//                Log.e("vishwamRefResp", String.valueOf(regResponse));

               /* if (regResponse.code() == 400) {
                    crashProof = new CrashProof("Bad Request", null,0);
                    crashProof.setResponseCode(400);
                    return crashProof;
                } else if (regResponse.code() == 401) {
                    crashProof = new CrashProof("Unathorised request", null,0);
                    crashProof.setResponseCode(401);
                    return crashProof;
                } else if (regResponse.code() == 500) {
                    crashProof = new CrashProof("Internal Server Error", null,0);
                    crashProof.setResponseCode(regResponse.code());
                    return crashProof;
                }else*/
               if ( regResponse.code() == 502) {
                    crashProof = new CrashProof("Bad Gateway", null,0);
                    crashProof.setResponseCode(String.valueOf(regResponse.code()));
                    return crashProof;
               } else {
                    //Log.e("xcall", reqId);

                    ResponseBody responseBody = regResponse.body();
                    JSONObject jsonObject = new JSONObject(responseBody.string());
//                    Log.e("vishwamRefRespBody", jsonObject.toString());
//                        String statusCode = jsonObject.getString("statusCode");

                    Headers respHeaders = regResponse.headers();
//                    Log.e("vishwamRefRespHead", respHeaders.toString());

                    HashMap<String, String> result = new HashMap<String, String>();
                    for (int i = 0, size = respHeaders.size(); i < size; i++) {
                        result.put(respHeaders.name(i) , respHeaders.value(i));
                    }
//                    Log.e("vishwamRefRespHeadMap", result.toString());

                    JSONObject headerObject = new JSONObject(result);
//                    Log.e("vishwamRefRespHeadJO", headerObject.toString());

//                        String xCallid = headerObject.getString("X-Callid");
//                        Log.e("vishwamRegxCallid", xCallid);

                    crashProof = new CrashProof(null, regResponse,0);
                    crashProof.setResponseCode(String.valueOf(regResponse.code()));
                    crashProof.setResponseHeadJson(headerObject);
//                        crashProof.setBodyStatusCode(statusCode);
                    crashProof.setResponseBodyJson(jsonObject);
                    return crashProof;
               }
            } catch (IOException e) {
//                Log.e("vishwamRefIO", e.toString());
                return new CrashProof(e.getMessage(), null,1);
            } catch (JSONException e) {
//                Log.e("vishwamRefJE", e.toString());
                return new CrashProof("JsonException:"+e.getMessage(), null,2);
            }
        }

        @Override
        protected void onPostExecute(CrashProof crashProof) {
            super.onPostExecute(crashProof);

            if (crashProof.getErrorType() != null) {
                if (crashProof.getExceptionCode() == 1) {
                    if (crashProof.getErrorType().equals("timeout") || crashProof.getErrorType().equals("SSL handshake timed out") || crashProof.getErrorType().contains("I/O error during system call, Software caused connection abort")) {
                        logApiErrorEvent(user_id, "userRef_AT408_" + crashProof.getErrorType());
                        onResponseReceived.onResponse("AT408", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Socket Time out"), null, null);
                    } else if (crashProof.getErrorType().contains("failed to connect to") ) {
                        logApiErrorEvent(user_id, "userRef_AT218_" + crashProof.getErrorType());
                        onResponseReceived.onResponse("AT218", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Connection Time out"), null, null);
                    } else {
                        logApiErrorEvent(user_id, "userRef_100_" + crashProof.getErrorType());
                        onResponseReceived.onResponse("100", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Request timed out"), null, null);
                    }
                } else if (crashProof.getExceptionCode() == 2) {
                    logApiErrorEvent(user_id, "userRef_101_" + crashProof.getErrorType());
                    onResponseReceived.onResponse("101", new VishwamError(VishwamError.RESPONSE_JSON_E, "Json Exception"), null, null);
                } else {
                    logApiErrorEvent(user_id, "userRef_" + crashProof.getResponseCode() + "_" + crashProof.getErrorType());
                    onResponseReceived.onResponse(crashProof.getResponseCode(), new VishwamError(VishwamError.REQUEST_TIME_OUT, crashProof.getErrorType()), null, null);
                }
            } else {
                if (crashProof.getResponse() != null){
                    ResponseBody responseBody = crashProof.getResponse().body();
                    JSONObject jsonObject = crashProof.getResponseBodyJson();
                    JSONObject headers = crashProof.getResponseHeadJson();

                    logApiSuccessEvent(user_id, "UserRefApiCall_" + crashProof.getResponseCode());
                    onResponseReceived.onResponse(crashProof.getResponseCode(), null, jsonObject, headers);
                }else {
                    logApiErrorEvent(user_id, "userRef_AT217_" + crashProof.getErrorType());
                    onResponseReceived.onResponse("AT217", new VishwamError(VishwamError.REQUEST_TIME_OUT, "No HTTPResponse"), null, null);
                }
            }


            /*else if (crashProof.getResponse() != null) {

                if (crashProof.getHeaderStatusCode() == 502 || crashProof.getHeaderStatusCode() == 500) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.INTERNAL_SERVER_ERROR, "Internal Server Error"), null, null);
                } else if (crashProof.getHeaderStatusCode() == 400) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.BAD_REQUEST, "Bad request"), null, null);
                }else if (crashProof.getResponse().code() == 401) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.UN_AUTH, "Request not authorized"), null, null);
                } *//*else if (crashProof.getHeaderStatusCode() == 410) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.UNIQUE_ID_ERROR, "Unique ID error"), null);
                }  else if (crashProof.getHeaderStatusCode() == 424) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.IMAGE_DEFECT, "Image defect"), null);
                }else if (crashProof.getHeaderStatusCode() == 200) {

                    ResponseBody responseBody = crashProof.getResponse().body();
                    JSONObject jsonObject = crashProof.getResponseBodyJson();

                    onResponseReceived.onResponse(null, jsonObject);

                }*//*
                else {

                    ResponseBody responseBody = crashProof.getResponse().body();
                    JSONObject jsonObject = crashProof.getResponseBodyJson();
                    JSONObject headers = crashProof.getResponseHeadJson();

                    onResponseReceived.onResponse(null, jsonObject, headers);

                    *//*try {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.BAD_REQUEST, jsonObject.getString("error")), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*//*
                }
            } else {
                onResponseReceived.onResponse(new VishwamError(VishwamError.RESPONSE_NULL, "Request Failed"), null, null);
            }*/
        }
    }

    /**
     * This is AsyncTask to make read document network call creating new thread and passing on the response to main Ui thread
     * This uses okhttp lib with multipart builder to build with  ocr documents images.
     * **/
    @SuppressLint("StaticFieldLeak")
    public class ApiCallForReg extends AsyncTask<String, String, CrashProof> {

        @Override
        protected CrashProof doInBackground(String... strings) {

            apiCallLog("Reg",user_id);

            String url = null, server = regDomainUrl;
            url = "https://" + server + "/v1/me/reference";


//            authTok = JwtGenerator.getToken(app_id, user_id, secureKey);
            //            Log.e("prefesAuthTOken1", authTok);

            CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add(server, "sha256/" + context.getString(R.string.certificateKey))
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(reg_timeout, TimeUnit.SECONDS)
                    .writeTimeout(reg_timeout, TimeUnit.SECONDS)
                    .readTimeout(reg_timeout, TimeUnit.SECONDS)
                    .protocols(Collections.singletonList(HTTP_1_1))
                    .certificatePinner(certificatePinner)
                    .connectionPool(new ConnectionPool(0,1, TimeUnit.NANOSECONDS))
                    .build();

            File imageFile = new File(regPath224);
            File imageFile2= new File(regPathFull);

                //Log.e("fullDocImageSize", String.valueOf(Integer.parseInt(String.valueOf(imageFile.length()/1024))));

            MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
            multipartBodyBuilder.setType(MultipartBody.FORM); //this may not be needed
            multipartBodyBuilder.addFormDataPart("image", imageFile.getName(), RequestBody.create(MediaType.parse("image/jpg"), imageFile));
            multipartBodyBuilder.addFormDataPart("image2", imageFile2.getName(), RequestBody.create(MediaType.parse("image/jpg"), imageFile2));
//            multipartBodyBuilder.addFormDataPart("user_id", user_id);
            multipartBodyBuilder.addFormDataPart("type", type);
            multipartBodyBuilder.addFormDataPart("app_id", app_id);
            multipartBodyBuilder.addFormDataPart("deviceModel",phonemodel);
            multipartBodyBuilder.addFormDataPart("deviceOs", "A");
            multipartBodyBuilder.addFormDataPart("version", version);
            multipartBodyBuilder.addFormDataPart("skip_auth", skip_auth);
            multipartBodyBuilder.addFormDataPart("org_id",org_id);

            Request request = new Request.Builder()
                    .header("Authorization", "Bearer " + authTok)
                    .url(url)
                    .post(multipartBodyBuilder.build())
                    .build();

            Response regResponse;
            CrashProof crashProof;

            try {
                regResponse = client.newCall(request).execute();
//                    Log.e("vishwamRegResp", String.valueOf(regResponse));

                /*if (regResponse.code() == 400) {
                    crashProof = new CrashProof("Bad Request", null,0);
                    crashProof.setResponseCode(400);
                    return crashProof;
                } else if (regResponse.code() == 401) {
                    crashProof = new CrashProof("Unathorised request", null,0);
                    crashProof.setResponseCode(401);
                    return crashProof;
                } else if (regResponse.code() == 500 || regResponse.code() == 502) {
                    crashProof = new CrashProof("Internal Server Error", null,0);
                    crashProof.setResponseCode(regResponse.code());
                    return crashProof;
                } else*/
                if (regResponse.code() == 502) {
                    crashProof = new CrashProof("Bad Gateway", null,0);
                    crashProof.setResponseCode(String.valueOf(regResponse.code()));
                    return crashProof;
                }
                else {
                    //Log.e("xcall", reqId);

                    ResponseBody responseBody = regResponse.body();
                    JSONObject jsonObject = new JSONObject(responseBody.string());
//                        Log.e("vishwamRegRespBody", jsonObject.toString());
//                        String statusCode = jsonObject.getString("statusCode");

                    Headers respHeaders = regResponse.headers();
//                        Log.e("vishwamRegRespHead", respHeaders.toString());

                    HashMap<String, String> result = new HashMap<String, String>();
                    for (int i = 0, size = respHeaders.size(); i < size; i++) {
                        result.put(respHeaders.name(i) , respHeaders.value(i));
                    }
//                        Log.e("vishwamRegRespHeadMap", result.toString());

                    JSONObject headerObject = new JSONObject(result);
//                        Log.e("vishwamRegRespHeadJO", headerObject.toString());

//                        String xCallid = headerObject.getString("X-Callid");
//                        Log.e("vishwamRegxCallid", xCallid);

                    crashProof = new CrashProof(null, regResponse,0);
                    crashProof.setResponseCode(String.valueOf(regResponse.code()));
                    crashProof.setResponseHeadJson(headerObject);
//                        crashProof.setBodyStatusCode(statusCode);
                    crashProof.setResponseBodyJson(jsonObject);
                    return crashProof;
                }
            } catch (IOException e) {
//                    Log.e("vishwamIO", e.getMessage());
                return new CrashProof(e.getMessage(), null,1);

            } catch (JSONException e) {
//                    Log.e("vishwamJE", e.getMessage());
                return new CrashProof("JsonException:"+e.getMessage(), null,2);
            }
        }

        @Override
        protected void onPostExecute(CrashProof crashProof) {
            super.onPostExecute(crashProof);

            if (crashProof.getErrorType() != null) {
                if (crashProof.getExceptionCode() == 1) {
                    if (crashProof.getErrorType().equals("timeout") || crashProof.getErrorType().equals("SSL handshake timed out") || crashProof.getErrorType().contains("I/O error during system call, Software caused connection abort")){
//                            Log.e("vishwamIO_AT408", crashProof.getErrorType());
                        logApiErrorEvent(user_id,"Reg_AT408_"+crashProof.getErrorType());
                        onResponseReceived.onResponse("AT408",new VishwamError(VishwamError.REQUEST_TIME_OUT, "Socket timed out"), null, null);
                    }else if (crashProof.getErrorType().contains("failed to connect to")){
//                            Log.e("vishwamIO_AT218", crashProof.getErrorType());
                        logApiErrorEvent(user_id,"Reg_AT218_"+crashProof.getErrorType());
                        onResponseReceived.onResponse("AT218", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Connection timed out"), null, null);
                    }else {
//                            Log.e("vishwamIO_100", crashProof.getErrorType());
                        logApiErrorEvent(user_id,"Reg_100_"+crashProof.getErrorType());
                        onResponseReceived.onResponse("100",new VishwamError(VishwamError.REQUEST_TIME_OUT, "Request timed out"), null, null);
                    }
                }else if (crashProof.getExceptionCode() == 2) {
                    logApiErrorEvent(user_id, "Reg_100_"+crashProof.getErrorType());
                    onResponseReceived.onResponse("101",new VishwamError(VishwamError.RESPONSE_JSON_E, "Json Exception"), null, null);
                }else {
                    logApiErrorEvent(user_id, "Reg_"+crashProof.getResponseCode()+"_"+crashProof.getErrorType());
                    onResponseReceived.onResponse(crashProof.getResponseCode(),new VishwamError(VishwamError.REQUEST_TIME_OUT, crashProof.getErrorType()), null, null);
                }
            } else {
                if (crashProof.getResponse()!=null){
                    ResponseBody responseBody = crashProof.getResponse().body();
                    JSONObject jsonObject = crashProof.getResponseBodyJson();
                    JSONObject headers = crashProof.getResponseHeadJson();

                    logApiSuccessEvent(user_id,"RegApiCall_"+crashProof.getResponseCode());
                    onResponseReceived.onResponse(crashProof.getResponseCode(),null, jsonObject, headers);
                }else {
                    logApiErrorEvent(user_id,"Reg_AT217_"+crashProof.getErrorType());
                    onResponseReceived.onResponse("AT217",new VishwamError(VishwamError.REQUEST_TIME_OUT, "No HttpResponse"), null, null);
                }
            }



            /*else if (crashProof.getResponse() != null) {

                if (crashProof.getHeaderStatusCode() == 502 || crashProof.getHeaderStatusCode() == 500) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.INTERNAL_SERVER_ERROR, "Internal Server Error"), null, null);
                } else if (crashProof.getHeaderStatusCode() == 400) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.BAD_REQUEST, "Bad request"), null, null);
                }else if (crashProof.getResponse().code() == 401) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.UN_AUTH, "Request not authorized"), null, null);
                } *//*else if (crashProof.getHeaderStatusCode() == 410) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.UNIQUE_ID_ERROR, "Unique ID error"), null);
                }  else if (crashProof.getHeaderStatusCode() == 424) {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.IMAGE_DEFECT, "Image defect"), null);
                }else if (crashProof.getHeaderStatusCode() == 200) {

                    ResponseBody responseBody = crashProof.getResponse().body();
                    JSONObject jsonObject = crashProof.getResponseBodyJson();

                    onResponseReceived.onResponse(null, jsonObject);

                }*//*
                else {

                    ResponseBody responseBody = crashProof.getResponse().body();
                    JSONObject jsonObject = crashProof.getResponseBodyJson();
                    JSONObject headers = crashProof.getResponseHeadJson();

                    onResponseReceived.onResponse(null, jsonObject, headers);

                    *//*try {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.BAD_REQUEST, jsonObject.getString("error")), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*//*
                }
            } else {
                onResponseReceived.onResponse(new VishwamError(VishwamError.RESPONSE_NULL, "Request Failed"), null, null);
            }*/
        }
    }

    /**
     * This is AsyncTask to make face match network call creating new thread and passing on the response to main Ui thread
     * This uses okhttp lib with multipart builder to build with  faces images.
     * **/
    @SuppressLint("StaticFieldLeak")
    public class ApiCallForFaceLookup extends AsyncTask<String, String, CrashProof> {

        @SuppressLint("LongLogTag")
        @Override
        protected CrashProof doInBackground(String... strings) {
            apiCallLog("Auth",user_id);

            String url = "", server = authDomainUrl;;
//            url = "https://" + server + "/v1/single_gesture";
            url = "https://" + server + "/v1/face_lookup";

            CertificatePinner certificatePinner = null;
            Response authResponse;
            CrashProof crashProof;

            if (!server.equals("")) {
                certificatePinner = new CertificatePinner.Builder()
                        .add(server, "sha256/" + context.getString(R.string.certificateKey))
                        .build();
            } else {
                crashProof= new CrashProof("Bad Request params", null, 0);
                crashProof.setResponseCode("100");
                return crashProof;
            }


//            authTok = JwtGenerator.getToken(app_id, user_id, secureKey);
//            Log.e("prefesAuthTOken1", authTok);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(auth_timeout, TimeUnit.SECONDS)
                    .writeTimeout(auth_timeout, TimeUnit.SECONDS)
                    .readTimeout(auth_timeout, TimeUnit.SECONDS)
                    .protocols(Collections.singletonList(HTTP_1_1))
                    .certificatePinner(certificatePinner)
                    .connectionPool(new ConnectionPool(0,1, TimeUnit.NANOSECONDS))
                    .build();

            File imageFile = new File(authPath224);
            File imageFile2 = new File(authPathFull);

            MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
            multipartBodyBuilder.setType(MultipartBody.FORM);
            multipartBodyBuilder.addFormDataPart("image", imageFile.getName(), RequestBody.create(MediaType.parse("image/jpg"), imageFile));
            multipartBodyBuilder.addFormDataPart("image2", imageFile2.getName(), RequestBody.create(MediaType.parse("image/jpg"), imageFile2));
            multipartBodyBuilder.addFormDataPart("app_id", app_id);
//            multipartBodyBuilder.addFormDataPart("user_id", user_id);
            multipartBodyBuilder.addFormDataPart("deviceModel",phonemodel);
            multipartBodyBuilder.addFormDataPart("n", "8");
            multipartBodyBuilder.addFormDataPart("deviceOs", "A");
            multipartBodyBuilder.addFormDataPart("version", version);
            multipartBodyBuilder.addFormDataPart("delay", delay);
            //new Params
            multipartBodyBuilder.addFormDataPart("org_id",org_id);
//            multipartBodyBuilder.addFormDataPart("signature","");
//            multipartBodyBuilder.addFormDataPart("deviceId","");
//            multipartBodyBuilder.addFormDataPart("deviceIdType","");
//            multipartBodyBuilder.addFormDataPart("osVersion","");


            Request request = null;
            if (!url.equals("")) {
                request = new Request.Builder()
                        .header("Authorization", "Bearer " + authTok)
                        .url(url)
                        .post(multipartBodyBuilder.build())
                        .build();
            } else {
                crashProof=  new CrashProof("URL not found", null, 0);
                crashProof.setResponseCode("100");
                return crashProof;
            }

            try {
                authResponse = client.newCall(request).execute();
//                Log.e("vishwamAuthResp", String.valueOf(authResponse));

                /*if (authResponse.code() == 400) {
                    crashProof = new CrashProof("Bad Request", null,0);
                    crashProof.setResponseCode(400);
                    return crashProof;
                } else if (authResponse.code() == 401) {
                    crashProof = new CrashProof("Unathorised request", null,0);
                    crashProof.setResponseCode(401);
                    return crashProof;
                } else if (authResponse.code() == 500 || authResponse.code() == 502) {
                    crashProof = new CrashProof("Internal Server Error", null,0);
                    crashProof.setResponseCode(String.valueOf(authResponse.code()));
                    return crashProof;
                }else */
                if (authResponse.code() == 502) {
                    crashProof = new CrashProof("Bad Gateway", null,0);
                    crashProof.setResponseCode(String.valueOf(authResponse.code()));
                    return crashProof;
                }
                else {
//                    Log.e("vishwamSukshiMatchXcall", String.valueOf(headers));

                    ResponseBody responseBody = authResponse.body();
                    JSONObject jsonObject = new JSONObject(responseBody.string());
//                    Log.e("vishwamAuthRespBody", jsonObject.toString());
//                    String statusCode = jsonObject.getString("statusCode");

                    Headers headers = authResponse.headers();
//                    JSONObject headerObject = new JSONObject(headers.toString());
//                    Log.e("vishwamAuthRespHead", headers.toString());

                    HashMap<String, String> result = new HashMap<String, String>();
                    for (int i = 0, size = headers.size(); i < size; i++) {
                        result.put(headers.name(i) , headers.value(i));
                    }
//                    Log.e("vishwamRegRespHeadMap", result.toString());

                    JSONObject headerObject = new JSONObject(result);
//                    Log.e("vishwamRegRespHeadJO", headerObject.toString());

//                        String xCallid = headerObject.getString("X-Callid");
//                        Log.e("vishwamRegxCallid", xCallid);

                    crashProof = new CrashProof(null, authResponse, 0);
                    crashProof.setResponseCode(String.valueOf(authResponse.code()));
//                    crashProof.setBodyStatusCode(statusCode);
                    crashProof.setResponseBodyJson(jsonObject);
                    crashProof.setResponseHeadJson(headerObject);
                    return crashProof;
                }
            } catch (IOException e) {
                return new CrashProof(e.getMessage(), null, 1);
            } catch (JSONException e) {
//                Log.e("vishwamSukshiJSONE", e.toString());
                return new CrashProof("JsonException" + e.getMessage(), null, 2);
            }
        }

        @Override
        protected void onPostExecute(CrashProof crashProof) {
            super.onPostExecute(crashProof);

                if (crashProof.getErrorType() != null) {
                    if (crashProof.getExceptionCode() == 1) {
                        if (crashProof.getErrorType().equals("timeout") || crashProof.getErrorType().equals("SSL handshake timed out") || crashProof.getErrorType().contains("I/O error during system call, Software caused connection abort")) {
                            logApiErrorEvent(user_id, "Auth_AT408_" + crashProof.getErrorType());
                            onResponseReceived.onResponse("AT408", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Socket Time out  "), null, null);
                        } else if (crashProof.getErrorType().contains("failed to connect to")) {
                            logApiErrorEvent(user_id, "Auth_AT218_" + crashProof.getErrorType());
                            onResponseReceived.onResponse("AT218", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Connection Time out "), null, null);
                        } else {
                            logApiErrorEvent(user_id, "Auth_100_" + crashProof.getErrorType());
                            onResponseReceived.onResponse("100", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Request timed out"), null, null);
                        }
                    } else if (crashProof.getExceptionCode() == 2) {
                        logApiErrorEvent(user_id, "Auth_100_" + crashProof.getErrorType());
                        onResponseReceived.onResponse("101", new VishwamError(VishwamError.RESPONSE_JSON_E, "Json Exception"), null, null);
                    } else {
                        logApiErrorEvent(user_id, "Auth_" + crashProof.getResponseCode() + "_" + crashProof.getErrorType());
                        onResponseReceived.onResponse(crashProof.getResponseCode(), new VishwamError(VishwamError.REQUEST_TIME_OUT, crashProof.getErrorType()), null, null);
                    }
                } else {
                    if (crashProof.getResponse()!=null) {

                        ResponseBody responseBody = crashProof.getResponse().body();
                        JSONObject jsonObject = crashProof.getResponseBodyJson();
                        JSONObject headers = crashProof.getResponseHeadJson();

                        logApiSuccessEvent(user_id, "AuthApiCall_" + crashProof.getResponseCode());
                        onResponseReceived.onResponse(crashProof.getResponseCode(), null, jsonObject, headers);
                    }else {
                        logApiErrorEvent(user_id, "Auth_AT217_" + crashProof.getErrorType());
                        onResponseReceived.onResponse("AT217", new VishwamError(VishwamError.REQUEST_TIME_OUT, "No HTTPResponse"), null, null);
                    }
                }


                /*else if (crashProof.getResponse() != null) {

                    if (crashProof.getHeaderStatusCode() == 502 || crashProof.getHeaderStatusCode() == 500) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.INTERNAL_SERVER_ERROR, "Internal Server Error"), null, null);
                    } else if (crashProof.getResponse().code() == 400) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.BAD_REQUEST, "Bad request"), null, null);
                    }else if (crashProof.getResponse().code() == 401) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.UN_AUTH, "Request not authorized"), null, null);
                    } else if (crashProof.getHeaderStatusCode() == 424) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.IMAGE_DEFECT, "Image defect"), null, null);
                    } else if (crashProof.getHeaderStatusCode() == 409) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.USER_NOT_REG, "User not registered"), null, null);
                    }else if (crashProof.getHeaderStatusCode() == 406) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.FL_FAIL, "Face liveness failed"), null, null);
                    }else if (crashProof.getHeaderStatusCode() == 404) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.FR_FAIL, "FR failed"), null, null);
                    }else if (crashProof.getHeaderStatusCode() == 200) {
                        JSONObject jsonObject = crashProof.getResponseBodyJson();
                        Log.e("vishwamDocResult", jsonObject.toString());

                        onResponseReceived.onResponse(null, jsonObject);
                    }else {

                        JSONObject jsonObject = crashProof.getResponseBodyJson();
                        Log.e("vishwamDocResult", jsonObject.toString());

                        onResponseReceived.onResponse(new VishwamError(VishwamError.FACE_NOT_FOUND, jsonObject.getString("error")), null);
                    }
                } else {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.RESPONSE_NULL, "Request Failed"), null);
                }*/

        }
    }


    /**
     * This is AsyncTask to make face match network call creating new thread and passing on the response to main Ui thread
     * This uses okhttp lib with multipart builder to build with  faces images.
     * **/
    @SuppressLint("StaticFieldLeak")
    public class ApiCallForAuthentication extends AsyncTask<String, String, CrashProof> {

        @SuppressLint("LongLogTag")
        @Override
        protected CrashProof doInBackground(String... strings) {
            apiCallLog("Auth",user_id);

            String url = "", server = authDomainUrl;;
            url = "https://" + server + "/v1/single_gesture";
//            url = "https://" + server + "/v1/face_lookup";

            CertificatePinner certificatePinner = null;
            Response authResponse;
            CrashProof crashProof;

            if (!server.equals("")) {
                certificatePinner = new CertificatePinner.Builder()
                        .add(server, "sha256/" + context.getString(R.string.certificateKey))
                        .build();
            } else {
                crashProof= new CrashProof("Bad Request params", null, 0);
                crashProof.setResponseCode("100");
                return crashProof;
            }


//            authTok = JwtGenerator.getToken(app_id, user_id, secureKey);
//            Log.e("prefesAuthTOken1", authTok);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(auth_timeout, TimeUnit.SECONDS)
                    .writeTimeout(auth_timeout, TimeUnit.SECONDS)
                    .readTimeout(auth_timeout, TimeUnit.SECONDS)
                    .protocols(Collections.singletonList(HTTP_1_1))
                    .certificatePinner(certificatePinner)
                    .connectionPool(new ConnectionPool(0,1, TimeUnit.NANOSECONDS))
                    .build();

            File imageFile = new File(authPath224);
            File imageFile2 = new File(authPathFull);

            MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
            multipartBodyBuilder.setType(MultipartBody.FORM);
            multipartBodyBuilder.addFormDataPart("image", imageFile.getName(), RequestBody.create(MediaType.parse("image/jpg"), imageFile));
            multipartBodyBuilder.addFormDataPart("image2", imageFile2.getName(), RequestBody.create(MediaType.parse("image/jpg"), imageFile2));
            multipartBodyBuilder.addFormDataPart("app_id", app_id);
//            multipartBodyBuilder.addFormDataPart("user_id", user_id);
            multipartBodyBuilder.addFormDataPart("deviceModel",phonemodel);
            multipartBodyBuilder.addFormDataPart("n", "8");
            multipartBodyBuilder.addFormDataPart("deviceOs", "A");
            multipartBodyBuilder.addFormDataPart("version", version);
            multipartBodyBuilder.addFormDataPart("delay", delay);
            //new Params
            multipartBodyBuilder.addFormDataPart("org_id",org_id);
//            multipartBodyBuilder.addFormDataPart("signature","");
//            multipartBodyBuilder.addFormDataPart("deviceId","");
//            multipartBodyBuilder.addFormDataPart("deviceIdType","");
//            multipartBodyBuilder.addFormDataPart("osVersion","");


            Request request = null;
            if (!url.equals("")) {
                request = new Request.Builder()
                        .header("Authorization", "Bearer " + authTok)
                        .url(url)
                        .post(multipartBodyBuilder.build())
                        .build();
            } else {
                crashProof=  new CrashProof("URL not found", null, 0);
                crashProof.setResponseCode("100");
                return crashProof;
            }

            try {
                authResponse = client.newCall(request).execute();
//                Log.e("vishwamAuthResp", String.valueOf(authResponse));

                /*if (authResponse.code() == 400) {
                    crashProof = new CrashProof("Bad Request", null,0);
                    crashProof.setResponseCode(400);
                    return crashProof;
                } else if (authResponse.code() == 401) {
                    crashProof = new CrashProof("Unathorised request", null,0);
                    crashProof.setResponseCode(401);
                    return crashProof;
                } else if (authResponse.code() == 500 || authResponse.code() == 502) {
                    crashProof = new CrashProof("Internal Server Error", null,0);
                    crashProof.setResponseCode(String.valueOf(authResponse.code()));
                    return crashProof;
                }else */
                if (authResponse.code() == 502) {
                    crashProof = new CrashProof("Bad Gateway", null,0);
                    crashProof.setResponseCode(String.valueOf(authResponse.code()));
                    return crashProof;
                }
                else {
//                    Log.e("vishwamSukshiMatchXcall", String.valueOf(headers));

                    ResponseBody responseBody = authResponse.body();
                    JSONObject jsonObject = new JSONObject(responseBody.string());
//                    Log.e("vishwamAuthRespBody", jsonObject.toString());
//                    String statusCode = jsonObject.getString("statusCode");

                    Headers headers = authResponse.headers();
//                    JSONObject headerObject = new JSONObject(headers.toString());
//                    Log.e("vishwamAuthRespHead", headers.toString());

                    HashMap<String, String> result = new HashMap<String, String>();
                    for (int i = 0, size = headers.size(); i < size; i++) {
                        result.put(headers.name(i) , headers.value(i));
                    }
//                    Log.e("vishwamRegRespHeadMap", result.toString());

                    JSONObject headerObject = new JSONObject(result);
//                    Log.e("vishwamRegRespHeadJO", headerObject.toString());

//                        String xCallid = headerObject.getString("X-Callid");
//                        Log.e("vishwamRegxCallid", xCallid);

                    crashProof = new CrashProof(null, authResponse, 0);
                    crashProof.setResponseCode(String.valueOf(authResponse.code()));
//                    crashProof.setBodyStatusCode(statusCode);
                    crashProof.setResponseBodyJson(jsonObject);
                    crashProof.setResponseHeadJson(headerObject);
                    return crashProof;
                }
            } catch (IOException e) {
                return new CrashProof(e.getMessage(), null, 1);
            } catch (JSONException e) {
//                Log.e("vishwamSukshiJSONE", e.toString());
                return new CrashProof("JsonException" + e.getMessage(), null, 2);
            }
        }

        @Override
        protected void onPostExecute(CrashProof crashProof) {
            super.onPostExecute(crashProof);

            if (crashProof.getErrorType() != null) {
                if (crashProof.getExceptionCode() == 1) {
                    if (crashProof.getErrorType().equals("timeout") || crashProof.getErrorType().equals("SSL handshake timed out") || crashProof.getErrorType().contains("I/O error during system call, Software caused connection abort")) {
                        logApiErrorEvent(user_id, "Auth_AT408_" + crashProof.getErrorType());
                        onResponseReceived.onResponse("AT408", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Socket Time out  "), null, null);
                    } else if (crashProof.getErrorType().contains("failed to connect to")) {
                        logApiErrorEvent(user_id, "Auth_AT218_" + crashProof.getErrorType());
                        onResponseReceived.onResponse("AT218", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Connection Time out "), null, null);
                    } else {
                        logApiErrorEvent(user_id, "Auth_100_" + crashProof.getErrorType());
                        onResponseReceived.onResponse("100", new VishwamError(VishwamError.REQUEST_TIME_OUT, "Request timed out"), null, null);
                    }
                } else if (crashProof.getExceptionCode() == 2) {
                    logApiErrorEvent(user_id, "Auth_100_" + crashProof.getErrorType());
                    onResponseReceived.onResponse("101", new VishwamError(VishwamError.RESPONSE_JSON_E, "Json Exception"), null, null);
                } else {
                    logApiErrorEvent(user_id, "Auth_" + crashProof.getResponseCode() + "_" + crashProof.getErrorType());
                    onResponseReceived.onResponse(crashProof.getResponseCode(), new VishwamError(VishwamError.REQUEST_TIME_OUT, crashProof.getErrorType()), null, null);
                }
            } else {
                if (crashProof.getResponse()!=null) {

                    ResponseBody responseBody = crashProof.getResponse().body();
                    JSONObject jsonObject = crashProof.getResponseBodyJson();
                    JSONObject headers = crashProof.getResponseHeadJson();

                    logApiSuccessEvent(user_id, "AuthApiCall_" + crashProof.getResponseCode());
                    onResponseReceived.onResponse(crashProof.getResponseCode(), null, jsonObject, headers);
                }else {
                    logApiErrorEvent(user_id, "Auth_AT217_" + crashProof.getErrorType());
                    onResponseReceived.onResponse("AT217", new VishwamError(VishwamError.REQUEST_TIME_OUT, "No HTTPResponse"), null, null);
                }
            }


                /*else if (crashProof.getResponse() != null) {

                    if (crashProof.getHeaderStatusCode() == 502 || crashProof.getHeaderStatusCode() == 500) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.INTERNAL_SERVER_ERROR, "Internal Server Error"), null, null);
                    } else if (crashProof.getResponse().code() == 400) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.BAD_REQUEST, "Bad request"), null, null);
                    }else if (crashProof.getResponse().code() == 401) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.UN_AUTH, "Request not authorized"), null, null);
                    } else if (crashProof.getHeaderStatusCode() == 424) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.IMAGE_DEFECT, "Image defect"), null, null);
                    } else if (crashProof.getHeaderStatusCode() == 409) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.USER_NOT_REG, "User not registered"), null, null);
                    }else if (crashProof.getHeaderStatusCode() == 406) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.FL_FAIL, "Face liveness failed"), null, null);
                    }else if (crashProof.getHeaderStatusCode() == 404) {
                        onResponseReceived.onResponse(new VishwamError(VishwamError.FR_FAIL, "FR failed"), null, null);
                    }else if (crashProof.getHeaderStatusCode() == 200) {
                        JSONObject jsonObject = crashProof.getResponseBodyJson();
                        Log.e("vishwamDocResult", jsonObject.toString());

                        onResponseReceived.onResponse(null, jsonObject);
                    }else {

                        JSONObject jsonObject = crashProof.getResponseBodyJson();
                        Log.e("vishwamDocResult", jsonObject.toString());

                        onResponseReceived.onResponse(new VishwamError(VishwamError.FACE_NOT_FOUND, jsonObject.getString("error")), null);
                    }
                } else {
                    onResponseReceived.onResponse(new VishwamError(VishwamError.RESPONSE_NULL, "Request Failed"), null);
                }*/

        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ApiCallForCheckModelUpdate extends AsyncTask<String, String, CrashProof> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Setting up things...");
            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        @Override
        protected CrashProof doInBackground(String... strings) {

            String url = null, server = sdkDomainUrl;
            url = "https://" + server + "/v1/sdk_models_metadata";


//            authTok = JwtGenerator.getToken(app_id, user_id, secureKey);
//            //            Log.e("prefesAuthTOken1", authTok);
//
            CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add(server, "sha256/" + context.getString(R.string.certificateKey))
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .protocols(Collections.singletonList(HTTP_1_1))
                    .certificatePinner(certificatePinner)
                    .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                    .build();


            MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
            multipartBodyBuilder.setType(MultipartBody.FORM); //this may not be needed
            multipartBodyBuilder.addFormDataPart("app_id", app_id);
            multipartBodyBuilder.addFormDataPart("deviceModel", phonemodel);
            multipartBodyBuilder.addFormDataPart("deviceOs", "A");


            Request request = new Request.Builder()
//                    .header("Authorization", "Bearer " + authTok)
                    .url(url)
                    .post(multipartBodyBuilder.build())
                    .build();

            Response regResponse;
            CrashProof crashProof;

            try {
                regResponse = client.newCall(request).execute();
//                Log.e("vishwamRegResp", String.valueOf(regResponse));

                /*if (regResponse.code() == 400) {
                    crashProof = new CrashProof("Bad Request", null,0);
                    crashProof.setResponseCode(400);
                    return crashProof;
                } else if (regResponse.code() == 401) {
                    crashProof = new CrashProof("Unathorised request", null,0);
                    crashProof.setResponseCode(401);
                    return crashProof;
                } else if (regResponse.code() == 500 || regResponse.code() == 502) {
                    crashProof = new CrashProof("Internal Server Error", null,0);
                    crashProof.setResponseCode(regResponse.code());
                    return crashProof;
                } else*/
                if (regResponse.code() == 502) {
                    crashProof = new CrashProof("Bad Gateway", null, 0);
                    crashProof.setResponseCode(String.valueOf(regResponse.code()));
                    return crashProof;
                } else {
                    //Log.e("xcall", reqId);

                    ResponseBody responseBody = regResponse.body();
                    JSONObject jsonObject = new JSONObject(responseBody.string());
                        Log.e("vishwamRegRespBody", jsonObject.toString());
//                        String statusCode = jsonObject.getString("statusCode");

                    Headers respHeaders = regResponse.headers();
//                        Log.e("vishwamRegRespHead", respHeaders.toString());

                    HashMap<String, String> result = new HashMap<String, String>();
                    for (int i = 0, size = respHeaders.size(); i < size; i++) {
                        result.put(respHeaders.name(i), respHeaders.value(i));
                    }
//                        Log.e("vishwamRegRespHeadMap", result.toString());

                    JSONObject headerObject = new JSONObject(result);

                    crashProof = new CrashProof(null, regResponse, 0);
                    crashProof.setResponseCode(String.valueOf(regResponse.code()));
                    crashProof.setResponseHeadJson(headerObject);
//                        crashProof.setBodyStatusCode(statusCode);
                    crashProof.setResponseBodyJson(jsonObject);
                    return crashProof;
                }
            } catch (IOException e) {
//                Log.e("vishwamIO", e.getMessage());
                return new CrashProof(e.getMessage(), null, 1);

            } catch (JSONException e) {
//                Log.e("vishwamJE", e.getMessage());
                return new CrashProof("JsonException:" + e.getMessage(), null, 2);
            }
        }

        @Override
        protected void onPostExecute(CrashProof crashProof) {
            super.onPostExecute(crashProof);

            if (crashProof.getErrorType() == null) {
                String generatedCheksum = "", serverChecksum = "";
                try {
                    JSONObject jsonObject = crashProof.getResponseBodyJson();
//                    Log.e("checkSum", jsonObject.toString());
                    if (jsonObject.has("checksum")) {
                        serverChecksum = jsonObject.getString("checksum");
                    }

                    if (jsonObject.has("sdk_model_details")) {
                        JSONObject modelObject = jsonObject.getJSONObject("sdk_model_details");

                        generatedCheksum = generateHashWithHmac256(modelObject.toString(), checkSumKey);

//                        Log.e("checkSum",generatedCheksum+"   :     "+modelObject.toString());

                        if (modelObject.has("SdkModelPath")) {
                            String sdkModelPath1 = modelObject.getString("SdkModelPath");
                            byte[] data = Base64.decode(sdkModelPath1, Base64.DEFAULT);
                            sdkModelPath = new String(data, "UTF-8");
//                            Log.e("decryptedURL", sdkModelPath);
                        }
                        if (modelObject.has("SdkModelToken")) {
                            String sdkModelKey1 = modelObject.getString("SdkModelToken");
                            byte[] data = Base64.decode(sdkModelKey1, Base64.DEFAULT);
                            sdkModelKey = new String(data, "UTF-8");
//                            Log.e("decryptedURL", sdkModelKey);
                        }
                        if (modelObject.has("ModelFileName")) {
                            String sdkModelName1 = modelObject.getString("ModelFileName");
                            byte[] data = Base64.decode(sdkModelName1, Base64.DEFAULT);
                            sdkModelName = new String(data, "UTF-8");
//                            Log.e("decryptedURL", sdkModelName);
                        }
                        sdkModelUrl = sdkModelPath + "?" + sdkModelKey;
//                        Log.e("decryptedURL", sdkModelUrl);
                        if (modelObject.has("IsAndroid")) {
                            isAndroid = modelObject.getBoolean("IsAndroid");
                        } else {
                            isAndroid = false;
                        }
//                        Log.e("isAdnroid", isAndroid + "");
                        if (serverChecksum.equals(generatedCheksum)) {
//                            Log.e(TAG1,"Checksum passed");
                            if (isAndroid) {
                                if (!assetModelName.equals(sdkModelName)) {
//                                    Log.e(TAG1, ": 1");
                                    String savedFileName = prefs.getString("savedModelName", "");
                                    File storedModel = Utils2.getModelFile(context, savedFileName);
                                    if (storedModel.exists()) {
                                        if (!savedFileName.isEmpty() && !savedFileName.equals(sdkModelName)) {
                                            //download new model
//                                            Log.e(TAG1, ": 3");

                                            new ApiCallFordownloadModelUpdate().execute();


                                        } else if (savedFileName.isEmpty()) {
                                            // download new model
//                                            Log.e(TAG1, ": 4");
                                            new ApiCallFordownloadModelUpdate().execute();
                                        } else {
//                                            if (progressDialog!=null && progressDialog.isShowing()) {
//                                                progressDialog.dismiss();
//                                            }
////                                            imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                                            imageAnalysis.initImageAnalysis();
//                                            imageAnalysis.setupImageAnalysis();
//                                            sdKinitializationCallback.OnInitialized("initialization completed...", imageAnalysis);
                                            initialiseModel("initialization completed");
                                        }
                                    } else {
                                        // download new model
//                                        Log.e(TAG1, ": 5");
                                        new ApiCallFordownloadModelUpdate().execute();
                                    }
                                } else {
//                                    if (progressDialog!=null && progressDialog.isShowing()) {
//                                        progressDialog.dismiss();
//                                    }
//                                    imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                                    imageAnalysis.initImageAnalysis();
//                                    imageAnalysis.setupImageAnalysis();
//                                    sdKinitializationCallback.OnInitialized("initialization completed", imageAnalysis);
//                                    Log.e(TAG1, ": 2  assetfileName is same");
                                    initialiseModel("initialization completed");
                                }
                            } else {
                                String formattedDate = getDateString();
                                editor.putString("date_string", formattedDate).apply();
//                                imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                                imageAnalysis.initImageAnalysis();
//                                imageAnalysis.setupImageAnalysis();
//                                sdKinitializationCallback.OnInitialized("initialization completed", imageAnalysis);
                                initialiseModel("initialization completed");
//                                if (progressDialog!=null && progressDialog.isShowing()) {
//                                    progressDialog.dismiss();
//                                }
                            }
                        } else {
//                            Log.e(TAG1,"Checksum failed");
//                            imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                            imageAnalysis.initImageAnalysis();
//                            imageAnalysis.setupImageAnalysis();
//                            sdKinitializationCallback.OnInitialized("initialization completed", imageAnalysis);
                            initialiseModel("initialization completed");
//                            if (progressDialog!=null && progressDialog.isShowing()) {
//                                progressDialog.dismiss();
//                            }
                        }

                    } else {
                        String formattedDate = getDateString();
                        editor.putString("date_string", formattedDate).apply();
//                        imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                        imageAnalysis.initImageAnalysis();
//                        imageAnalysis.setupImageAnalysis();
//
//                        sdKinitializationCallback.OnInitialized("initialization completed", imageAnalysis);
                        initialiseModel("initialization completed");
//                        if (progressDialog!=null && progressDialog.isShowing()) {
//                            progressDialog.dismiss();
//                        }
                    }

                } catch (Exception e) {
//                    Log.e("JSONException", e.getMessage());
//                    imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                    imageAnalysis.initImageAnalysis();
//                    imageAnalysis.setupImageAnalysis();
//                    sdKinitializationCallback.OnInitialized(e.getMessage(), imageAnalysis);
                    e.printStackTrace();
                    initialiseModel(e.getMessage());
//                    if (progressDialog!=null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
                }
            } else {
//                imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                imageAnalysis.initImageAnalysis();
//                imageAnalysis.setupImageAnalysis();
//                sdKinitializationCallback.OnInitialized("initialization completed", imageAnalysis);
                initialiseModel("initialization completed");
//                if (progressDialog!=null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
            }

        }
    }

    public void initialiseModel(String message){
        imageAnalysis = new ImageAnalysis(context.getAssets(), context);
        imageAnalysis.initImageAnalysis();
        ImageAnalysis.setupImageAnalysis();
        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        sdKinitializationCallback.OnInitialized(message, imageAnalysis);
    }

    @SuppressLint("StaticFieldLeak")
    public class ApiCallFordownloadModelUpdate extends AsyncTask<Void, Long, Boolean> {
        @Override
        protected void onPreExecute() {
            if (progressDialog!=null && !progressDialog.isShowing()) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Setting up things...");
                progressDialog.setCancelable(false);
                //  dialog.setCanceledOnTouchOutside(false);
//            progressDialog.setIndeterminate(false);
                //  progressDialog.setMax(100);
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                // progressDialog.setProgress(0);
//            progressDialog.setMax(100);
                progressDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            final long MEGABYTE = 1024L * 1024L;
            String url = sdkModelUrl;

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.MINUTES)
                    .writeTimeout(30, TimeUnit.MINUTES)
                    .readTimeout(30, TimeUnit.MINUTES)
                    .protocols(Collections.singletonList(HTTP_1_1))
                    .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = null;

            try {
                response = client.newCall(request).execute();
//                Log.e("VishwamResp", response.code() + "");
                if (response.code() == 200 || response.code() == 201) {
                    InputStream inputStream = null;
                    try {
                        inputStream = response.body().byteStream();

                        byte[] buff = new byte[1024 * 4];
                        long downloaded = 0;
                        long target = response.body().contentLength();
                        modelfile = Utils2.getModelFile(context, sdkModelName);

                        OutputStream output = new FileOutputStream(modelfile);
                        Log.e("Pathofthefile", modelfile.getAbsolutePath());
                        publishProgress(0L, target);

                        while (true) {
                            int readed = inputStream.read(buff);

                            if (readed == -1) {
                                break;
                            }
                            output.write(buff, 0, readed);
                            //write buff
                            downloaded += readed;
                            publishProgress(downloaded / MEGABYTE, target / MEGABYTE);
                            if (isCancelled()) {
                                return false;
                            }
                        }

                        output.flush();
                        output.close();

                        return downloaded == target;
                    } catch (IOException ignore) {
                        return false;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
        /*    if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            progressDialog.setMax(values[1].intValue());

            progressDialog.setProgress(values[0].intValue());*/
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);


            if (aBoolean) {
//                String formattedDate = getDateString();
//                editor.putString("date_string", formattedDate).apply();

                //deleting old saved model from storage
                String savedFileName = prefs.getString("savedModelName", "");
                if (!savedFileName.isEmpty() && !savedFileName.equals(sdkModelName)) {
                    File storedModel = Utils2.getModelFile(context, savedFileName);
                    if (storedModel!=null && storedModel.exists()) {
                        storedModel.delete();
                    }
                }

                editor.putString("savedModelName", sdkModelName).apply();
//                Toast.makeText(context, "Download success", Toast.LENGTH_SHORT).show();
//                imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                imageAnalysis.initImageAnalysis();
//                imageAnalysis.setupImageAnalysis();
//                sdKinitializationCallback.OnInitialized("Model updated", imageAnalysis);
                initialiseModel("Model updated");


            } else {
//                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                if (modelfile != null && modelfile.exists()) {
                    modelfile.delete();
//                    Log.e("fileDelete", "FileeDeltedddd");
                }
//                imageAnalysis = new ImageAnalysis(context.getAssets(), context);
//                imageAnalysis.initImageAnalysis();
//                imageAnalysis.setupImageAnalysis();
//                sdKinitializationCallback.OnInitialized("Model update failed", imageAnalysis);
                initialiseModel("Model update failed");
            }

//            if (progressDialog!=null && progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }
        }
    }

    private void initLogs(int env){
        String logSecret = "";
        if (env == 0){
            logSecret = "e582bc74-3900-4c08-ab0f-6995f5663539";
        }else {
            logSecret = "d8fd3d18-20ac-4d20-ab81-36c69ec70fbb";
        }
        AppCenter.start(activity.getApplication(),logSecret, Analytics.class, Crashes.class);
        Analytics.setEnabled(true);
    }

    private void apiCallLog(String apiName, String userID) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        Map<String, String> properties = new HashMap<>();
        properties.put("apiName", apiName);
        properties.put("userID",userID+timeStamp);
        Analytics.trackEvent("on"+apiName+"ApiCall", properties);
    }

    private void logNoInternetEvent(String ActivityName, String userID) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        Map<String, String> properties = new HashMap<>();
        properties.put("Activity", ActivityName);
        properties.put("userID",userID+timeStamp);
        Analytics.trackEvent(ActivityName+" : No Internet", properties);

    }

    private void logApiErrorEvent(String userID, String error) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        Map<String, String> properties = new HashMap<>();
        properties.put("userID", userID+timeStamp);
        Analytics.trackEvent(error, properties);
    }

    private void logApiSuccessEvent(String userID, String apiType) {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            Map<String, String> properties = new HashMap<>();
            properties.put("ReferenceId",userID+timeStamp);
            Analytics.trackEvent(apiType, properties);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected();
    }
    /**
     * Returns string with current date.
     * **/


    private String getDateString() {

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

        return dateFormat.format(date);
    }
    //methods for HmacSHA256
    private String generateHashWithHmac256(String message, String key) {
        String messageDigest = "";
        try {
            final String hashingAlgorithm = "HmacSHA256"; //or "HmacSHA1", "HmacSHA512"

            byte[] bytes = hmac(hashingAlgorithm, key.getBytes(), message.getBytes());

            messageDigest = bytesToHex(bytes);

            Log.i("KeyGeneration", "message digest: " + messageDigest);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageDigest;
    }

    public static byte[] hmac(String algorithm, byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(message);
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0, v; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
