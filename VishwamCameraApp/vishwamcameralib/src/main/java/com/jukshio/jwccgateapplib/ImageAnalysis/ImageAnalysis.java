package com.jukshio.jwccgateapplib.ImageAnalysis;

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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jukshio.jwccgateapplib.Utils2;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//import static com.sukshi.vishwamattendancefrlib.Networking.VishwamNetworkHelper.loadedModelName;

public class ImageAnalysis {

    AssetManager assetManager;
    Context context;

    public Thread thread;
    public Queue<ImageObject> bitmapQueue = new LinkedList<>();
    public static ArrayList<String> resultStringArray = new ArrayList<>();
    public static boolean okForTask;
    public static boolean firstFrame;
    public static boolean okForAnalysis, readyForAnalysis = false, canAddData = true, okForImageAnalysis;
    static int totalNumFrames;
    static int realFrames;
    static int fakeFrames;
    static Bitmap realImage;
    static Bitmap fakeImage;
    float realThreshValue = 0.007f, fakeThreshValue = 0.006f, frameMeanUnder10 = 0.65f, frameMean10To20 = 0.7f, frameMeanOver20 = 0.8f;

//    final String MODEL_PATH = "31012019_ia.tflite";
//    final String MODEL_PATH = "18032020.tflite";
    /*final String LABEL_PATH = "22_01_2019_ia.txt";
    final String MODEL_PATH = "14042020.tflite";
    final int INPUT_SIZE = 224;*/

    final String LABEL_PATH = "08_01_2021_ia.txt";
    final String MODEL_PATH = "08012021_A.tflite";
    final int INPUT_SIZE = 224;

//    final String LABEL_PATH_LIGHT = "size160_light_det.txt";
//    final String MODEL_PATH_LIGHT = "size160_light_det.tflite";


    public static int framesAddedCount =0;

    public static Classifier classifier;
    public static Classifier classifier_light;
    private Executor executor = Executors.newSingleThreadExecutor();
//    private Executor executor_light = Executors.newSingleThreadExecutor();

    Bitmap image160;

    File modeFile;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String sdkModelName = "";

    public ImageAnalysis(AssetManager assetManager, Context context) {
        this.assetManager = assetManager;
        this.context = context;
        preferences = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        editor = preferences.edit();
//        initAnalytics();
        Log.e("imageAnalysssssss","imageAnalysissssssssssssssssss");
    }

    public void initImageAnalysis() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sdkModelName = preferences.getString("savedModelName", "empty.tflite");
                    modeFile = Utils2.getModelFile(context, sdkModelName);
                    classifier = TensorFlowImageClassifier.create(
                            assetManager,
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            modeFile);

                    readyForAnalysis = true;
//                    initLightDetection();

                    Log.e("initImageAnalysis", "done");

                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

/*    public void initLightDetection() {

        executor_light.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier_light = TensorFlowImageClassifier.create(
                            assetManager,
                            MODEL_PATH_LIGHT,
                            LABEL_PATH_LIGHT,
                            INPUT_SIZE, modeFile);

                    readyForAnalysis = true;

                    Log.e("initLightDetection", "done");

                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }*/

    public List<Classifier.Recognition> getImageAnalysisData(Bitmap bitmap){

        List<Classifier.Recognition> result = classifier.recognizeImage(bitmap);
        return result;
    }

    public String getImageAnalysisDataString(Bitmap bitmap){

        List<Classifier.Recognition> result = classifier.recognizeImage(bitmap);
        String resultsString = result.toString().replace(" ", "");

        return resultsString;
    }

    public static void setupImageAnalysis(){
        totalNumFrames = 0;
        realFrames = 0;
        fakeFrames = 0;
        realImage = null;
        fakeImage = null;
        okForTask = true;
        firstFrame = true;
        canAddData = true;
        framesAddedCount = 0;
        okForAnalysis = true;
        okForImageAnalysis = true;
        resultStringArray.clear();
    }

    public void startImageAnalysis(){
        Log.e("stepCheck_analysis_1","in StartImageAnalysis method");
        new MyAsyncTask().execute();
    }

    int framesProcessed = 0;
    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends android.os.AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Log.e("shank", "in ImageAnalysisTask");

            Iterator<ImageObject> iterator = bitmapQueue.iterator();
            int framesProcessed = 0;


