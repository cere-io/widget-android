package com.github.funler.widget_android;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import com.github.funler.jsbridge.BridgeWebView;
import com.github.funler.jsbridge.CallBackFunction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WidgetView extends BridgeWebView {

    private static String TAG = "WidgetView";
    private static WidgetView INSTANCE;

    private WidgetEnv env = WidgetEnv.PRODUCTION;
    private WidgetMode mode = WidgetMode.REWARDS;
    private String appId = "";
    private String userId = "";
    private String[] sections = {};

    private String sdkUrl;
    private String widgetUrl;

    private int defaultWidth = 0;
    private int defaultHeight = 0;

    public WidgetView(Context context) {
        super(context);
    }

    public WidgetView(Context context, String appId, String userId, String[] sections) {
        super(context);
        this.appId = appId;
        this.userId = userId;
        this.sections = sections;
    }

    public WidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    public WidgetView setMode(WidgetMode mode) {
        this.mode = mode;
        return this;
    }

    public WidgetView onSignUp(OnSignUpHandler handler) {
        this.registerHandler("onSignUp", (Context context, String data, CallBackFunction function) -> {
            try {
                JSONObject jsonObject = new JSONObject(data);
                handler.handle(
                        jsonObject.getString("email"),
                        jsonObject.getString("token"),
                        prepareExtras(jsonObject)
                );
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                function.onCallBack(null);
            }
        });
        return this;
    }

    public WidgetView onSignIn(OnSignInHandler handler) {
        this.registerHandler("onSignIn", (Context context, String data, CallBackFunction function) -> {
            try {
                JSONObject jsonObject = new JSONObject(data);
                handler.handle(
                        jsonObject.getString("email"),
                        jsonObject.getString("token"),
                        prepareExtras(jsonObject)
                );
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                function.onCallBack(null);
            }
        });
        return this;
    }

    private Map<String, String> prepareExtras(JSONObject jsonObject) throws JSONException {
        Map<String, String> extras = Collections.EMPTY_MAP;

        if (jsonObject.has("extras")) {
            JSONObject extrasJson = jsonObject.getJSONObject("extras");
            extras = new HashMap<>();

            Iterator<String> iterator = extrasJson.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                extras.put(key, extrasJson.getString(key));
            }
        }

        return extras;
    }

    public WidgetView onGetUserEmailById() {
        return this;
    }

    public WidgetView show() {
        return this;
    }

    public WidgetView hide() {
        return this;
    }

    public WidgetView collapse() {
        return this;
    }

    public WidgetView expand() {
        return this;
    }

    public WidgetView load() {
        return load(WidgetEnv.PRODUCTION, WidgetEnv.PRODUCTION.sdkURL(), WidgetEnv.PRODUCTION.widgetURL());
    }

    protected WidgetView reloadWidgetView() {
        return load(this.env, this.sdkUrl, this.widgetUrl);
    }

    protected static WidgetView getInstance() {
        return INSTANCE;
    }

    private WidgetView load(WidgetEnv env) {
        return load(env, env.sdkURL(), env.widgetURL());
    }

    private WidgetView load(WidgetEnv env, String sdkUrl, String widgetUrl) {
        INSTANCE = this;

        for (JS2JavaHandlers handler : JS2JavaHandlers.values()) {
            this.registerHandler(handler.name(), handler.handler());
        }

        this.setBackgroundColor(Color.TRANSPARENT);
        this.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        this.sdkUrl = sdkUrl;
        this.widgetUrl = widgetUrl;
        this.env = env;
        String jsPostfix = "/static/js/bundle.js";

        String html = generateHTML(sdkUrl + jsPostfix);
        Log.d(TAG, "Load HTML:\n" + html);

        this.loadDataWithBaseURL(widgetUrl, html, "text/html", "UTF-8", null);
        this.measure();

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
                        .replaceAll("::env::", this.env.name().toLowerCase())
                        .replaceAll("::sections::", getSectionsStr())
                        .replaceAll("::mode::", this.mode.name().toLowerCase());

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
        return this.env == WidgetEnv.PRODUCTION;
    }

    private WidgetView setEnv(WidgetEnv env) {
        this.env = env;
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

    public interface OnSignInHandler {
        void handle(String email, String token, Map<String, String> extras);
    }

    public interface OnSignUpHandler {
        void handle(String email, String token, Map<String, String> extras);
    }
}
