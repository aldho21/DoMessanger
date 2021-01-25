package com.example.domessanger.fragments;

import com.example.domessanger.Notif.MyResponse;
import com.example.domessanger.Notif.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HEAD;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=\tAAAA52I6L9A:APA91bG4auOjzsCdKF8zUOEpBlfemMVFXk5xteEqO7MAKsEtOBfKoxoTQd-q-3ZTOgCLEF5zonNAnXPzA_gED752p5OqxQ9MxhWtoBysnUSMlQWSp21YTQP7OO3VKk32K9KGcNtayIJ8"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
