package com.jukshio.jwccgateapplib.FRCaptureView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.images.Size;
import com.jukshio.jwccgateapplib.ImageAnalysis.Classifier;
import com.jukshio.jwccgateapplib.ImageAnalysis.ImageAnalysis;
import com.jukshio.jwccgateapplib.ImageAnalysis.ImageObject;
import com.jukshio.jwccgateapplib.R;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;


import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.jukshio.jwccgateapplib.FRCaptureView.FaceDect.detectorAvailable;
import static com.jukshio.jwccgateapplib.FRCaptureView.FaceDect.straightFaceFound;
import static com.jukshio.jwccgateapplib.FRCaptureView.FaceGraphic.faceIsInTheBox;
import static com.jukshio.jwccgateapplib.FRCaptureView.FaceGraphic.faceRatioOk;
import static com.jukshio.jwccgateapplib.FRCaptureView.FaceGraphic.mHintOutlinePaint;
import static com.jukshio.jwccgateapplib.FRCaptureView.FaceGraphic.mHintOutlinePaint2;
import static com.jukshio.jwccgateapplib.ImageAnalysis.ImageAnalysis.*;



public class ParamsView extends View implements View.OnClickListener {
    private RelativeLayout rlCustomLayout;
    Context context;
    Button btnTag,exitBtn;
    int height;
    public static int width;
    public static boolean correctFace= true, islightEnabled = false;

    public static boolean takePicture;

    TextView straightFaceWarning,captureIntimationtxt;
    String titleString="";

    byte[] bytes;
    Camera camera;
    Camera.Parameters parameters;
    Size mPreviewSize;
    int angle;
    public boolean  firstFrame, inRegCam;
    public ImageAnalysis imageAnalysis1;
    byte[] finalByte;
    boolean autoCap=false;
    boolean enableClose=false;
   FaceDect.OnCameraCloseListener onCameraCloseListener;
    public static ImageAnalysis imageAnalysis;

    Bitmap rotated;

    public ParamsView(Context context, RelativeLayout relativeLayout, boolean setFaceDect, String title, boolean inReg, boolean autoCap,boolean visibleCloseBtn,FaceDect.OnCameraCloseListener onCameraCloseListener) {
        super(context);
        this.rlCustomLayout = relativeLayout;
        this.context = context;
        correctFace = setFaceDect;
        this.autoCap=autoCap;
        this.enableClose=visibleCloseBtn;
        this.onCameraCloseListener=onCameraCloseListener;
        if (!title.equals("")){
            this.titleString = title;
        }else {
            this.titleString = "Face Capture";
        }
        this.inRegCam = inReg;
        if (imageAnalysis==null){
            imageAnalysis = new ImageAnalysis(context.getAssets(), context);
            imageAnalysis.initImageAnalysis();
            imageAnalysis.setupImageAnalysis();
        }else {
            imageAnalysis.setupImageAnalysis();
        }
        initial();
//        initAnalytics();
    }

