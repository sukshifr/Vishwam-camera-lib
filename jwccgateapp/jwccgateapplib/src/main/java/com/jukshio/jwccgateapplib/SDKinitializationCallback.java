package com.jukshio.jwccgateapplib;


import com.jukshio.jwccgateapplib.ImageAnalysis.ImageAnalysis;

public interface SDKinitializationCallback {
    void OnInitialized(String message, ImageAnalysis imageAnalysis);
}
