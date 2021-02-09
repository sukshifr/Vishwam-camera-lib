package com.jukshio.jwccgateapplib.FRCaptureView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import android.view.View;

import com.jukshio.jwccgateapplib.R;


@SuppressLint("ViewConstructor")
public class CameraView extends View {

    private Bitmap bitmap;
    private Canvas cnvs;
    private Paint p = new Paint();
    private Paint transparentPaint = new Paint();
    private Paint semiTransparentPaint = new Paint();
    private int parentWidth;
    private int parentHeight;

    int height;
    int width;
    public RectF oval1;
    Context context;
    public static int circleX, circleY, circleRadius;


    public CameraView(Context context) {
        super(context);
//        this.rCustomLayout = relativeLayout;
        this.context = context;
        init();
    }

    @SuppressLint("NewApi")
    private void init() {

        transparentPaint.setColor(getResources().getColor(R.color.whiteopacity));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        semiTransparentPaint.setColor(getResources().getColor(android.R.color.white));
        Shader gradient = new LinearGradient(0,0,0,getHeight(), getResources().getColor(R.color.whiteopacity), getResources().getColor(R.color.whiteopacity),Shader.TileMode.MIRROR);
        semiTransparentPaint.setDither(true);
        semiTransparentPaint.setShader(gradient);
    }

    @SuppressLint({"DrawAllocation", "CanvasSize"})
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        height = metrics.heightPixels;
        width = metrics.widthPixels;

        circleX = width / 2;
        circleY = height / 2;

        circleRadius = (int) (width * 0.4);

        bitmap = Bitmap.createBitmap(parentWidth, parentHeight, Bitmap.Config.ARGB_8888);
        cnvs = new Canvas(bitmap);

        oval1 = new RectF((width/2) - (width*2/5), (width*2/3) - (width*2/5), (width/2) + (width*2/5), (width*2/3) + (width*2/5));

        cnvs.drawRect(0, 0, cnvs.getWidth(), cnvs.getHeight(), semiTransparentPaint);
        cnvs.drawOval(oval1,transparentPaint );

        canvas.drawBitmap(bitmap, 0, 0, p);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
