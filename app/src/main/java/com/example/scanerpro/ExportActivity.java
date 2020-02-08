package com.example.scanerpro;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ExportActivity extends AppCompatActivity {
    private static final int STORAGE_CODE_PDF = 1234;
    private static final int STORAGE_CODE_TEXT = 1235;

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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE_PDF:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    savePdf();
                }
                else {
                    makeText(this, "Permission denied...!", LENGTH_SHORT).show();
                }
            }
            case STORAGE_CODE_TEXT:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    saveText();
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
    private void saveText(){
        try {
            String h = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();

            File root = new File(Environment.getExternalStorageDirectory(), "Notes");

            if (!root.exists()) {
                root.mkdirs();
            }
            File filepath = new File(root, h + ".txt");
            FileWriter writer = new FileWriter(filepath);
            writer.append(editText.getText().toString());
            writer.flush();
            writer.close();
            String m = "File generated with name " + h + ".txt";
            String mFileName = root + "/" + h + ".txt";
            Toast.makeText(ExportActivity.this, h + ".txt\nis saved to\n" + mFileName, LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(ExportActivity.this, e.getMessage(), LENGTH_SHORT).show();
        }
    }

    public void clickSavePDFBTN(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ExportActivity.this);
        dialog.setCancelable(true);
        dialog.setTitle("PDF");
        dialog.setMessage("Save to internal storage or Upload to Google Driver");
        dialog.setNegativeButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED){
                        String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                        requestPermissions(permissions, STORAGE_CODE_PDF);
                    }
                    else {
                        savePdf();
                    }
                }
                else {
                    savePdf();
                }
            }
        });
        dialog.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //up file
            }
        });
        AlertDialog dialog1 = dialog.create();
        dialog1.show();


    }

    public void clickSaveTextBtn(View view) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                requestPermissions(permissions, STORAGE_CODE_TEXT);
            }
            else {
                saveText();
            }
        }
        else {
            saveText();
        }
    }

    public void clickCopyBtn(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("noidung", editText.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(ExportActivity.this, "Successfully copy to clipboard", LENGTH_SHORT).show();
    }
}
