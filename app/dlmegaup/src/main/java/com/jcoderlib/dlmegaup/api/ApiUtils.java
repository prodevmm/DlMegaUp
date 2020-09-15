package com.jcoderlib.dlmegaup.api;

import retrofit2.Retrofit;

public class ApiUtils {
    public static ApiRequest getRequest(){
        return create().create(ApiRequest.class);
    }

    private static Retrofit create(){
        return new Retrofit.Builder().baseUrl("http://localhost/").build();
    }
}
