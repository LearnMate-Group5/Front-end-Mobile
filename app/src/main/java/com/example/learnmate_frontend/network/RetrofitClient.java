package com.example.learnmate_frontend.network;
import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitClient {
    private static ApiService instance;

    public static ApiService getInstance(Context context) {
        if (instance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://phygen.ticketresell-swp.click/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            instance = retrofit.create(ApiService.class);
        }
        return instance;
    }
}
