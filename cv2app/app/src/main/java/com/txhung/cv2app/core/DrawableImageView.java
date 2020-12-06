package com.txhung.cv2app.core;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import android.view.View.*;
import android.view.WindowManager;

public class DrawableImageView extends AppCompatImageView  implements OnTouchListener
{
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;

    Canvas canvas;
    Canvas canvasMask;
    Paint paint;
    Matrix matrix;

    public DrawableImageView(Context context)
    {
        super(context);
        setOnTouchListener(this);
    }

    public DrawableImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public DrawableImageView(Context context, AttributeSet attrs,
                             int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
    }

    public void setNewImage(Bitmap alteredBitmap, Bitmap bmp)
    {
        Bitmap resultBitmap = Bitmap.createBitmap(alteredBitmap);
        canvas = new Canvas(alteredBitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);
        matrix = new Matrix();
//        canvas.drawBitmap(bmp, matrix, paint);
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity) getContext()).getWindowManager()
//                .getDefaultDisplay()
//                .getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels;
//        int width = displayMetrics.widthPixels;
//        float r = bmp.getWidth()/width;
//        Bitmap backgroundBitmap = Bitmap.createScaledBitmap(bmp,width,Math.round(bmp.getHeight()/r),true);

        Drawable d = new BitmapDrawable(getResources(), bmp);
        setBackgroundDrawable(d);

        setImageBitmap(alteredBitmap);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        int action = event.getAction();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                downx = getPointerCoords(event)[0];
                downy = getPointerCoords(event)[1];
                break;
            case MotionEvent.ACTION_MOVE:
                upx = getPointerCoords(event)[0];
                upy = getPointerCoords(event)[1];
                canvas.drawLine(downx, downy, upx, upy, paint);
                invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
                upx = getPointerCoords(event)[0];
                upy = getPointerCoords(event)[1];
                canvas.drawLine(downx, downy, upx, upy, paint);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    final float[] getPointerCoords(MotionEvent e)
    {
        final int index = e.getActionIndex();
        final float[] coords = new float[] { e.getX(index), e.getY(index) };
        Matrix matrix = new Matrix();
        getImageMatrix().invert(matrix);
        matrix.postTranslate(getScrollX(), getScrollY());
        matrix.mapPoints(coords);
        return coords;
    }
}