package com.jukshio.jwccgateapplib.Activities;

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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.images.Size;
import com.jukshio.jwccgateapplib.FRCaptureView.CameraView;
import com.jukshio.jwccgateapplib.FRCaptureView.FaceDect;
import com.jukshio.jwccgateapplib.FRCaptureView.ParamsView;
import com.jukshio.jwccgateapplib.ImageAnalysis.ImageAnalysis;
import com.jukshio.jwccgateapplib.Networking.APICompletionCallback;
import com.jukshio.jwccgateapplib.Networking.VishwamError;
import com.jukshio.jwccgateapplib.R;
import com.jukshio.jwccgateapplib.Tracker.GraphicOverlay;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.jukshio.jwccgateapplib.Activities.CameraSource.*;
import static com.jukshio.jwccgateapplib.FRCaptureView.FaceDect.authFaceDetector;
import static com.jukshio.jwccgateapplib.FRCaptureView.ParamsView.imageAnalysis;
import static com.jukshio.jwccgateapplib.FRCaptureView.ParamsView.takePicture;
import static com.jukshio.jwccgateapplib.ImageAnalysis.ImageAnalysis.*;


//import com.sukshi.vishwamfrlib.Tracker.FaceDect;
//import com.sukshi.vishwamfrlib.Tracker.FaceDect2;
//import static com.sukshi.vishwamfrlib.FRCaptureView.CameraView.takePicture;
//import static com.sukshi.vishwamfrlib.FRCaptureView.FaceDect.previewFaceDetector;
//import static com.sukshi.vishwamfrlib.Tracker.FaceDect2.previewFaceDetector;

public class AuthCameraActivity extends AppCompatActivity implements FaceDect.OnMultipleFacesDetectedListener, FaceDect.OnCaptureListener, FaceDect.OnNextFrameListener, FaceDect.OnCameraCloseListener /*,FaceDect2.OnMultipleFacesDetectedListener*/ /*, FaceDect2.OnCaptureListener*/ {

    private static final String TAG = "Custom Camera";
    private Context context;
    public CameraSource mCameraSource;

    // CAMERA VERSION ONE DECLARATIONS
    FaceDect faceDect;

    // COMMON TO BOTH CAMERAS
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private boolean wasActivityResumed = false;

    public static FaceDect authFrameFaceDect;

    String username = "vishwam";

    ImageView previewImages;

    public ProgressDialog dialog;

    public Bitmap OriginalBitmap, rotatedbitmap;
    //    CameraView cameraView;
    ParamsView paramsView;
    boolean isAuth = false;
    boolean continuousCapture = false;
    TextView responseTv;
    public static Bitmap rotatedBitmap2;

//    FaceDetector faceFramedetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

//        faceRec = new FaceRec(Constants.getDLibDirectoryPath(), getApplicationContext());

//        MainActivity.inReg = false;
        takePicture = false;

//        if (!readyForAnalysis) {
//            imageAnalysis = new ImageAnalysis(getAssets(), AuthCameraActivity.this);
//            imageAnalysis.initImageAnalysis();
//        }
//        imageAnalysis.setupImageAnalysis();

//        authFrameFaceDect = new FaceDect(CameraActivity.this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isAuth = bundle.getBoolean("isAuth");
            continuousCapture = bundle.getBoolean("countinuousCapture");
        }
        context = getApplicationContext();

        dialog = new ProgressDialog(AuthCameraActivity.this);
        ImageView camera = findViewById(R.id.camera);
        camera.setVisibility(View.INVISIBLE);

        previewImages = findViewById(R.id.preview);
        RelativeLayout relativeLayout = findViewById(R.id.camRLayout);

        CameraView cameraView = new CameraView(this);
        relativeLayout.addView(cameraView);

        paramsView = new ParamsView(this, relativeLayout, true, "Scan Your Face", false, true, false, this);
        relativeLayout.addView(paramsView);

//        faceFramedetector = new FaceDetector.Builder(CameraActivity.this)
//                .setTrackingEnabled(false)
//                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
//                .build();


        responseTv = new TextView(context);
        RelativeLayout.LayoutParams instructionTextview = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
