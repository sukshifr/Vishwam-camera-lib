package com.jukshio.jwccgateapplib.FRCaptureView;

/**
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
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.SparseArray;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.jukshio.jwccgateapplib.FRCaptureView.FaceGraphic;
import com.jukshio.jwccgateapplib.Tracker.GraphicOverlay;


import java.util.List;

public class FaceDect {

    private Context context;

    public static boolean straightFaceFound = false;
    public static boolean detectorAvailable = true;

    public static OnMultipleFacesDetectedListener onMultipleFacesDetectedListener;
    public static OnCaptureListener onCaptureListener;
    public static OnNextFrameListener onNextFrameListener;
    public static OnCameraCloseListener onCameraCloseListener;

    public static FaceDetector previewFaceDetector = null, authFaceDetector = null;
    private GraphicOverlay mGraphicOverlay;
    private FaceGraphic mFaceGraphic;

//    public static double Rx_L = 0.7, Rx_U = 1.4, Ry_L = 0.8, Ry_U = 1.7;
//    public static double slope_T = 0.0556;

     public static Long startTime = 0L;
    Long endTime = 0L;

    int stage=0;
    float delay = -1;

//    public static OnFrontalFaceDetectedListener mOnFrontalFaceDetectedListener;

    /**
     * Interface callback on multiple faces detected with face count.
     * **/
    public interface OnMultipleFacesDetectedListener {
        void onMultipleFacesDetected(int n);
    }
    /**
     * Interface callback return captured image byte array and angle of orientation image.
     * **/
    public interface OnCaptureListener {
        void onCapture(byte[] data, int angle);
    }

    public interface OnNextFrameListener {
        void onSetNextFrame(byte[] data1, Camera.Parameters parameters, Size mPreviewSize, int angle);
    }
    public interface OnCameraCloseListener {
        void onCameraClose();
    }


    /*public static interface OnFrontalFaceDetectedListener {
        public void onFrontalFaceDetected(byte[] data);
    }*/
    /**
     * Constructor initialise context and graphicoverlay objects and initialising multiple face interface and oncapture interface.
     *
     * **/
    public FaceDect(Context mcontext, GraphicOverlay graphicOverlay) {

        this.context = mcontext;
        mGraphicOverlay = graphicOverlay;

        initialisefaceDetec();
//        this.mOnFrontalFaceDetectedListener = (OnFrontalFaceDetectedListener) context;
        this.onMultipleFacesDetectedListener = (OnMultipleFacesDetectedListener) context;
        this.onCaptureListener= (OnCaptureListener) context;
        this.onNextFrameListener = (OnNextFrameListener) context;
        this.onCameraCloseListener=(OnCameraCloseListener) context;
    }

    public FaceDect(Context mcontext) {

        this.context = mcontext;

        initialisefaceDetec();

    }
    /**
     * This  method used to initialise FaceDetector and set it with mutliprocessor using its Builder class.
     * Multiprocessor is a class to handle high speed frames stream optimised to use multiple processors.
     * **/
    public void initialisefaceDetec() {

        startTime = System.currentTimeMillis();

        previewFaceDetector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(false)
                .setTrackingEnabled(true)
                .build();
//        Log.e("vishwam","Here");

        authFaceDetector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(false)
                .setTrackingEnabled(true)
                .build();

        if (authFaceDetector.isOperational()) {
            //Log.e("Detector","found");
            detectorAvailable = true;
            previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
            authFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
        } else {
            detectorAvailable = false;

            //Log.e("Detector","Not found");
        }
    }

    public Bitmap getCroppedFace(Bitmap bitmap){

        if (detectorAvailable) {

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = previewFaceDetector.detect(frame);

            if (faces.size() == 1) {
                Face face = faces.valueAt(0);

                List<Landmark> landmarks = face.getLandmarks();
                int landmarksCount = landmarks.size();

                float centerX = face.getPosition().x + face.getWidth() / 2.0f;
                float centerY = face.getPosition().y + face.getHeight() / 2.0f;
                float faceWidth = face.getWidth();
                float faceHeight = face.getHeight();

                float crop_size = 1;

                // Draw a box around the face.
                float left = centerX - (faceWidth / 2 * crop_size);
                float right = centerX + (faceWidth / 2 * crop_size);
                float top = centerY - (faceHeight / 2 * 1.5f);
                float bottom = centerY + (faceHeight / 2 * crop_size);

                if (left < 0) left = 0;
                if (top < 0) top = 0;
                if (right > bitmap.getWidth()) right = bitmap.getWidth();
                if (bottom > bitmap.getHeight()) bottom = bitmap.getHeight();

                Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (int) (left), (int) (top), (int) (right - left), (int) (bottom - top));

                delay = (bitmap.getHeight()*bitmap.getHeight())/(croppedBitmap.getHeight()*croppedBitmap.getHeight());
                return croppedBitmap;
            } else {
                return bitmap;
            }
        }else {
            return getSquareBitmap(bitmap);
        }
    }

    public Bitmap getSquareBitmap(Bitmap bitmap){
        int originalImageHeight = bitmap.getHeight();
        int originalImageWidth = bitmap.getWidth();
        Bitmap squareBitmap=  Bitmap.createBitmap(bitmap, originalImageWidth / 10, (originalImageHeight / 2 - 2 * originalImageWidth / 5), 4 * originalImageWidth / 5, 4 * originalImageWidth / 5);

        delay = (bitmap.getHeight()*bitmap.getHeight())/(squareBitmap.getHeight()*squareBitmap.getHeight());

        return squareBitmap;
    }

    public int checkMultipleFaces(Bitmap bitmap) {

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = previewFaceDetector.detect(frame);

        if(faces.size() == 1) {
            return 1;
        }else if (faces.size()> 1){
            return 2;


        }else {

            return  0;
        }
    }

    public int checkMultipleFacesForGalleryImage(Bitmap bitmap) {

        FaceDetector detector = new FaceDetector.Builder(context).setTrackingEnabled(false).build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        if(faces.size() == 1) {
            return 1;
        }else if (faces.size()> 1){
            return 2;
        }else {
            return  0;
        }
    }

    public int checkMultipleFacesInImage(Bitmap bitmap) {

        FaceDetector detector = new FaceDetector.Builder(context).setTrackingEnabled(false).build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        if(faces.size() == 1) {
            return 1;
        }else if (faces.size()> 1){
            return 2;
        }else {
            return  0;
        }
    }

    public int checkMultipleFacesInFrame(Bitmap bitmap) {
        FaceDetector detector = new FaceDetector.Builder(context).setTrackingEnabled(false).build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        if(faces.size() == 1) {
            return 1;
        }else if (faces.size()> 1){
            return 2;
        }else {
            return  0;
        }
    }

    public  int getVersion(){
        if (!detectorAvailable){
            return 13;
        }
        return stage;
    }

    public float getDelay(){
        return delay;
    }


    /**
     * This is class implementing mutilprocessorfactory.on create it then passes on newly create graphicfacetracker class.
     * **/
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {

        private GraphicOverlay mOverlay;
        /**
         * Constructor to initialise graphicovelay.
         * **/
        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, context);

        }
        /**
         * on new face detected this method is called to add new face item.
         * **/
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }
        /**
         * Ondetector detected is this method is called and is used to get landmarks.
         * Using landmarks to find the slope of two eye points and calculating the rx and ry.
         * FaceGraphic is then updated with face object.
         * **/



        @SuppressLint("ResourceAsColor")
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, final Face face) {

            List<Landmark> landmarks = face.getLandmarks();
            int landmarksCount = landmarks.size();
//            Log.e("landmarksCount", String.valueOf(landmarksCount));

//            long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(startTime);

//            Log.e("millisecs", String.valueOf(startTime));


            if (landmarksCount == 12) {
                float slope = (landmarks.get(1).getPosition().y - landmarks.get(0).getPosition().y) / (landmarks.get(1).getPosition().x - landmarks.get(0).getPosition().x);
                float Ry = (landmarks.get(3).getPosition().y - landmarks.get(2).getPosition().y) / (landmarks.get(2).getPosition().y - (landmarks.get(0).getPosition().y + landmarks.get(1).getPosition().y) / 2);
                float Rx = (landmarks.get(1).getPosition().x - landmarks.get(2).getPosition().x) / (landmarks.get(2).getPosition().x - landmarks.get(0).getPosition().x);

                //Log.e("vishwamSukshiAngle", String.valueOf(Math.atan(slope)*180/3.14));
//                Log.e("santhuValues",Rx_L+","+Rx_U+","+Ry_L+","+Ry_U+","+slope_T);

//                double slopeThreshold = Math.tan(Math.PI/18);
                double slopeThreshold = 0;

//                Log.e("SRxRy", Rx + ", " + Ry + ", " + slope );

                if(endTime-startTime <= 15000){

                    stage = 11;
//                    Log.e("less10","here");
                     slopeThreshold = Math.tan(Math.PI*0.0556);


//                if(0.7 <= Ry && Ry <= 1.8 && 0.6 <= Rx && Rx <= 1.5 && ((-1 * slopeThreshold) < slope) && (slope < slopeThreshold)){
                    if(0.8 <= Ry && Ry <= 1.7 && 0.7 <= Rx && Rx <= 1.4 && ((-1 * slopeThreshold) < slope) && (slope < slopeThreshold)){
                        straightFaceFound = true;
                    }else{
                        straightFaceFound = false;
                    }
                }else {
                    stage = 12;
//                    Log.e("greater10","here");
                    slopeThreshold = Math.tan(Math.PI*0.0556*2);

                    if(0.9 <= Ry && Ry <= 2.3 && 0.46 <= Rx && Rx <= 2.2 && ((-1 * slopeThreshold) < slope) && (slope < slopeThreshold)){
                        straightFaceFound = true;
                    }else{
                        straightFaceFound = false;
                    }

                }

                endTime = System.currentTimeMillis();

            }else if (landmarksCount == 8){
                float slope = (landmarks.get(1).getPosition().y - landmarks.get(0).getPosition().y) / (landmarks.get(1).getPosition().x - landmarks.get(0).getPosition().x);
                float Ry = (landmarks.get(7).getPosition().y - landmarks.get(2).getPosition().y) / (landmarks.get(2).getPosition().y - (landmarks.get(0).getPosition().y + landmarks.get(1).getPosition().y) / 2);
                float Rx = (landmarks.get(1).getPosition().x - landmarks.get(2).getPosition().x) / (landmarks.get(2).getPosition().x - landmarks.get(0).getPosition().x);

//                double slopeThreshold = Math.tan(Math.PI/18);
                //Log.e("SRxRy", Rx + ", " + Ry + ", " + slope + ", " + slopeThreshold);

                double slopeThreshold;

                if(endTime-startTime <= 15000) {
                    stage = 11;

                    slopeThreshold = Math.tan(Math.PI/18);

                    if (0.8 <= Ry && Ry <= 1.7 && 0.7 <= Rx && Rx <= 1.4 && ((-1 * slopeThreshold) < slope) && (slope < slopeThreshold)) {
                        straightFaceFound = true;
                    } else {
                        straightFaceFound = false;
                    }
                }else {
                    stage = 12;
                    slopeThreshold = Math.tan(Math.PI*0.0556*2);

                    if(0.9 <= Ry && Ry <= 2.3 && 0.46 <= Rx && Rx <= 2.2 && ((-1 * slopeThreshold) < slope) && (slope < slopeThreshold)){
                        straightFaceFound = true;
                    } else {
                        straightFaceFound = false;
                    }
                }
                endTime = System.currentTimeMillis();

            } else{
                straightFaceFound = false;
            }

            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }
        /**
         * On No face detected is method is called and previously inflated graphic overlay is removed.
         * **/
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {

            mFaceGraphic.goneFace();
            mOverlay.remove(mFaceGraphic);
            mOverlay.clear();
        }
        /**
         * OnDone overlay is removed.
         * **/
        @Override
        public void onDone() {

            mFaceGraphic.goneFace();
            mOverlay.remove(mFaceGraphic);
            mOverlay.clear();
        }
    }


}