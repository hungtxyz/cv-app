package com.txhung.cv2app.core;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
public class ServerConnector {

    private final String mainUrl = "http://192.168.1.9:5000/";


    private static volatile ServerConnector instance;

    private ServerConnector(){}
    public static ServerConnector getInstance(){
        if (instance == null){
            synchronized (ContextImage.class){
                if(instance == null)
                    instance = new ServerConnector();
            }
        }
        return instance;
    }

    private RequestBody buildRequestBody(String msg){
        MediaType mediaType = MediaType.parse("text/plain");
        return RequestBody.create(msg, mediaType);
    }
    public void postRequest(String message, final Activity activity){
        RequestBody requestBody = buildRequestBody(message);
        OkHttpClient  okHttpClient = new OkHttpClient();
        String url = mainUrl+"sendstr";
        Request request = new Request.Builder().post(requestBody).url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                        call.cancel();
                    }
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(activity, response.body().string(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void sendImg(byte[] maskBytesArray,byte[] originBytesArray, final Activity activity){
        String url = mainUrl + "sendimg";
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("mask","mask.jpg", RequestBody.create(MediaType.parse("img/jpg"), maskBytesArray))
                .addFormDataPart("origin","origin.jpg", RequestBody.create(MediaType.parse("img/jpg"), originBytesArray))
                .build();
        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                        call.cancel();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(activity, response.body().string(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        call.cancel();
                    }
                });
            }
        });
    }
}


































