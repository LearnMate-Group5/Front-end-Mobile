package com.example.learnmate_frontend.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Context;
import com.example.learnmate_frontend.util.SharedPrefManager;

public class AuthInterceptor implements Interceptor  {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = SharedPrefManager.getInstance(context).getToken();

        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
