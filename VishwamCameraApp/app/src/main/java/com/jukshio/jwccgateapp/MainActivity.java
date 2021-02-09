package com.jukshio.jwccgateapp;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jukshio.jwccgateapplib.Activities.AuthCameraActivity;
import com.jukshio.jwccgateapplib.ImageAnalysis.ImageAnalysis;
import com.jukshio.jwccgateapplib.Networking.VishwamNetworkHelper;
import com.jukshio.jwccgateapplib.OncaptureImageCallback;
import com.jukshio.jwccgateapplib.SDKinitializationCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    Button button;
    public static VishwamNetworkHelper vishwamNetworkHelper;
    public static String app_id, domainUrl, user_id, phonemodel, secure_key, org_id = "", checksumKey = "";
    ;
    public static int env, timeoutSecs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);

        env = 0;
        timeoutSecs = 10;
        user_id = "santhosh_dil";
        app_id = getResources().getString(R.string.app_id);
        org_id = getResources().getString(R.string.org_id);
        checksumKey = getResources().getString(R.string.key);
//        app_id ="vishwam";
        vishwamNetworkHelper = new VishwamNetworkHelper(env, this, this);

        phonemodel = Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE;
        secure_key = "asdsadnmnckggrhtjrnfjhkzysdyleur";
        Log.e("OSVERSION", Build.VERSION.BASE_OS);

//        domainUrl = "jio-att-staging.vishwamcorp.com";
        domainUrl = "jio-att-dev.vishwamcorp.com";
        checkPermissions();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthCameraActivity.start(MainActivity.this, null);
            }
        });
    }

    SDKinitializationCallback sdKinitializationCallback = new SDKinitializationCallback() {
        @Override
        public void OnInitialized(String message, ImageAnalysis imageAnalysis1) {
            Log.e("sdkResponse", message);
//            imageAnalysis = imageAnalysis1;
//            AuthCameraActivity.start(MainActivity.this,null);
        }
    };

    public void checkPermissions() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 501);
            return;
        } else {

//            copyNow();
            //faceRec = new FaceRec(Constants.getDLibDirectoryPath(), getApplicationContext());
//        resourceFiles= new ResourceFiles(this);
//        resourceFiles.getFiles();
            try {
                JSONObject apiParam = new JSONObject();
                apiParam.put("app_id", app_id);
                apiParam.put("device_model", phonemodel);
                apiParam.put("checksumKey", checksumKey);
                apiParam.put("timeout_secs", timeoutSecs);
                vishwamNetworkHelper.initSDK(env, domainUrl, apiParam, sdKinitializationCallback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 501) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    OncaptureImageCallback oncaptureImageCallbacknew = new OncaptureImageCallback() {
        @Override
        public void onImageCaptured(Bitmap capturedImage, String Response) {

        }
    };
}