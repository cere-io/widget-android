package com.github.funler.widget_android;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.github.funler.jsbridge.BridgeWebView;
import com.github.funler.jsbridge.BridgeWebViewClient;

import java.io.File;
import java.io.IOException;

class WidgetWebViewClient extends BridgeWebViewClient {

    private final String TAG = "WidgetWebViewClient";
    private BridgeWebView bridgeWebView;

    public WidgetWebViewClient(BridgeWebView webView) {
        super(webView);
        this.bridgeWebView = webView;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
        if (req.getMethod().equals("GET")) {
            return shouldInterceptRequest(view, req.getUrl().toString());
        }

        return null;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebResourceResponse response = null;

        String urlLower = url.toLowerCase();

        if (WebResourceHelper.isCacheable(urlLower)) {
            Log.d(TAG, urlLower);
            try {
                String fileName =  WebResourceHelper.getFileName(urlLower);
                String ext = getExt(fileName);

                File file = new File(bridgeWebView.getContext().getFilesDir(), fileName + ".loaded");
                if (!file.exists()) {
                    if (WebResourceHelper.isImage(ext)) {
                        WebResourceHelper.saveImageFile(urlLower, fileName, bridgeWebView.getContext());
                    } else {
                        WebResourceHelper.saveTextFile(urlLower, fileName, bridgeWebView.getContext());
                    }
                } else {
                    Log.d(TAG, "Read file from internal storage " + fileName);
                    response = WebResourceHelper.generateWebResourceResponse(ext, "utf-8", bridgeWebView.getContext().openFileInput(fileName));
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return response;
    }

    private String getExt(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
