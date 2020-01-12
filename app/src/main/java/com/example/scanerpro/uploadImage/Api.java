package com.example.scanerpro.uploadImage;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {
  @Multipart
  @POST("upload-image")
  Call<FileUploadReponse> uploadFile(@Part MultipartBody.Part file);
}
