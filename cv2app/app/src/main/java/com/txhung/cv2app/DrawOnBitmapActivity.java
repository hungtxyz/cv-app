package com.txhung.cv2app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.widget.Toast;


import com.txhung.cv2app.core.ContextImage;
import com.txhung.cv2app.core.DrawableImageView;
import com.txhung.cv2app.core.ServerConnector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Date;

public class DrawOnBitmapActivity extends Activity
{
    DrawableImageView choosenImageView;
    Button savePicture;

    Bitmap bmp;
    Bitmap alteredBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_on_bitmap);



        choosenImageView = (DrawableImageView) this.findViewById(R.id.ChoosenImageView);
        savePicture = (Button) this.findViewById(R.id.SavePictureButton);

        bmp = ContextImage.getInstance().getBitmap();

        alteredBitmap = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), bmp.getConfig());
        choosenImageView.setNewImage(alteredBitmap, bmp);



        savePicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (alteredBitmap != null)
                {
                    try {

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        alteredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] maskByteArray = stream.toByteArray();

                        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
                        byte[] originByteArray = stream1.toByteArray();

                        ServerConnector connector = ServerConnector.getInstance();
                        connector.sendImg(maskByteArray,originByteArray, DrawOnBitmapActivity.this);

                    } catch (Exception e) {
                        Log.v("EXCEPTION", e.getMessage());
                    }
                }
            }
        });

    }

}