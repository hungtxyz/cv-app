package com.txhung.cv2app.core;

import android.graphics.Bitmap;

public class ContextImage {
    private static volatile ContextImage instance;
    private static Bitmap mBitmap;
    private ContextImage(){}
    public static ContextImage getInstance(){
        if (instance == null){
            synchronized (ContextImage.class){
                if(instance == null)
                    instance = new ContextImage();
            }
        }
    return instance;
    }
    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        synchronized (this) {
            if (mBitmap != null) {
                mBitmap.recycle();
            }
        }
        mBitmap = Bitmap.createBitmap(bitmap);
    }

}
