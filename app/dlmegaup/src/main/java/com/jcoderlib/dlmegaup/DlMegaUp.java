package com.jcoderlib.dlmegaup;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jcoderlib.dlmegaup.api.ApiRequest;
import com.jcoderlib.dlmegaup.api.ApiUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class DlMegaUp {

    private Context context;

    public DlMegaUp(Context context){
        this.context = context;
    }

    private static final String ERR_MSG_NULL_RESPONSE = "Server returns null response";
    private static final String ERR_MSG_NO_DL_URL = "No download url found";

    private String getStr(ResponseBody responseBody) {
        try {
            return responseBody.string();
        } catch (IOException e) {
            return ERR_MSG_NULL_RESPONSE;
        }
    }

    public void enqueue(String url, Callback callback) {
        ApiRequest request = ApiUtils.getRequest();
        request.getRawResponse(url).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) callback.onFailure(ERR_MSG_NULL_RESPONSE);
                else handleResponse(callback, responseBody);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });

    }

    private void handleResponse(Callback callback, ResponseBody responseBody) {
        String response = getStr(responseBody);
        String[] splits = response.split("<script>", 2);

        String script = splits[1];
        String regex = "href='(.*?)'";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(script);
        String dlUrl = null;

        if (matcher.find()) dlUrl = matcher.group(1);
        if (dlUrl == null) callback.onFailure(ERR_MSG_NO_DL_URL);
        else handleDlUrl(callback, dlUrl);
    }

    private void handleDlUrl(Callback callback, String dlUrl) {
        LimitProcess limitProcess = new LimitProcess();
        limitProcess.startProcess(context, dlUrl, new LimitProcess.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String url, String cookie) {
                callback.onSuccess(url, cookie);
            }

            @Override
            public void onTaskFailed(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public interface Callback {
        void onSuccess(String dlUrl, String cookie);

        void onFailure(String errorMessage);
    }
}
