package com.jukshio.jwccgateapplib.FRCaptureView;

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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;

import com.google.android.gms.vision.face.Face;
import com.jukshio.jwccgateapplib.FRCaptureView.ParamsView;
import com.jukshio.jwccgateapplib.R;
import com.jukshio.jwccgateapplib.Tracker.GraphicOverlay;

import static com.jukshio.jwccgateapplib.FRCaptureView.CameraView.circleRadius;
import static com.jukshio.jwccgateapplib.FRCaptureView.CameraView.circleX;
import static com.jukshio.jwccgateapplib.FRCaptureView.CameraView.circleY;
import static com.jukshio.jwccgateapplib.FRCaptureView.FaceDect.detectorAvailable;
import static com.jukshio.jwccgateapplib.FRCaptureView.FaceDect.startTime;


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private Bitmap marker;

    private BitmapFactory.Options opt;
    private Resources resources;

    private int faceId;
    Context context1;

    float isSmilingProbability = -1;
    float eyeRightOpenProbability = -1;
    float eyeLeftOpenProbability = -1;

    public   static Paint mHintOutlinePaint,mHintOutlinePaint2;
    private Rect rectangle;
    public Paint mHintTextPaint;

    private volatile Face mFace;

    public static Float faceArea;

    public static boolean faceIsInTheBox =false , faceRatioOk;

    public FaceGraphic(GraphicOverlay overlay, Context context) {
        super(overlay);
        this.context1 = context;
        opt = new BitmapFactory.Options();
        opt.inScaled = false;
        resources = context.getResources();
        marker = BitmapFactory.decodeResource(resources, R.drawable.marker, opt);
        initializePaints(resources);
    }

    public void setId(int id) {
        faceId = id;
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    private void initializePaints(Resources resources) { }

    public void goneFace() {
        mFace = null;
    }

    float left = 0, right = 0, top = 0, bottom = 0;


    @Override
    public void draw(Canvas canvas) {

        /*Face face = mFace;
        if(face == null) {

            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            isSmilingProbability = -1;
            eyeRightOpenProbability= -1;
            eyeLeftOpenProbability = -1;
            return;
        }

        float centerX = translateX(face.getPosition().x + face.getWidth() / 2.0f);
        float centerY = translateY(face.getPosition().y + face.getHeight() / 2.0f);
        float offsetX = scaleX(face.getWidth() / 2.0f);
        float offsetY = scaleY(face.getHeight() / 2.0f);

        // Draw a box around the face.
        float left = centerX - offsetX * 0.75f;
        float right = centerX + offsetX * 0.75f;
        float top = centerY - offsetY * 0.75f;
        float bottom = centerY + offsetY * 0.75f;

        faceArea = (right-left) * (bottom-top);
//        Log.e("vishwamFaceArea", String.valueOf(faceArea));

        if (mHintOutlinePaint != null){
            canvas.drawRect(left, top, right, bottom, mHintOutlinePaint);
        }*/

        if (detectorAvailable){
            Face face = mFace;
            if(face == null) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                return;
            }

            float centerX = translateX(face.getPosition().x + face.getWidth() / 2.0f);
            float centerY = translateY(face.getPosition().y + face.getHeight() / 2.0f);
            float offsetX = scaleX(face.getWidth() / 2.0f);
            float offsetY = scaleY(face.getHeight() / 2.0f);

        /*//Draw a box around the face.
        float left, right, top, bottom;*/

            left = centerX - offsetX * 0.75f;
            right = centerX + offsetX * 0.75f;
            top = centerY - offsetY * 0.75f;
            bottom = centerY + offsetY * 0.75f;
            if (ParamsView.correctFace){
                top = top * (circleX * 4) / (3 * circleY);
                bottom = bottom * (circleX * 4) / (3 * circleY);
            }else {
//                top = top * (circleX * 4) / (3 * circleY);
//                bottom = bottom * (circleX * 4) / (3 * circleY);
            }


//            rectangle=new Rect(0,0,ParamsView.width,ParamsView.width*4/3);
            rectangle=new Rect(7,7,ParamsView.width-7,ParamsView.width*4/3);

            faceIsInTheBox = isFaceInTheBox(left, right, top, bottom);

            float boxArea = 4 * circleX * circleX * 16 / 25;
            float faceArea = (right-left) * (bottom-top);
            float faceRatio = boxArea / faceArea;

            faceRatioOk = (faceRatio < 6);

            if (mHintOutlinePaint != null && mHintOutlinePaint2 !=null) {
//            canvas.drawRect(left, top, right, bottom, mHintOutlinePaint);
                canvas.drawCircle(circleX, circleX*4/3, circleRadius, mHintOutlinePaint);
                canvas.drawRect(rectangle,mHintOutlinePaint2);
            }
        }else{

            Paint mHintOutlinePaint1 = new Paint();
            if (System.currentTimeMillis()-startTime <= 3000 ){

                mHintOutlinePaint1.setColor(context1.getResources().getColor(R.color.red_color));
                mHintOutlinePaint1.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint1.setStrokeWidth(context1.getResources().getDimension(R.dimen.hintStroke));
                canvas.drawCircle(circleX, circleX*4/3, circleRadius, mHintOutlinePaint1);
            }else {

                mHintOutlinePaint1.setColor(context1.getResources().getColor(R.color.green_color));
                mHintOutlinePaint1.setStyle(Paint.Style.STROKE);
                mHintOutlinePaint1.setStrokeWidth(context1.getResources().getDimension(R.dimen.hintStroke));
                canvas.drawCircle(circleX, circleX*4/3, circleRadius, mHintOutlinePaint1);
            }

        }


    }

    public boolean isFaceInTheBox(float left, float right, float top, float bottom) {

        Point p1 = new Point((int)(left+right)/2, (int)top);
        Point p2 = new Point((int)right, (int)(top+bottom)/2);
        Point p3 = new Point((int)(left+right)/2, (int)bottom);
        Point p4 = new Point((int)left, (int)(top+bottom)/2);

        if (isForeheadInsideCircle(p1) && isPointInsideCircle(p2) && isPointInsideCircle(p3) && isPointInsideCircle(p4)){
            //if (isPointInsideCircle(p1) && isPointInsideCircle(p2) && isPointInsideCircle(p3) && isPointInsideCircle(p4)){
            return true;
        }else return false;
    }

    public boolean isPointInsideCircle(Point point){

        Point circleCentre = new Point(circleX, circleX*4/3);

        double dX = circleCentre.x - point.x;
        double dY = circleCentre.y - point.y;

        double distance = Math.sqrt(dX * dX + dY * dY);

        if (distance <= circleRadius){
            return  true;
        }else return false;
    }
    /**
     * Return a boolean to detect whelther point is in circle or not.This is calculated by finding distance from centre to given point and comparing with circle radius.
     * **/

    public boolean isForeheadInsideCircle(Point point){

        Point circleCentre = new Point(circleX, circleX*4/3);

        double dX = circleCentre.x - point.x;
        double dY = circleCentre.y - point.y;

        double distance = Math.sqrt(dX * dX + dY * dY);

        if (distance <= circleRadius - ((right - left)/4)){
            return  true;
        }else return false;
    }
}