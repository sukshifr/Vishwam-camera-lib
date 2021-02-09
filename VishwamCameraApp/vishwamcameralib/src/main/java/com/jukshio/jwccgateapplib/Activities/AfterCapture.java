package com.jukshio.jwccgateapplib.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;


import com.jukshio.jwccgateapplib.R;

import static com.jukshio.jwccgateapplib.Activities.AuthCameraActivity.rotatedBitmap2;

public class AfterCapture extends AppCompatActivity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_capture);
        imageView=findViewById(R.id.imageView);

        imageView.setImageBitmap(rotatedBitmap2);
//        Glide.with(getApplicationContext())
//                .load(rotatedBitmap2)
//                .into(imageView);

        Handler handler=new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);


    }
}