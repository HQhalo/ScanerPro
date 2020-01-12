package com.example.scanerpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.scanerpro.libraries.NativeClass;
import com.example.scanerpro.libraries.PolygonView;
import com.example.scanerpro.uitilities.FilePathUtils;
import com.example.scanerpro.uploadImage.ImageUploader;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

public class CropActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 123;
    private static final int REQUEST_IMAGE_SELECTOR = 124;
    private ImageView imageView;

    FrameLayout holderImageCrop;
    PolygonView polygonView;
    Bitmap selectedImageBitmap;
    NativeClass nativeClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        this.getSupportActionBar().hide();

        initializeElement();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_crop);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_process:
                        Bitmap bitmap = selectedImageBitmap;
                        Mat imageMat = new Mat();
                        Utils.bitmapToMat(bitmap, imageMat);
                        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
                        Imgproc.threshold(imageMat, imageMat, 0, 200,Imgproc.THRESH_OTSU);
                        Utils.matToBitmap(imageMat, bitmap);

                        imageView.setImageBitmap(bitmap);
                        break;
                    case R.id.action_crop:
                        selectedImageBitmap = getCroppedImage();
                        imageView.setImageBitmap(selectedImageBitmap);
                        polygonView.setVisibility(View.INVISIBLE);

                        break;
                    case R.id.action_space:
                        pickImage();


                        break;
                }
                return true;
            }
        });
    }

    private void initializeElement() {
        nativeClass = new NativeClass();
        holderImageCrop = findViewById(R.id.holderImageCrop);
        imageView = findViewById(R.id.imageViewCrop);
        polygonView = findViewById(R.id.polygonView);

        holderImageCrop.post(new Runnable() {
            @Override
            public void run() {
                initializeCropping();
            }
        });
    }
    private void initializeCropping() {

        selectedImageBitmap = Image.selectedImageBitmap;
        Image.selectedImageBitmap = null;

        Bitmap scaledBitmap = scaledBitmap(selectedImageBitmap, holderImageCrop.getWidth(), holderImageCrop.getHeight());
        imageView.setImageBitmap(scaledBitmap);

        Bitmap tempBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Map<Integer, PointF> pointFs =  getOutlinePoints(tempBitmap);

        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);

        int padding = (int) getResources().getDimension(R.dimen.scanPadding);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;

        polygonView.setLayoutParams(layoutParams);

    }

    public void pickImage() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_SELECTOR);
            } else {
                Intent i = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, PICK_IMAGE_REQUEST);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null &&
                data.getData() != null) {

            Uri uri = data.getData();
            String filePath = FilePathUtils.getPath(this, uri);
            ImageUploader imageUploader = new ImageUploader();

            Log.d("quang",filePath);
            imageUploader.setImageUploadCallback(new ImageUploader.ImageUploadCallback() {
                @Override
                public void onImageUploaded(String text) {
                    Toast.makeText(CropActivity.this,"text: "+text,Toast.LENGTH_LONG).show();
                }

                @Override
                public void onImageUploadFailed() {

                }
            });
            imageUploader.uploadImage(filePath,"something");
        }
    }
    protected Bitmap getCroppedImage() {

        Map<Integer, PointF> points = polygonView.getPoints();

        float xRatio = (float) selectedImageBitmap.getWidth() / imageView.getWidth();
        float yRatio = (float) selectedImageBitmap.getHeight() / imageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;

        return nativeClass.getScannedBitmap(selectedImageBitmap, x1, y1, x2, y2, x3, y3, x4, y4);

    }
    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Log.v("Quang", "scaledBitmap");
        Log.v("Quang", width + " " + height);
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Log.v("aashari-tag", "getOutlinePoints");
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }

}



