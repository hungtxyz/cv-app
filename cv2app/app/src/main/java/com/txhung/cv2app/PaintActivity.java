package com.txhung.cv2app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.txhung.cv2app.core.ContextImage;
import com.txhung.cv2app.core.PainView;

public class PaintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_paint);

        PainView painView = findViewById(R.id.paintView);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Bitmap bitmap = ContextImage.getInstance().getBitmap();
        painView.init(displayMetrics, bitmap);

    }

}