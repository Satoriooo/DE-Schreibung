package com.example.deschreibung.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("evaluate")
    Call<ApiResponse> evaluateText(@Body ApiRequest requestBody);

}