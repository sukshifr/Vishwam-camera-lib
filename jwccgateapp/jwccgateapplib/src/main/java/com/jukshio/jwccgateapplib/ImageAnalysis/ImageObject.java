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

import android.graphics.Bitmap;
import android.hardware.Camera;

public class ImageObject {

    public byte[] byteArray;
    public int previewW, previewH, angle;
    public Camera.Parameters parameters;
    public Bitmap bitmap;

    public ImageObject(byte[] byteArray, int previewW, int previewH, Camera.Parameters parameters, int angle, Bitmap bitmap1) {
        this.byteArray = byteArray;
        this.previewW = previewW;
        this.previewH = previewH;
        this.parameters = parameters;
        this.angle = angle;
        this.bitmap = bitmap1;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap1) {
        this.bitmap = bitmap1;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public int getPreviewW() {
        return previewW;
    }

    public int getPreviewH() {
        return previewH;
    }

    public Camera.Parameters getParameters() {
        return parameters;
    }

    public int getAngle() {
        return angle;
    }
}
