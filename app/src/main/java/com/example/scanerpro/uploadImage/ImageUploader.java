package com.example.scanerpro.uploadImage;

import android.util.Log;


import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageUploader {

  private ImageUploadCallback imageUploadCallback;

  public void uploadImage(String filePath , String serverToken) {
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
