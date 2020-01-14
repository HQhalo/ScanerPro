package com.example.scanerpro.uploadImage;

import android.app.ProgressDialog;
import android.util.Log;


import org.xml.sax.Parser;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageUploader {

  private ImageUploadCallback imageUploadCallback;
  ProgressDialog dialog;
  int progress=12;

  public ImageUploader(ProgressDialog dialog)
  {
      this.dialog=dialog;
  }
  public void uploadImage(String filePath , String serverToken) {
      dialog.setProgress(progress);
    File file = new File(filePath);
    final RequestBody requestFile = RequestBody.create(MediaType.parse("File/*"), file);
    MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

      Log.d("quag","helo1");
      RetrofitApiClient.getClient(serverToken)
     .create(Api.class)
     .uploadFile(body)
     .enqueue(new Callback<FileUploadReponse>() {
       @Override

       public void onResponse(Call<FileUploadReponse> call, Response<FileUploadReponse> response) {
           Log.d("quag",response.body().getText());
           Double x=Math.random()*3+0;
           progress+= x.intValue();
           if (progress<90)
               dialog.setProgress(progress);
           if (response.isSuccessful()) {

           if (imageUploadCallback != null) {
               imageUploadCallback.onImageUploaded(response.body().getText());
           }
         } else {
           if (imageUploadCallback != null) {
             imageUploadCallback.onImageUploadFailed();
           }
         }
       }

       @Override
       public void onFailure(Call<FileUploadReponse> call, Throwable t) {
           Log.d("quag",t.toString());
         if (imageUploadCallback != null) {
           imageUploadCallback.onImageUploadFailed();
         }
       }
     });
  }

  public void setImageUploadCallback(ImageUploadCallback imageUploadCallback) {
    this.imageUploadCallback = imageUploadCallback;
  }

  public interface ImageUploadCallback {
    void onImageUploaded(String text);

    void onImageUploadFailed();
  }
}
