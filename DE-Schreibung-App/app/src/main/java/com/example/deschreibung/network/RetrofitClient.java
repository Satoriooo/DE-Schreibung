package com.example.deschreibung.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // IMPORTANT: This URL must point to your backend.
    // Use http://10.0.2.2:5000/ for the Android Emulator to connect to your local PC.
    // When you deploy, change this to your live URL (e.g., "https://your-app.onrender.com/").
    private static final String BASE_URL = "http://10.0.2.2:5000/";

    private static Retrofit retrofitInstance = null;
    private static ApiService apiService = null;

    private static Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            // Create a logger to see request and response details in Logcat
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create a custom OkHttpClient to add the logger
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // Build the Retrofit instance
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
}