package com.txhung.cv2app.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class PainView extends View{
    public int BRUSH_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.rgb(0,177,64);
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private Bitmap bitmapBackground;
    private  static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private final ArrayList<FingerPath> paths =new ArrayList<>();
    private int currentColor;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitMap;
    private Canvas mCanvas;
    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private int width;
    private int height;


    public PainView(Context context) {
        super(context);
    }

    public PainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

        mEmboss = new EmbossMaskFilter(new float[] {1,1,1},0.4f,6,3.5f);
        mBlur = new BlurMaskFilter(5,BlurMaskFilter.Blur.NORMAL);

    }

    public void  init(DisplayMetrics metrics, Bitmap bitmap){
        height = metrics.heightPixels;
        width = metrics.widthPixels;
        bitmapBackground = bitmap;
        mBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitMap);
        Log.d("IMG WIDTH: ", Integer.toString(bitmapBackground.getWidth()));
        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;

    }

    public void normal(){
        emboss = false;
        blur = false;
    }
    public void emboss(){
        emboss = true;
        blur = false;
    }
    public void clear(){
        paths.clear();
        normal();
        invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    //    mCanvas.drawColor(backgroundColor);
        int imgWidth = bitmapBackground.getWidth();
        int imgHeight = bitmapBackground.getHeight();
        float rate = width/imgWidth;
        int dstHeight = Math.round(imgHeight*rate);
        int dstWidth = Math.round(imgWidth/rate);
        @SuppressLint("DrawAllocation") Rect rect = new Rect(0,0,width,dstHeight);

        canvas.drawBitmap(bitmapBackground,0, 0, mBitmapPaint);

        for (FingerPath fp : paths){
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            mCanvas.drawPath((Path) fp.path, mPaint);

        }
        canvas.drawBitmap(mBitMap,0,0, mBitmapPaint);
        canvas.save();
        canvas.restore();

    }
    private void touchStart(float x, float y){
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);
        mPath.reset();
        mPath.moveTo(x,y);
        mX = x;
        mY = y;
        Log.d("DEbug TAG", "TouchStart: " + Float.toString(mY));
    }
    private void touchMove(float x, float y){
        float dx = Math.abs(x-mX);
        float dy = Math.abs(x-mY);
        if (dx >= TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY,(x+mX)/2, (y+mY)/2);
            mX = x;
            mY = y;
        }
        Log.d("DEbug TAG", "TouchMove: " + Float.toString(mY));
    }
    private void touchUp(){
        mPath.lineTo(mX, mY);
        Log.d("DEbug TAG", "touchUp: " + Float.toString(mY));
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        Log.d("Action", String.valueOf(event.getAction()));
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.d("action","down");
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("action","move");
                touchMove(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                Log.d("action","up");
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
}













