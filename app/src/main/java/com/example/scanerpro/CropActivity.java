package com.example.scanerpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.scanerpro.libraries.NativeClass;
import com.example.scanerpro.libraries.PolygonView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class CropActivity extends AppCompatActivity {

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

                        break;
                    case R.id.action_crop:
                        imageView.setImageBitmap(getCroppedImage());
                        polygonView.setVisibility(View.INVISIBLE);

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



