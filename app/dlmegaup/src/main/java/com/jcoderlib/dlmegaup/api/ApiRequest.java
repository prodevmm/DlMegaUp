package com.jcoderlib.dlmegaup.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiRequest {

    @GET
    Call<ResponseBody> getRawResponse(@Url String url);
}
