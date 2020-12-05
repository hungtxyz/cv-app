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

import java.io.OutputStream;

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
        Log.d("debug#####################", bmp.getConfig().toString());
        choosenImageView.setNewImage(alteredBitmap, bmp);



        savePicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (alteredBitmap != null)
                {
                    ContentValues contentValues = new ContentValues(3);
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "Draw On Me");

                    Uri imageFileUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    try {
                        OutputStream imageFileOS = getContentResolver()
                                .openOutputStream(imageFileUri);
                        alteredBitmap
                                .compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS);
                        Toast t = Toast
                                .makeText(DrawOnBitmapActivity.this, "Saved!", Toast.LENGTH_SHORT);
                        t.show();

                    } catch (Exception e) {
                        Log.v("EXCEPTION", e.getMessage());
                    }
                }
            }
        });

    }

}