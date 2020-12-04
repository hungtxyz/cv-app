package com.txhung.cv2app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.txhung.cv2app.core.ContextImage;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class ChooseObjectActivivy extends AppCompatActivity {

    private static void onClick(View v) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_object_activivy);
    //App code
        // Define components
        ImageView imageView = findViewById(R.id.imageView);
        Button backButton = findViewById(R.id.backButton);

        // Get bitmap image
        Bitmap bmImage = ContextImage.getInstance().getBitmap();

//        // Convert bitmap to mat
//        Bitmap bmp32 = bmImage.copy(Bitmap.Config.ARGB_8888, true);
//        Mat matImage = new Mat();
//        Utils.bitmapToMat(bmp32, matImage);
//        String msg = "w="+matImage.cols() +" h="+ matImage.rows();
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//        //
//
//

//        // convert mat to bitmap
//        Utils.matToBitmap(matImage, bmImage);
        imageView.setImageBitmap(bmImage);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(ChooseObjectActivivy.this ,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}