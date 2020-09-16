package com.jcoderlib.dlmegaup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class LimitProcess {
    private WebView webView;
    private OnTaskCompleted callback;
    private boolean downloadEventFired = false;
    private Handler handler;
    static int TERMINATE_TIME = 20000;
    static boolean speedBoost;
    static int speedBoostJsExecutionTime = 5000;

    private boolean clickUrlMethodInvoked;
    private static final String ERR_MSG_TIMEOUT = "api execution timeout";

    @SuppressLint({"SetJavaScriptEnabled"})
    public void startProcess(Context context, String url, final OnTaskCompleted callback) {

        this.callback = callback;
        webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (speedBoost && !clickUrlMethodInvoked && !downloadEventFired)
                    new Handler().postDelayed(() -> clickDlUrl(), speedBoostJsExecutionTime);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!speedBoost && !clickUrlMethodInvoked && !downloadEventFired) clickDlUrl();
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(params);

        webView.setDownloadListener(listener);
        webView.loadUrl(url);

        handler = new Handler();
        handler.postDelayed(processTerminator, TERMINATE_TIME);
    }

    private Runnable processTerminator = new Runnable() {
        @Override
        public void run() {
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
            if (!downloadEventFired) {
                downloadEventFired = true;
                if (handler != null) handler.removeCallbacks(processTerminator);
                callback.onTaskCompleted(url, cookies);
            }
            destroyWebView();
        }
    };

    private String decodeBase64(String coded) {
        return new String(Base64.decode(coded, Base64.DEFAULT));
    }

    private void clickDlUrl() {
        clickUrlMethodInvoked = true;
        if (webView != null) {
            String js = "javascript: (function() {" + decodeBase64(getJavascript()) + "})()";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(js, null);
            } else {
                webView.loadUrl(js);
            }
        }
    }

    private String getJavascript() {
        return "c2Vjb25kcyA9IDA7DWRpc3BsYXkoKTsNbGV0IGRsRWxlbWVudCA9IGRvY3VtZW50LmdldEVsZW1lbnRzQnlDbGFzc05hbWUoImJ0biBidG4tZGVmYXVsdCIpWzBdOw1kbEVsZW1lbnQuY2xpY2soKTs=";
    }

    private void destroyWebView() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(String url, String cookies);

        void onTaskFailed(String errorMessage);
    }
}
