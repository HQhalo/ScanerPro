package com.example.scanerpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ExportActivity extends AppCompatActivity {
    private static final int STORAGE_CODE = 1234;

    EditText editText;
    Button btSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        this.getSupportActionBar().hide();

        Intent intent = getIntent();

        String text =" " ;
        text = intent.getStringExtra("text");

        editText = findViewById(R.id.edText);

        editText.setText(text);


    }

    public void clickSave(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("noidung", editText.getText().toString());
        clipboard.setPrimaryClip(clip);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    savePdf();
                }
                else {
                    makeText(this, "Permission denied...!", LENGTH_SHORT).show();
                }
            }
        }
    }
    private void savePdf() {
        Document mDoc = new Document();
        String mFileName = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(System.currentTimeMillis());

        String mFilePath = Environment.getExternalStorageDirectory() + "/" + mFileName + ".pdf";
        Log.d("quangabc",mFileName);
        try {
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            mDoc.open();
            String mText = editText.getText().toString();

            mDoc.addAuthor("Khanh-Toan Nguyen");

            mDoc.add(new Paragraph(mText));

            mDoc.close();

            makeText(ExportActivity.this, mFileName +".pdf\nis saved to\n"+ mFilePath, LENGTH_SHORT).show();
        }
        catch (Exception e){
            makeText(ExportActivity.this, e.getMessage(), LENGTH_SHORT).show();
        }
    }
}
