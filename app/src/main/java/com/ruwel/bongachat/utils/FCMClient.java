package com.ruwel.bongachat.utils;

import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

public class FCMClient {
    private static Retrofit retrofit = null;

    public static FCMApi getClient() {
        if(retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(Constants.FCM_SEND_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    return retrofit.create(FCMApi.class);
    }
}