//        instructionTextview.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        instructionTextview.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        instructionTextview.addRule(RelativeLayout.ABOVE,btnTag.getId());
        instructionTextview.setMargins(0, 0, 0, 50);
//        instructionTextview.setMargins(0, (int) ((width*2/3) + (width*0.45f)),0,0);
        responseTv.setTextSize(18);
        responseTv.setTextColor(getResources().getColor(R.color.yellow_color));
        responseTv.setGravity(Gravity.CENTER);
        responseTv.setTypeface(null, Typeface.BOLD);
        responseTv.setLayoutParams(instructionTextview);
//        straightFaceWarning.setBackgroundColor(getResources().getColor(R.color.white));
        relativeLayout.addView(responseTv);


        mPreview = findViewById(R.id.previewAuth);
        mGraphicOverlay = findViewById(R.id.faceOverlayAuth);
        createCameraSourceFront();
        startCameraSource();

        /*camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });*/
    }

    /*final CameraSource.ShutterCallback cameraSourceShutterCallback = new CameraSource.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };
    final CameraSource.PictureCallback cameraSourcePictureCallback = new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data) {
            Log.d(TAG, "Taken picture is here!");

            dialog.setMessage("Recognizing...");
            dialog.setCancelable(false);
            dialog.show();

            OriginalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            //Get orientation
            InputStream io = new ByteArrayInputStream(data);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(io);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            rotatedbitmap = rotateBitmap(OriginalBitmap, orientation);
            Log.e("RxRyOriginalSize", rotatedbitmap.getWidth() + ", " + rotatedbitmap.getHeight());

            saveFile(rotatedbitmap);

            stopCameraSource();
            previewImages.setVisibility(View.VISIBLE);
            previewImages.setImageBitmap(rotatedbitmap);
            pixelReducedBitmap = faceRec.getProcessedBitmap(rotatedbitmap);
        }
    };*/

    /*@Override
    public void onFrontalFaceDetected(byte[] data) {

        Log.e("shankOnFrontal21", "here");
    }*/

    @Override
    public void onMultipleFacesDetected(int n) {

        paramsView.isFrontal(n);
    }

    @Override
    public void onCapture(byte[] data, int angle) {

        Toast.makeText(context, "Image Captured", Toast.LENGTH_SHORT).show();
//        ImageAnalysis.canAddData = false;
        stopCameraSource();
//        dialog.setMessage("Recognizing...");
//        dialog.setCancelable(false);
//        dialog.show();



        Bitmap OriginalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        Bitmap rotatedbitmap = Bitmap.createBitmap(OriginalBitmap, 0, 0, OriginalBitmap.getWidth(), OriginalBitmap.getHeight(), matrix, true);
        rotatedBitmap2=rotatedbitmap;

        Intent intent= new Intent(AuthCameraActivity.this,AfterCapture.class);
        startActivity(intent);

   /*     Bitmap kothaBitmap = Bitmap.createScaledBitmap(rotatedbitmap, 224, 224, false);
        Bitmap bitmap160 = Bitmap.createScaledBitmap(rotatedbitmap, 160, 160, false);

        long start_time = System.currentTimeMillis();
//        String light_response = paramsView.checkLight(bitmap160);
//        Log.e("light_response", light_response);
//        Log.e("light_response_time", System.currentTimeMillis() - start_time + "");

        saveFile(kothaBitmap);

        Bitmap croppedBitmap = faceDect.getCroppedFace(rotatedbitmap);
        if (croppedBitmap != null) {
            saveCropedFile(croppedBitmap, "cropped");
        } else {
            Bitmap compressed = faceDect.getSquareBitmap(rotatedbitmap);
            saveCropedFile(compressed, "square");
        }*/

//        saveFile(rotatedbitmap);
    }



    /*@Override
    public void onFrontalFaceDetected2(byte[] data) {

        Log.e("shankOnFrontal22", "here");

        dialog.setMessage("Recognizing...");
        dialog.setCancelable(false);
        dialog.show();

        OriginalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap rotated = Bitmap.createBitmap(OriginalBitmap,0,0,OriginalBitmap.getWidth(),OriginalBitmap.getHeight(),matrix,true);

        //Get orientation
        InputStream io = new ByteArrayInputStream(data);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(io);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        rotatedbitmap = rotateBitmap(rotated, orientation);
        Log.e("RxRyOriginalSize", rotatedbitmap.getWidth() + ", " + rotatedbitmap.getHeight());

        saveFile(rotatedbitmap);

        previewImages.setVisibility(View.VISIBLE);
        previewImages.setImageBitmap(rotatedbitmap);
        pixelReducedBitmap = faceRec.getProcessedBitmap(rotatedbitmap);
    }*/

    String pathAuth224, pathAuthFull;
    Bitmap pixelReducedBitmap;
    String message;

    public void saveCropedFile(Bitmap bitmap, String mess) {

        message = mess;
        File file = getOutputMediaFile();
        pathAuthFull = file.getPath();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();


                    Log.e("vishwam", getImageIndex());
                    Log.e("version", String.valueOf(faceDect.getVersion()));

                    String delay = String.valueOf(faceDect.getDelay()) + "," + getImageIndex();
                    Log.e("delay", delay);
                    String version = String.valueOf(faceDect.getVersion());

                    JSONObject apiAuthParams = new JSONObject();
//                    apiAuthParams.put("app_id", app_id);
//                    apiAuthParams.put("org_id", org_id);
//                    apiAuthParams.put("version", faceDect.getVersion());
//                    apiAuthParams.put("user_id", user_id);
//                    apiAuthParams.put("delay", delay);
//                    apiAuthParams.put("device_model", phonemodel+","+ getVersion());
//                    apiAuthParams.put("secure_key", secure_key);
//                    apiAuthParams.put("timeout_secs", timeoutSecs);

                    /*
                    app_id,
                    image,
                    org_id,
                    signature,
                    deviceOs,
                    deviceId,
                    deviceIdType
                    osVersion
                    deviceModel*/

                 /*   if (!isAuth) {
                        vishwamNetworkHelper.makeFaceLookupCall(env, domainUrl, pathAuth224, pathAuthFull, apiAuthParams, authApiCompletionCallback);
                        Log.e("isAuth","lookupcall");
                    }else {
                        vishwamNetworkHelper.makeVerifyFRCall(env, domainUrl, pathAuth224, pathAuthFull, apiAuthParams, authApiCompletionCallback1);
                        Log.e("isAuth","authCall");
                    }*/
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveFile(Bitmap bitmap) {
        message = "full";

        File file = getOutputMediaFile();
        pathAuth224 = file.getPath();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();


//                    Intent i = new Intent(AuthCameraActivity.this, MainActivity.class);
//                    startActivity(i);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void start(Context context, JSONObject faceParams) {

        Intent intent = new Intent(context, AuthCameraActivity.class);
//        LiveFaceAuthHolder.getInstance().setLiveFaceAuthResultListener(onLiveFaceCapturedResultListener);
        try {
//            intent.putExtra("env", faceParams.getInt("env"));
//            intent.putExtra("capture_type", faceParams.getInt("capture_type"));
//            intent.putExtra("referenceId", faceParams.getJSONObject("headers").getString("referenceId"));

            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    APICompletionCallback authApiCompletionCallback = new APICompletionCallback() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onResponse(String responseCode, VishwamError error, JSONObject jsonObjectApiResponse, JSONObject headers) {
            dialog.dismiss();

          /*  Log.e("authRespCode", responseCode);
            if (!continuousCapture) {
                Intent i = new Intent(AuthCameraActivity.this, MainActivity.class);
                if (error == null) {
                    i.putExtra("response", responseCode + ": " + jsonObjectApiResponse.toString());
                    i.putExtra("headers", headers.toString());
                } else {
                    i.putExtra("response", responseCode + ": " + error.getErrorMsg());
                    i.putExtra("headers", "null");
                }
                startActivity(i);
                finish();
            }else {
                Log.e("Contious", "here");
                if (error==null){
                    responseTv.setText("response: "+responseCode+": "+jsonObjectApiResponse.toString()+"\n"+"headers: "+ headers.toString());
                }else {
                    responseTv.setText("response: "+responseCode+": "+error.getErrorMsg()+"\n"+"headers: "+"null");
                }

//                authFaceDetector.release();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        responseTv.setText("");
                        imageAnalysis.setupImageAnalysis();
                        okForImageAnalysis = true;
                        okForAnalysis=true;
                        canAddData=true;
                        faceSize=0;
//                        createCameraSourceFront();
                        if (!isCameraStarted) {
                            startCameraSource();
                        }
//                        paramsView.visibleCloseBtn(true);
                    }
                }, 3000);
            }*/
        }
    };

    APICompletionCallback authApiCompletionCallback1 = new APICompletionCallback() {
        @Override
        public void onResponse(String responseCode, VishwamError error, JSONObject jsonObjectApiResponse, JSONObject headers) {
            dialog.dismiss();

            Log.e("authRespCode", responseCode);
         /*   Intent i = new Intent(AuthCameraActivity.this, MainActivity.class);
            if (error == null) {
                i.putExtra("response", responseCode + ": " + jsonObjectApiResponse.toString());
                i.putExtra("headers", headers.toString());
            } else {
                i.putExtra("response", responseCode + ": " + error.getErrorMsg());
                i.putExtra("headers", "null");
            }
            startActivity(i);
            finish();*/
        }
    };

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createCameraSourceFront() {
        faceDect = new FaceDect(this, mGraphicOverlay);

        mCameraSource = new Builder(context, authFaceDetector)
                .setFacing(CAMERA_FACING_FRONT)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .setRequestedFps(30.0f)
                .build();

        startCameraSource();
    }

    private void startCameraSource() {

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                // Log.e(TAG, "Unable to start caera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
        canAddData = true;
    }

    boolean isCameraStarted = false;

    private void stopCameraSource() {
        mPreview.stop();
        isCameraStarted = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasActivityResumed) {
            createCameraSourceFront();
        }
        startCameraSource();
    }

    /*public void takePicture() {

        if (mCameraSource != null)
            mCameraSource.takePicture(cameraSourceShutterCallback, cameraSourcePictureCallback);
    }*/

    @Override
    protected void onPause() {
        super.onPause();

        wasActivityResumed = true;
        stopCameraSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCameraSource();

//        if (authFaceDetector != null) {
//            authFaceDetector.release();
//        }
    }

    public File getOutputMediaFile() {

        final String TAG = "CameraPreview";

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FRLib");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        long time = System.currentTimeMillis();
        File file = new File(mediaStorageDir.getPath() + File.separator + username + "_" + message + "_" + time + ".jpg");

        return file;
    }

    @Override
    public void onSetNextFrame(byte[] data1, Camera.Parameters parameters, Size mPreviewSize, int angle) {
        paramsView.startImageAnalysis(data1, parameters, mPreviewSize, angle);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        okForImageAnalysis = false;
        setupImageAnalysis();
//        Intent i = new Intent(AuthCameraActivity.this, MainActivity.class);
//        i.putExtra("response", "User clicked back button");
//        i.putExtra("headers", "");
//        startActivity(i);
//        finish();

    }

    @Override
    public void onCameraClose() {
//      okForImageAnalysis = false;
        setupImageAnalysis();
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
//        Intent i = new Intent(AuthCameraActivity.this, MainActivity.class);
//        i.putExtra("response", "User clicked close button");
//        i.putExtra("headers", "");
//        startActivity(i);
//        finish();
    }

    //    @Override
//    public void onMultipleFacesDetected2(int n) {
//
//    }


   /* @Override
    public void onMultipleFacesDetected2(int n) {

    }

    @Override
    public void onCapture2(byte[] data, int angle) {
        Log.e("shankOnFrontal22", "here");

    }*/
}
