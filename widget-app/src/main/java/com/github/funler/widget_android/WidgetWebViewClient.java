package com.github.funler.widget_android;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class WidgetWebViewClient extends BridgeWebViewClient {

    private final String TAG = "WidgetWebViewClient";
    private BridgeWebView bridgeWebView;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

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

        Log.d(TAG, url);
        String urlLower = url.toLowerCase();

        if (isCacheable(urlLower)) {

            try {
                String[] urlParts = urlLower.split("/");
                String fileName = urlParts[urlParts.length - 1];
                String ext = getExt(fileName);

                File file = new File(bridgeWebView.getContext().getFilesDir(), fileName);
                if (!file.exists()) {
                    if (isImage(ext)) {
                        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
                        if (Cacheable.jpg.name().equals(ext)) {
                            compressFormat = Bitmap.CompressFormat.JPEG;
                        }

                        saveImageFile(urlLower, fileName, compressFormat);
                    } else {
                        saveTextFile(urlLower, fileName);
                    }
                } else {
                    Log.d(TAG, "Read file from internal storage " + fileName);
                    response = generateWebResourceResponse(Cacheable.valueOf(ext).mimeType(), "utf-8", bridgeWebView.getContext().openFileInput(fileName));
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return response;
    }

    private boolean isCacheable(String url) {
        return isInCacheableEnum(url) && notExcluded(url);
    }

    private boolean isInCacheableEnum(String url) {
        for (Cacheable c : Cacheable.values()) {
            if (url.endsWith(c.name())) {
                return true;
            }
        }

        return false;
    }

    private boolean notExcluded(String url) {
        return !url.contains("bee_mobile.js") &&
                !url.contains("jquery.min.js") &&
                !url.endsWith("bundle.js");
    }

    private String getExt(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private boolean isImage(String ext) {
        return Cacheable.jpg.name().equals(ext) ||
                Cacheable.png.name().equals(ext);
    }

    private void saveTextFile(String url, String fileName) {
        executorService.execute(() -> {
            try {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void saveImageFile(String url, String fileName, Bitmap.CompressFormat compressFormat) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Save file to internal storage " + fileName);
                FileOutputStream fos = bridgeWebView.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);

                URL urlObj = new URL(url);
                URLConnection urlConnection = urlObj.openConnection();
                InputStream is = urlConnection.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(is);
                image.compress(compressFormat, 100, fos);

                is.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

    private enum Cacheable {
        js("text/javascript"),
        png("image/png"),
        jpg("image/jpeg");

        private String mimeType;

        Cacheable(String mimeType) {
            this.mimeType = mimeType;
        }

        public String mimeType() { return mimeType; }
    }
}
