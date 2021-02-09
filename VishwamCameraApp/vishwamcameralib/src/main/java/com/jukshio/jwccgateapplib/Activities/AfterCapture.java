package com.jukshio.jwccgateapplib.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.jukshio.jwccgateapplib.R;

import static com.jukshio.jwccgateapplib.Activities.AuthCameraActivity.rotatedBitmap2;

public class AfterCapture extends AppCompatActivity {

    ImageView imageView1,imageView;
    LinearLayout layout1,layout2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_after_capture);
        imageView=findViewById(R.id.imageView);
        layout1=findViewById(R.id.layout1);
        layout2=findViewById(R.id.layout2);
        imageView1=findViewById(R.id.imageView1);
        imageView1.setImageBitmap(rotatedBitmap2);
        imageView.setImageBitmap(rotatedBitmap2);
//        Glide.with(getApplicationContext())
//                .load(rotatedBitmap2)
//                .into(imageView);

        Handler handler=new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);
            }
        },1000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },5000);

    }
}