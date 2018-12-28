package com.github.funler.widget_android;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import com.github.funler.jsbridge.BridgeWebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WidgetView extends BridgeWebView {

    private static String TAG = "WidgetView";
    private static WidgetView INSTANCE;

    private WidgetMode mode = WidgetMode.PRODUCTION;
    private String appId = "";
    private String userId = "";
    private String[] sections = {};

    private String sdkUrl;
    private String widgetUrl;

    private int defaultWidth = 0;
    private int defaultHeight = 0;

    public WidgetView(Context context) {
        super(context);
        this.measure();
    }

    public WidgetView(Context context, String appId, String userId, String[] sections) {
        super(context);
        this.appId = appId;
        this.userId = userId;
        this.sections = sections;
        this.measure();
    }

    public WidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.measure();
    }

    public WidgetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.measure();
    }

    public WidgetView setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public WidgetView setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public WidgetView setSections(String[] sections) {
        this.sections = sections;
        return this;
    }

    public WidgetView load() {
        return load(WidgetMode.PRODUCTION, WidgetMode.PRODUCTION.sdkURL(), WidgetMode.PRODUCTION.widgetURL());
    }

    protected WidgetView reloadWidgetView() {
        return load(this.mode, this.sdkUrl, this.widgetUrl);
    }

    protected static WidgetView getInstance() {
        return INSTANCE;
    }

    private WidgetView load(WidgetMode mode) {
        return load(mode, mode.sdkURL(), mode.widgetURL());
    }

    private WidgetView load(WidgetMode mode, String sdkUrl, String widgetUrl) {
        INSTANCE = this;

        for (JS2JavaHandlers handler : JS2JavaHandlers.values()) {
            this.registerHandler(handler.name(), handler.handler());
        }

        this.setBackgroundColor(Color.TRANSPARENT);
        this.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        this.sdkUrl = sdkUrl;
        this.widgetUrl = widgetUrl;
        this.mode = mode;
        String jsPostfix = "/static/js/bundle.js";

        String html = generateHTML(sdkUrl + jsPostfix);
        Log.d(TAG, "Load HTML:\n" + html);

        this.loadDataWithBaseURL(widgetUrl, html, "text/html", "UTF-8", null);
        return this;
    }

    private String generateHTML(String widgetUrl) {
        StringBuilder stringBuilder = new StringBuilder("");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getContext().getAssets().open("index.html")));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line
                        .replaceAll("::widgetUrl::", widgetUrl)
                        .replaceAll("::userId::", this.userId)
                        .replaceAll("::appId::", this.appId)
                        .replaceAll("::env::", this.mode.name().toLowerCase())
                        .replaceAll("::sections::", getSectionsStr());

                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    private String getSectionsStr() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String section : this.sections) {
            stringBuilder.append(section).append(",");
        }
        return stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(","));
    }

    private boolean isProduction() {
        return this.mode == WidgetMode.PRODUCTION;
    }

    private WidgetView setMode(WidgetMode mode) {
        this.mode = mode;
        load();
        return this;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }

    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    public int getDefaultHeight() {
        return defaultHeight;
    }

    public void setDefaultHeight(int defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    private void measure() {
        Log.d(TAG, "Measure initial size and store: " + this.getWidth() + "x" + this.getHeight());
        this.setDefaultWidth(this.getWidth());
        this.setDefaultHeight(this.getHeight());
    }

    protected void resize(int width, int height) {
        Log.d(TAG, "Resize to: " + width + "x" + height);
        this.getLayoutParams().width = width;
        this.getLayoutParams().height = height;
        this.requestLayout();
    }
}