            while (iterator.hasNext()) {

                framesAddedCount++;
                Log.e("stepCheck_analysis_2","Printing frameNo"+framesAddedCount);
                if (okForAnalysis && framesAddedCount < 16) {
//                if (okForAnalysis) {
//                    Log.e("framesCount", framesAddedCount + " :in ImageAnalysisTask");
                    ImageObject currentImageObject = bitmapQueue.remove();

                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    YuvImage image = new YuvImage(currentImageObject.byteArray, currentImageObject.parameters.getPreviewFormat(),
                            currentImageObject.previewW, currentImageObject.previewH, null);
                    int quality = 100;   // adjust this as needed

                    image.compressToJpeg(new Rect(0, 0, currentImageObject.previewW, currentImageObject.previewH), quality, out);
                    byte[] finalByte = out.toByteArray();
                    Bitmap finalBitmap = BitmapFactory.decodeByteArray(finalByte, 0, finalByte.length);

                    Bitmap bitmap1 = Bitmap.createScaledBitmap(finalBitmap, 224, 224, false);

                    Matrix matrix = new Matrix();
                    matrix.postRotate(currentImageObject.angle);
                    Bitmap rotated = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);

                    final List<Classifier.Recognition> results = classifier.recognizeImage(rotated);
                    if (firstFrame) {
                        firstFrame = false;
                    } else {

                        String resultsString = results.toString().replace(" ", "");
                        if (canAddData) {
                            resultStringArray.add(resultsString);
                        }
                        //Log.e("shankImageData", framesProcessed + ") " + resultsString);

                        String stringID = results.get(0).getId();
                        int labelID = Integer.parseInt(stringID);

                        Float labelConf = results.get(0).getConfidence();

                        if (labelID == 0) {
                            if (labelConf >= realThreshValue) {
                                totalNumFrames++;
                                realFrames++;
                                realImage = rotated;
                            }
                        } else {
                            if (labelConf >= fakeThreshValue) {
                                totalNumFrames++;
                                fakeFrames++;
                                fakeImage = rotated;
                            }
                        }
                        framesProcessed++;
                    }
                } else {
                    framesProcessed = 1;
                    bitmapQueue.clear();
                }
            }
            return null;
        }
    }

    //If this bitmap is null, send compressedImage.
    public Bitmap getImage1(){

        okForAnalysis = false;

        int real = realFrames, fake = fakeFrames, total = totalNumFrames;
        float frameMean;

        if (total != 0){

            if (total < 10) {
                frameMean = frameMeanUnder10;
            } else if (10 <= total && total < 20) {
                frameMean = frameMean10To20;
            } else {
                frameMean = frameMeanOver20;
            }

            float index = Float.valueOf(real) / Float.valueOf(total);

            //Log.e("shankNumbers", String.valueOf(real) + ", " + String.valueOf(fake) + ", " + String.valueOf(total));
            //Log.e("shankIndex", String.valueOf(index));

            if (index > frameMean){
                //Log.e("shank", "Real");
                return realImage;
            }else{
                //Log.e("shank", "Fake");
                return fakeImage;
            }
        }else {
            return null;
        }
    }

    public int getImageIndex1(){

        okForAnalysis = false;

        int real = realFrames, fake = fakeFrames, total = totalNumFrames;
        float frameMean;

        if (total != 0){

            if (total < 10) {
                frameMean = frameMeanUnder10;
            } else if (10 <= total && total < 20) {
                frameMean = frameMean10To20;
            } else {
                frameMean = frameMeanOver20;
            }

            float index = Float.valueOf(real) / Float.valueOf(total);

            //Log.e("shankNumbers", String.valueOf(real) + ", " + String.valueOf(fake) + ", " + String.valueOf(total));
            //Log.e("shankIndex", String.valueOf(index));

            if (index > frameMean){
                //Log.e("shank", "Real");
                return 0;
            }else{
                //Log.e("shank", "Fake");
                return 1;
            }
        }else {
            return 0;
        }
    }

    public String checkImageAnalysis(Bitmap bitmap){
        String resultsString = "NA";
        if (readyForAnalysis && bitmap!=null) {
            List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
            resultsString = results.toString().replace(" ", "");
        }
        return resultsString;
    }

//    public static String getVersion(){
//        return loadedModelName;
//    }
    public static String getImageIndex(){

        //index for delay
        okForAnalysis = false;

//        if (totalNumFrames != 0){

        if (resultStringArray.size()> 0){
            String result = resultStringArray.toString().replaceAll(" ", "");
            return result;
        }else{
            return "";
        }
    }
    public void initAnalytics(){
        //   AppCenter.start(getApplication(), "edae2aab-a04a-49cd-8c64-ae5872a61a44", Analytics.class, Crashes.class);
        // AppCenter.start(getApplication(), "2215a2b1-5bba-4845-8e03-3af284cce984", Analytics.class, Crashes.class);
        AppCenter.start(((Activity)context).getApplication(), "e582bc74-3900-4c08-ab0f-6995f5663539", Analytics.class, Crashes.class);
        Analytics.setEnabled(true);
    }
}
