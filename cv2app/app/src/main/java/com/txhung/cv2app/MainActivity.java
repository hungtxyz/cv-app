package com.txhung.cv2app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.txhung.cv2app.core.ContextImage;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java4");
    }

    Button openButton, cameraButton;
    ImageView imageView;
    Uri imageUri;
    ContentValues values;
    Bitmap bmImage = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  App code
        openButton = findViewById(R.id.openButton);
        cameraButton = findViewById(R.id.cameraButton);
        imageView = findViewById(R.id.imageView);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        // Accept img and start Choose object activity
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dispatchTakePictureIntent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void openImage(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, REQUEST_IMAGE_GALLERY);
    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void dispatchTakePictureIntent() throws IOException {

        values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_IMAGE_GALLERY && resultCode==RESULT_OK) {
            assert data != null;
            imageUri = data.getData();
            imageView.setImageURI(imageUri);

            try {
                bmImage =MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);;
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert bmImage != null;
            ContextImage.getInstance().setBitmap(bmImage);
            Mat mat = new Mat(bmImage.getWidth(), bmImage.getHeight(), CvType.CV_8UC4);
            if (mat.rows()==0) Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
            else {
                String msg = "w="+mat.cols() +" h="+ mat.rows();
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DrawOnBitmapActivity.class);
                startActivity(intent);
            }


        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                 bmImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                 Log.d("Log Debug", Integer.toString(bmImage.getHeight()));
                ContextImage.getInstance().setBitmap(bmImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d("IMG WIDTH: ", Integer.toString(bmImage.getWidth()));
            Intent intent = new Intent(MainActivity.this, DrawOnBitmapActivity.class);
            startActivity(intent);
        }

    }
}