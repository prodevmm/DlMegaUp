package com.jcoderlib.dlmegaup;

import android.content.Context;

public class DlMegaUp {

    private Context context;

    public DlMegaUp(Context context){
        this.context = context;
    }


    public void enqueue(String url, Callback callback) {
        LimitProcess limitProcess = new LimitProcess();
        limitProcess.startProcess(context, url, new LimitProcess.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String url, String cookies) {
                callback.onSuccess(url, cookies);
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
