package com.example.scanerpro;

import android.Manifest;
import android.app.ProgressDialog;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.scanerpro.helpers.driveService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ExportActivity extends AppCompatActivity {
    private static final int STORAGE_CODE_TEXT = 1235;
    private static final int SIGN_IN_CODE = 400;
    private static final int STORAGE_CODE_PDF_DRIVE = 1111;
    private static final int STORAGE_CODE_PDF_NO_DRIVE = 1112;

    EditText editText;
    Button btSave;

    driveService driveServiceHelper;
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
            case STORAGE_CODE_PDF_NO_DRIVE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    String mFilePath = savePdf();
                    if (mFilePath!=null)
                        makeText(ExportActivity.this, mFilePath +".pdf\nis saved!", LENGTH_SHORT).show();
                }
                else {
                    makeText(this, "Permission denied...!", LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_CODE_PDF_DRIVE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    String mFilePath = savePdf();
                    if (mFilePath == null) return;
                    requestSignin();
                    uploadPDFfile(mFilePath);
                }
                else {
                    makeText(this, "Permission denied...!", LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_CODE_TEXT:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    saveText();
                }
                else {
                    makeText(this, "Permission denied...!", LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    private String savePdf() {
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

            return mFilePath;
        }
        catch (Exception e){
            makeText(ExportActivity.this, e.getMessage(), LENGTH_SHORT).show();
            return null;
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
                        requestPermissions(permissions, STORAGE_CODE_PDF_NO_DRIVE);
                    }
                    else {
                        String mFilePath = savePdf();
                        if (mFilePath!=null)
                            makeText(ExportActivity.this, mFilePath +".pdf\nis saved!", LENGTH_SHORT).show();
                    }
                }
                else {
                    String mFilePath = savePdf();
                    if (mFilePath!=null)
                        makeText(ExportActivity.this, mFilePath +".pdf\nis saved!", LENGTH_SHORT).show();
                }
            }
        });
        dialog.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mFilePath = null;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED){
                        String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                        requestPermissions(permissions, STORAGE_CODE_PDF_DRIVE);
                    }
                    else {
                        mFilePath = savePdf();
                    }
                }
                else {
                    mFilePath = savePdf();
                }
                if (mFilePath == null) return;
                requestSignin();
                uploadPDFfile(mFilePath);
            }
        });
        AlertDialog dialog1 = dialog.create();
        dialog1.show();
    }
    private void requestSignin() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(ExportActivity.this, signInOptions);

        startActivityForResult(client.getSignInIntent(),SIGN_IN_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case SIGN_IN_CODE:
                if (resultCode == RESULT_OK){
                    handleSigninIntent(data);
                }
                break;

        }
    }

    private void handleSigninIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential
                                .usingOAuth2(ExportActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                        credential.setSelectedAccount(googleSignInAccount.getAccount());

                        Drive googleDriveServices = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("ScannerPro")
                                .build();

                        driveServiceHelper = new driveService(googleDriveServices);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
    public void uploadPDFfile(String filePath) {
        ProgressDialog progressDialog = new ProgressDialog(ExportActivity.this);
        progressDialog.setTitle("Uploading to Google Drive");
        Log.d("UPLOAD","Uploading to Google Drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        driveServiceHelper.createFilePDF(filePath).addOnSuccessListener(
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        progressDialog.dismiss();
                        Toast.makeText(ExportActivity.this, "Uploaded Successfully",LENGTH_SHORT).show();
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ExportActivity.this,"Check your google drive api key", LENGTH_SHORT).show();
            }
        });

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
