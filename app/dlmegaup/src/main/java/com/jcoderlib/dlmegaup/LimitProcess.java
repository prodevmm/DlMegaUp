package com.jcoderlib.dlmegaup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class LimitProcess {
    private WebView webView;
    private OnTaskCompleted callback;
    private Handler handler;
    private boolean firstLoad = true;

    static int TERMINATE_TIME = 10000;
    private static final String ERR_MSG_TIMEOUT = "connection timeout";

    @SuppressLint({"SetJavaScriptEnabled"})
    public void startProcess(Context context, String url, final OnTaskCompleted callback) {
        this.callback = callback;



        webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (firstLoad) webView.loadUrl(url);
                firstLoad = false;
            }
        });

        webView.setDownloadListener(listener);

        webView.setLayoutParams(new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        webView.loadUrl(url);
        handler = new Handler();
        handler.postDelayed(processTerminator, TERMINATE_TIME);
    }

    private Runnable processTerminator = new Runnable() {
        @Override
        public void run() {
            webView.removeJavascriptInterface("JusticeCoder");
            destroyWebView();
            callback.onTaskFailed(ERR_MSG_TIMEOUT);
        }
    };

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent,
                                    String contentDisposition, String mimeType,
                                    long contentLength) {
            String cookies = CookieManager.getInstance().getCookie(url);

            if (handler != null) handler.removeCallbacks(processTerminator);
            callback.onTaskCompleted(url, cookies);

            destroyWebView();
        }
    };

    private void destroyWebView() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(String url, String cookie);

        void onTaskFailed(String errorMessage);
    }
}
