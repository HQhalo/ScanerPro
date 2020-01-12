package com.example.scanerpro.uploadImage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

;

public class RetrofitApiClient {
  private static Retrofit retrofit = null;

  public static Retrofit getClient(final String token) {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient client = new OkHttpClient.Builder()
     .addInterceptor(new Interceptor() {
       @Override
       public Response intercept(Chain chain) throws IOException {
         Request request = chain.request()
          .newBuilder()
          .addHeader("Authorization", "Token " + token)
          .build();
         return chain.proceed(request);
       }
     })
     .addInterceptor(logging)
     .build();

    retrofit = new Retrofit.Builder()
     .baseUrl("http://10.0.2.2:3000")
     .addConverterFactory(GsonConverterFactory.create())
     .client(client)
     .build();
    return retrofit;
  }
}