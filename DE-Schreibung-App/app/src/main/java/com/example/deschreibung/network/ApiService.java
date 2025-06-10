package com.example.deschreibung.network;

// ADD THESE 5 IMPORT STATEMENTS
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import com.example.deschreibung.network.ApiResponse;
import com.example.deschreibung.network.MoreExamplesResponse;

public interface ApiService {

    @POST("evaluate")
    Call<ApiResponse> evaluateText(@Body ApiRequest requestBody);

    @POST("more_examples")
    Call<MoreExamplesResponse> getMoreExamples(@Body MoreExamplesRequest requestBody);
}