    @SuppressLint({"NewApi", "SetTextI18n"})
    private void initial() {

        firstFrame = true;
        islightEnabled = false;

        /*if (inRegCam ) {
            if (!readyForAnalysis) {
                imageAnalysis1 = new ImageAnalysis(context.getAssets(), context);
                imageAnalysis1.initLightDetection();
                imageAnalysis1.setupImageAnalysis();
            }

        }*/


        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        height = metrics.heightPixels;
        width = metrics.widthPixels;

        RelativeLayout.LayoutParams titleViewParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        titleViewParams.addRule(RelativeLayout.ALIGN_LEFT);
        titleViewParams.setMargins(150, 50, 10, 0);

        TextView titleView = new TextView(context);
        titleView.setId(R.id.instructionTitle);
        titleView.setLayoutParams(titleViewParams);
        titleView.setTextColor(Color.BLACK);
        titleView.setTextSize(16);
        titleView.setText(titleString);
//        titleView.setTypeface(null, Typeface.BOLD);
//        titleView.setBackgroundColor(getResources().getColor(R.color.white));
        rlCustomLayout.addView(titleView);

        RelativeLayout.LayoutParams titleViewParams1 = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleViewParams1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        titleViewParams1.addRule(RelativeLayout.BELOW,titleView.getId());
        titleViewParams1.setMargins(150, 130, 10, 0);

        TextView titleView1 = new TextView(context);
        titleView1.setId(R.id.instruction1);
        titleView1.setLayoutParams(titleViewParams1);
        titleView1.setTextColor(Color.BLACK);
        titleView1.setTextSize(14);
        titleView1.setText("Place your face in the placeholder and make sure you’re well it.");

        rlCustomLayout.addView(titleView1);

        btnTag = new Button(context);
        btnTag.setId(R.id.capture_btn);
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(200, 200);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        btnParams.setMargins(0,(width*2/3) + (width*2/3),0,0);
//        btnParams.setMargins(0,0,0,80);
        btnParams.setMargins(0,0,0,(int) ((height- (width*4/3))*0.25f));
        btnTag.setLayoutParams(btnParams);
        btnTag.setBackground(getResources().getDrawable(R.drawable.disable_camera));
        btnTag.setOnClickListener((OnClickListener) this);
        rlCustomLayout.addView(btnTag);

        if (!inRegCam && autoCap){
            btnTag.setVisibility(INVISIBLE);
        }else {
            btnTag.setVisibility(VISIBLE);
        }

        captureIntimationtxt = new TextView(context);
        RelativeLayout.LayoutParams captureintimationTextview = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
//        captureintimationTextview.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        captureintimationTextview.addRule(RelativeLayout.CENTER_HORIZONTAL);
        captureintimationTextview.addRule(RelativeLayout.ABOVE,btnTag.getId());
        //captureintimationTextview.setMargins(0, (int) ((width*2/3) + (width*0.45f)),0,0);
        captureintimationTextview.setMargins(0, 0,0,50);
        captureIntimationtxt.setTextSize(18);
        captureIntimationtxt.setTypeface(null, Typeface.BOLD);
        captureIntimationtxt.setTextColor(getResources().getColor(R.color.white));
        captureIntimationtxt.setGravity(Gravity.CENTER);
        captureIntimationtxt.setLayoutParams(captureintimationTextview);
//        straightFaceWarning.setBackgroundColor(getResources().getColor(R.color.white));
        rlCustomLayout.addView(captureIntimationtxt);

//        Log.e("vishwam", String.valueOf(detectorAvailable));

        if (detectorAvailable) {
            btnTag.setEnabled(false);
        } else {
//            Log.e("vishwam","no detector");
            btnTag.setEnabled(true);
            btnTag.setBackground(getResources().getDrawable(R.drawable.enable_camera));
        }

        straightFaceWarning = new TextView(context);
        RelativeLayout.LayoutParams instructionTextview = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
//        instructionTextview.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        instructionTextview.addRule(RelativeLayout.CENTER_HORIZONTAL);
        instructionTextview.addRule(RelativeLayout.ABOVE,btnTag.getId());
        instructionTextview.setMargins(0, 0,0,50);
//        instructionTextview.setMargins(0, (int) ((width*2/3) + (width*0.45f)),0,0);
        straightFaceWarning.setTextSize(18);
        straightFaceWarning.setTextColor(getResources().getColor(R.color.yellow_color));
        straightFaceWarning.setGravity(Gravity.CENTER);
        straightFaceWarning.setTypeface(null, Typeface.BOLD);
        straightFaceWarning.setLayoutParams(instructionTextview);
//        straightFaceWarning.setBackgroundColor(getResources().getColor(R.color.white));
        rlCustomLayout.addView(straightFaceWarning);

         exitBtn = new Button(context);
        exitBtn.setId(R.id.exitbtn);
        RelativeLayout.LayoutParams exitbtnParams = new RelativeLayout.LayoutParams(70, 70);
        exitbtnParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        exitbtnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        btnParams.setMargins(0,(width*2/3) + (width*2/3),0,0);
//        btnParams.setMargins(0,0,0,80);
        exitbtnParams.setMargins(0,40,40,0);
        exitBtn.setLayoutParams(exitbtnParams);
        exitBtn.setBackground(getResources().getDrawable(R.drawable.closebtn));
        rlCustomLayout.addView(exitBtn);
        /*transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        semiTransparentPaint.setColor(getResources().getColor(android.R.color.white));*/
        if (enableClose){
            exitBtn.setVisibility(VISIBLE);
        }else {
            exitBtn.setVisibility(INVISIBLE);
        }
        exitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                okForAnalysis = false;
                canAddData = false;
                okForImageAnalysis = false;
                onCameraCloseListener.onCameraClose();
            }
        });

    }

    public void startImageAnalysis(byte[] bytes, Camera.Parameters parameters, Size mPreviewSize, int angle) {
//        Log.e("setnextframe", "came");
        this.bytes = bytes;
        this.parameters = parameters;
        this.mPreviewSize = mPreviewSize;
        this.angle = angle;
//        new MyAsyncTask().execute();
        int previewW = mPreviewSize.getWidth();
        int previewH = mPreviewSize.getHeight();
//        Log.e("stepCheck_analysis_0","in ParamsView");
        ImageObject imageObject = new ImageObject(bytes, previewW, previewH, parameters, angle,null);

        if (!inRegCam&&  okForAnalysis){
            Log.e("vishwam_IA_start2","here");
            imageAnalysis.bitmapQueue.add(imageObject);
//                Log.e("santhuSize1", String.valueOf(bitmapQueue.size()));

            if (imageAnalysis.bitmapQueue.size()>0 && imageAnalysis.okForTask ){
                imageAnalysis.okForTask = false;
                imageAnalysis.startImageAnalysis();
            }
        }


    }
    public void visibleCloseBtn(boolean visibility){
        if (visibility) {
//            exitBtn.setVisibility(VISIBLE);
            exitBtn.setEnabled(true);
        }else {
//            exitBtn.setVisibility(GONE);
            exitBtn.setEnabled(false);
        }
    }

    public int frontalFaceCount=0;
    public void isFrontal(int n){

//        Log.e("vishwam", "here");
        if (canAddData) {

//            Log.e("vishwam1", String.valueOf(canAddData));
//            Log.e("vishwam_islightEnabled", String.valueOf(islightEnabled));
            if (n == 1 && faceIsInTheBox && straightFaceFound /*&& islightEnabled*/) {

//                Log.e("vishwam_1", "here");
                if (faceRatioOk) {
                    if (!inRegCam && autoCap) {
                        frontalFaceCount++;
                        if (frontalFaceCount >= 15) {
//                            exitBtn.setVisibility(GONE);
//                            exitBtn.setEnabled(false);
                            takePicture = true;
                            canAddData = false;
                            okForImageAnalysis=false;
                            frontalFaceCount=0;
                        }
                    }
//                    Log.e("vishwam_2", "here");
                    mHintOutlinePaint = new Paint();
                    mHintOutlinePaint.setColor(getResources().getColor(R.color.green_color));
                    mHintOutlinePaint.setStyle(Paint.Style.STROKE);
                    mHintOutlinePaint.setStrokeWidth(getResources().getDimension(R.dimen.hintStroke));

                    mHintOutlinePaint2=new Paint();
                    mHintOutlinePaint2.setColor(Color.TRANSPARENT);
                    mHintOutlinePaint2.setStyle(Paint.Style.STROKE);
                    mHintOutlinePaint2.setStrokeWidth(getResources().getDimension(R.dimen.rectStroke));
//                    buttonChange(R.drawable.ic_mhere_cam_btn, true, "",true,getResources().getString(R.string.captureIntimation));
                } else {
//                    Log.e("vishwam_3", "here");
                    frontalFaceCount=0;
                    mHintOutlinePaint = new Paint();
                    mHintOutlinePaint.setColor(getResources().getColor(R.color.red_color));
                    mHintOutlinePaint.setStyle(Paint.Style.STROKE);
                    mHintOutlinePaint.setStrokeWidth(getResources().getDimension(R.dimen.hintStroke));

                    mHintOutlinePaint2=new Paint();
                    mHintOutlinePaint2.setColor(Color.TRANSPARENT);
                    mHintOutlinePaint2.setStyle(Paint.Style.STROKE);
                    mHintOutlinePaint2.setStrokeWidth(getResources().getDimension(R.dimen.rectStroke));
//                    buttonChange(R.drawable.ic_mhere_cam_btn_dis, false, "Come closer to camera",false,"");
                }

            } /*else if (n == 1 &&!islightEnabled) {
//                Log.e("vishwam_4", "here");
                frontalFaceCount=0;
                mHintOutlinePaint = new Paint();
                mHintOutlinePaint.setColor(getResources().getColor(R.color.red_color));
                mHintOutlinePaint.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint.setStrokeWidth(getResources().getDimension(R.dimen.hintStroke));

                mHintOutlinePaint2 = new Paint();
                mHintOutlinePaint2.setColor(Color.TRANSPARENT);
                mHintOutlinePaint2.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint2.setStrokeWidth(getResources().getDimension(R.dimen.rectStroke));
                buttonChange(R.drawable.disable_camera, false, "Low lighting detected", false, getResources().getString(R.string.captureIntimation));
            }*/ else if (n == 1 && faceIsInTheBox && !straightFaceFound) {
//                Log.e("vishwam_5", "here");
                frontalFaceCount=0;
                mHintOutlinePaint = new Paint();
                mHintOutlinePaint.setColor(getResources().getColor(R.color.red_color));
                mHintOutlinePaint.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint.setStrokeWidth(getResources().getDimension(R.dimen.hintStroke));

                mHintOutlinePaint2=new Paint();
                mHintOutlinePaint2.setColor(Color.TRANSPARENT);
                mHintOutlinePaint2.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint2.setStrokeWidth(getResources().getDimension(R.dimen.rectStroke));
//                buttonChange(R.drawable.ic_mhere_cam_btn_dis, false, "Please keep proper straight face",false,"");

            } else if (n > 1 && faceIsInTheBox) {
//                Log.e("vishwam_6", "here");
                frontalFaceCount=0;
                mHintOutlinePaint = new Paint();
                mHintOutlinePaint.setColor(getResources().getColor(R.color.red_color));
                mHintOutlinePaint.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint.setStrokeWidth(getResources().getDimension(R.dimen.hintStroke));

                mHintOutlinePaint2=new Paint();
                mHintOutlinePaint2.setColor(Color.TRANSPARENT);
                mHintOutlinePaint2.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint2.setStrokeWidth(getResources().getDimension(R.dimen.rectStroke));
//                buttonChange(R.drawable.ic_mhere_cam_btn_dis, false, "Multiple faces found",false,"");

            } else if (n == 1 && !faceIsInTheBox) {
//                Log.e("vishwam_7", "here");
                frontalFaceCount=0;
                mHintOutlinePaint = new Paint();
                mHintOutlinePaint.setColor(getResources().getColor(R.color.red_color));
                mHintOutlinePaint.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint.setStrokeWidth(getResources().getDimension(R.dimen.hintStroke));

                mHintOutlinePaint2=new Paint();
                mHintOutlinePaint2.setColor(Color.TRANSPARENT);
                mHintOutlinePaint2.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint2.setStrokeWidth(getResources().getDimension(R.dimen.rectStroke));
//                buttonChange(R.drawable.ic_mhere_cam_btn_dis, false, "Keep face inside the circle",false,"");
            } else {
//                Log.e("vishwam_8", "here");
                frontalFaceCount=0;
                mHintOutlinePaint = new Paint();
                mHintOutlinePaint.setColor(getResources().getColor(R.color.red_color));
                mHintOutlinePaint.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint.setStrokeWidth(getResources().getDimension(R.dimen.hintStroke));

                mHintOutlinePaint2=new Paint();
                mHintOutlinePaint2.setColor(Color.TRANSPARENT);
                mHintOutlinePaint2.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint2.setStrokeWidth(getResources().getDimension(R.dimen.rectStroke));
//                buttonChange(R.drawable.ic_mhere_cam_btn_dis, false, "Face not found",false,"");
            }
        }

    }

    public void buttonChange(final int n, final boolean b, final String instruction,final boolean txtvisible, final String captureTxt) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)

            public void run() {

//                Log.e("vishwam", instruction);

                if (!b){
                    straightFaceWarning.setText(instruction);
                } else {
                    straightFaceWarning.setText("");
                }

                if (txtvisible){
                    captureIntimationtxt.setText(Html.fromHtml(captureTxt));
                }else {
                    captureIntimationtxt.setText("");
                }
                btnTag.setEnabled(b);
                btnTag.setBackground(getResources().getDrawable(n));
            }
        });

    }


    @Override
    public void onClick(View view) {
//        if (readyForAnalysis ) {
            okForAnalysis = false;
            canAddData = false;
            takePicture = true;
            okForImageAnalysis = false;
            //takePicture();

//        } else {
//            Toast.makeText(context, "Please wait while image analysis is being setup!", Toast.LENGTH_LONG).show();
//        }
    }

  /*  @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends android.os.AsyncTask<String, String, List<Classifier.Recognition>> {

        @Override
        protected List<Classifier.Recognition> doInBackground(String... strings) {
            long yutime;

            int framesProcessed = 0;
            List<Classifier.Recognition> results = null;
            if (okForImageAnalysis) {
//                yutime=System.currentTimeMillis();
                yutime = System.currentTimeMillis();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                YuvImage image = new YuvImage(bytes, parameters.getPreviewFormat(),
                        mPreviewSize.getWidth(), mPreviewSize.getHeight(), null);
                int quality = 100;   // adjust this as needed

                image.compressToJpeg(new Rect(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight()), quality, out);
                finalByte = out.toByteArray();

//                Log.e("yuvTime",System.currentTimeMillis()-yutime+"");

                Bitmap finalBitmap = BitmapFactory.decodeByteArray(finalByte, 0, finalByte.length);
                Bitmap bitmap1 = Bitmap.createScaledBitmap(finalBitmap, 160, 160, false);
                Matrix matrix = new Matrix();
                matrix.postRotate(angle);
                rotated = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);
                final long currentTime = System.currentTimeMillis();
                results = classifier_light.recognizeImage(rotated);
//                Log.e("resultnew", results.toString());
//                Log.e("model_time_new", System.currentTimeMillis() - currentTime + "");
                final long tottalTime = System.currentTimeMillis() - currentTime;

                if (firstFrame) {
                    firstFrame = false;
                } else {

                    String resultsString = results.toString().replace(" ", "");
                    *//*if (canAddData) {
                        resultLightStringArray.add(resultsString);
                    }*//*
                    //Log.e("shankImageData", framesProcessed + ") " + resultsString);

                    *//*String stringID = results.get(0).getId();
                    int labelID = Integer.parseInt(stringID);

                    float labelConf = results.get(0).getConfidence() * 100;
//                    labelConf=labelConf*100;
                    Log.e("label_id", labelID + "");
                    Log.e("label_conf", labelConf + "");

                    if (labelID == 1) {
                        if (labelConf >= 0.5) {
                            islightEnabled = true;

                        } else {
                            islightEnabled = false;
                        }
                    } else {
                        if (labelConf > 0.5) {
                            islightEnabled = false;

                        }
                    }*//*
//                    framesProcessed++;
                }
                *//*Log.e("isLightEnabled 1", islightEnabled + "");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        timeTxt2.setText("light_model_time:" + String.valueOf(tottalTime));
                    }
                });*//*
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<Classifier.Recognition> results) {
            super.onPostExecute(results);
            if (results!= null) {
                String stringID = results.get(0).getId();
                int labelID = Integer.parseInt(stringID);

                float labelConf = results.get(0).getConfidence() * 100;
//                    labelConf=labelConf*100;
//                Log.e("label_id", labelID + "");
//                Log.e("label_conf", labelConf + "");

                if (labelID == 1) {
                    if (labelConf >= 0.5) {
                        islightEnabled = true;
//                        detectObject(finalByte, parameters, rotated);
                        if (!inRegCam && okForAnalysis) {
                            imageAnalysis1.startImageAnalysis(rotated);
                        }

                    } else {
                        islightEnabled = false;
                    }
                } else {
                    if (labelConf > 0.5) {
                        islightEnabled = false;

                    }
                }
            }

        }
    }*/

    public String checkLight(Bitmap bitmap){
        String resultsString = "NA";
        if (readyForAnalysis && bitmap!=null) {
            List<Classifier.Recognition> results = classifier_light.recognizeImage(bitmap);
            resultsString = results.toString().replace(" ", "");
        }
        return resultsString;
    }
    public void stopImageAnalysis(){
        classifier_light.close();
    }

    public void initAnalytics(){
        //   AppCenter.start(getApplication(), "edae2aab-a04a-49cd-8c64-ae5872a61a44", Analytics.class, Crashes.class);
        // AppCenter.start(getApplication(), "2215a2b1-5bba-4845-8e03-3af284cce984", Analytics.class, Crashes.class);
        AppCenter.start(((Activity) context).getApplication(), "e582bc74-3900-4c08-ab0f-6995f5663539", Analytics.class, Crashes.class);
        Analytics.setEnabled(true);
    }


    /*void detectObject(byte[] data, Camera.Parameters parameters, Bitmap bitmap) {

//        Camera.Parameters parameters = camera.getParameters();
        int previewW = mPreviewSize.getWidth();
        int previewH = mPreviewSize.getHeight();

        ImageObject imageObject = new ImageObject(data, previewW, previewH, parameters, angle, bitmap);

        if (!inRegCam && okForAnalysis){
            Log.e("vishwam_IA_start2","here");
            imageAnalysis1.bitmapQueue.add(imageObject);
                Log.e("vishwam_Size1", String.valueOf(imageAnalysis1.bitmapQueue.size()));

            if (imageAnalysis1.bitmapQueue.size()>0 && imageAnalysis1.okForTask ){
                Log.e("vishwam_IA_start3","here");
                Log.e("vishwam_Size3", String.valueOf(imageAnalysis1.bitmapQueue.size()));
                imageAnalysis1.okForTask = false;
                imageAnalysis1.startImageAnalysis();
            }
        }
    }*/
}
