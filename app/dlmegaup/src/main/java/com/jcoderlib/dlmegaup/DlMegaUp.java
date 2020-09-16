package com.jcoderlib.dlmegaup;

import android.content.Context;

public class DlMegaUp {

    private Context context;

    public DlMegaUp(Context context){
        this.context = context;
    }

    public DlMegaUp setForceTimeout(int miliseconds){
        LimitProcess.TERMINATE_TIME = miliseconds;
        return this;
    }

    public DlMegaUp setForceBoost(){
        LimitProcess.speedBoost = true;
        return this;
    }

    public DlMegaUp setForceBoost(int executeTimeMiliseconds){
        LimitProcess.speedBoost = true;
        LimitProcess.speedBoostJsExecutionTime = executeTimeMiliseconds;
        return this;
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
