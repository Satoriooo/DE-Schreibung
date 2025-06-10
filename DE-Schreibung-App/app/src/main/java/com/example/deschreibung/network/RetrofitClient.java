package com.example.deschreibung.network;

import java.util.concurrent.TimeUnit; // <-- ADD THIS IMPORT

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Make sure this is your live Render URL
    private static final String BASE_URL = "https://de-schreibung.onrender.com"; // Example URL, use yours

    private static Retrofit retrofitInstance = null;
    private static ApiService apiService = null;

    private static Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // --- NEW: Create an OkHttpClient with custom timeouts ---
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(60, TimeUnit.SECONDS) // Set connection timeout
                    .readTimeout(60, TimeUnit.SECONDS)    // Set read timeout
                    .writeTimeout(60, TimeUnit.SECONDS)   // Set write timeout
                    .build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // <-- Use the new client with longer timeouts
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