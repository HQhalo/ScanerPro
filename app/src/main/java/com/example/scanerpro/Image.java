package com.example.scanerpro;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Image implements Serializable {
    public Bitmap bitmap;

    public Image() {

    }
    public Image(Bitmap b) {
        this.bitmap = b;
    }
}
