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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

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
        return shouldInterceptRequest(view, req.getUrl().toString());
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebResourceResponse response = null;

        if (url.toLowerCase().endsWith(".chunk.js")) {
            Log.d(TAG, url);

            try {
                String[] urlParts = url.split("/");
                String fileName = urlParts[urlParts.length - 1];

                File file = new File(bridgeWebView.getContext().getFilesDir(), fileName);
                if (!file.exists()) {
                    saveTextFile(url, fileName);
                }

                Log.d(TAG, "Read file from internal storage " + fileName);
                response = generateWebResourceResponse("text/javascript", "utf-8", bridgeWebView.getContext().openFileInput(fileName));
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return response;
    }

    private void saveTextFile(String url, String fileName) throws IOException {
        Log.d(TAG, "Save file to internal storage " + fileName);
        FileOutputStream fos = bridgeWebView.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);

        URL urlObj = new URL(url);
        URLConnection urlConnection = urlObj.openConnection();
        InputStream is = urlConnection.getInputStream();

        int data;
        while ((data = is.read()) != -1) {
            fos.write(data);
        }

        is.close();
        fos.close();
    }

    private WebResourceResponse generateWebResourceResponse(String mimeType, String encoding, InputStream inputStream) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusCode = 200;
            String reasonPhase = "OK";
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("Access-Control-Allow-Origin", "*");
            return new WebResourceResponse(mimeType, encoding, statusCode, reasonPhase, responseHeaders, inputStream);
        }

        return new WebResourceResponse(mimeType, encoding, inputStream);
    }
}
