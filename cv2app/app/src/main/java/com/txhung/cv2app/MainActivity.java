package com.txhung.cv2app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.txhung.cv2app.core.ContextImage;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java4");
    }

    Button openButton, acceptButton;
    ImageView imageView;
    Uri imageUri;
    Mat image = null;
    Bitmap bmImage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  App code
        openButton = findViewById(R.id.openButton);
        acceptButton = findViewById(R.id.acceptButton);
        imageView = findViewById(R.id.imageView);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        // Accept img and start Choose object activity
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bmImage==null)
                    Toast.makeText(MainActivity.this,"Image is null", Toast.LENGTH_SHORT).show();
                else{
//                    showFilter2D(image);
                    // start ChooseObjectActivity
                    Intent intent = new Intent(MainActivity.this, ChooseObjectActivivy.class);
                    startActivity(intent);
                }
            }
        });
    }
    private void openImage(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 0);
    }
    // Never use
    private void showFilter2D(Mat mat){
        Mat desMat = new Mat( mat.cols(),mat.rows(),CvType.CV_8UC4);
        Imgproc.Laplacian( mat, desMat, -1);
        Imgproc.cvtColor(desMat, desMat,Imgproc.COLOR_BGR2GRAY);
       // Mat temMat = new Mat( mat.cols(),desMat.rows(),CvType.CV_8UC4 );
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(),desMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, bitmap);
        imageView.setImageBitmap(bitmap);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==0 && resultCode==RESULT_OK) {
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
            }


        }
    }
}