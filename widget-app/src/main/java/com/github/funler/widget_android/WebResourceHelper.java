package com.github.funler.widget_android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceResponse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class WebResourceHelper {

    private static final String TAG = "WebResourceHelper";
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);

    public static boolean isCacheable(String url) {
        return isInCacheableEnum(url) && notExcluded(url);
    }

    private static boolean isInCacheableEnum(String url) {
        for (Cacheable c : Cacheable.values()) {
            if (url.endsWith(c.name())) {
                return true;
            }
        }

        return false;
    }

    private static boolean notExcluded(String url) {
        return !url.contains("bee_mobile.js") &&
                !url.contains("jquery.min.js") &&
                !url.endsWith("bundle.js");
    }

    public static String getFileName(String url) {
        return url.replace("http://", "")
                .replace("https://", "")
                .replaceAll("/", ".");
    }

    public static String getExt(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public static boolean isImage(String ext) {
        return Cacheable.jpg.name().equals(ext) ||
                Cacheable.png.name().equals(ext);
    }

    public static boolean isJs(String ext) {
        return Cacheable.js.name().equals(ext);
    }

    public static void saveTextFile(String url, String fileName, FileOutputStream fos) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Save file to internal storage " + fileName);

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

    public static void saveImageFile(String url, String fileName, FileOutputStream fos) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Save file to internal storage " + fileName);

                URL urlObj = new URL(url);
                URLConnection urlConnection = urlObj.openConnection();
                InputStream is = urlConnection.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(is);

                Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
                if (getExt(fileName).equals(Cacheable.jpg.name())) {
                    compressFormat = Bitmap.CompressFormat.JPEG;
                }

                image.compress(compressFormat, 100, fos);

                is.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public static WebResourceResponse generateWebResourceResponse(String ext, String encoding, InputStream inputStream) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusCode = 200;
            String reasonPhase = "OK";
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("Access-Control-Allow-Origin", "*");
            return new WebResourceResponse(Cacheable.valueOf(ext).mimeType(), encoding, statusCode, reasonPhase, responseHeaders, inputStream);
        }

        return new WebResourceResponse(Cacheable.valueOf(ext).mimeType(), encoding, inputStream);
    }

    private enum Cacheable {
        js("text/javascript"),
        png("image/png"),
        jpg("image/jpeg"),
        css("text/css"),
        svg("image/svg+xml");

        private String mimeType;

        Cacheable(String mimeType) {
            this.mimeType = mimeType;
        }

        public String mimeType() { return mimeType; }
    }
}
