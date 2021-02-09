package com.jukshio.jwccgateapplib;

import android.graphics.Bitmap;

public interface OncaptureImageCallback {
    void onImageCaptured(Bitmap capturedImage, String Response);
}
