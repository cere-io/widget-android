package com.github.funler.widget_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.github.funler.jsbridge.BridgeWebView;
import com.github.funler.jsbridge.CallBackFunction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WidgetView {

    private static String TAG = "WidgetView";
    private static WidgetView INSTANCE;

    private WidgetEnv env = WidgetEnv.PRODUCTION;
    private WidgetMode mode = WidgetMode.REWARDS;

    private String appId = "";
    private String userId = "";
    private List<String> sections = Collections.EMPTY_LIST;

    private boolean initialized = false;
    private List<Java2JSHandler> java2JSHandlers = new ArrayList<>();

    private Context context;
    private BridgeWebView bridgeWebView;
    private int restoreWidth = 0;
    private int restoreHeight = 0;
    private boolean isMaximized = false;

    private OnSignInHandler onSignInHandler = null;
    private OnSignUpHandler onSignUpHandler = null;
    private OnProcessNonFungibleRewardHandler onProcessNonFungibleRewardHandler = null;
    private OnGetClaimedRewardsHandler onGetClaimedRewardsHandler = null;
    private OnGetUserByEmailHandler onGetUserByEmailHandler = null;
    private OnHideHandler onHideHandler = null;

    public WidgetView(Context context) {
        this.context = context;
        configureWebView();
        INSTANCE = this;
    }

    public Context getContext() { return this.context; }

    public WidgetView init(String appId, String userId, List<String> sections) {
        return init(appId, userId, sections, WidgetEnv.PRODUCTION);
    }

    private WidgetView init(String appId, String userId, List<String> sections, WidgetEnv env) {
        this.appId = appId;
        this.userId = userId;
        this.sections = sections;
        this.env = env;
        load();

        return this;
    }

    public void logout() {
        reload();
    }

    public WidgetView sendDataToField(String fieldName, String value) {
        putOrProcessHandler(() -> callWidgetJavascript("sendToField", "'" + fieldName + "', '" + value + "'"));
        return this;
    }

    public WidgetView setMode(WidgetMode mode) {
        this.mode = mode;

        putOrProcessHandler(() -> callWidgetJavascript("setMode", "'" + mode.toString().toLowerCase() + "'"));
        return this;
    }

    public WidgetView setUserData(JSONObject jsonObject) {
        putOrProcessHandler(() -> callWidgetJavascript("setUserData", jsonObject.toString()));
        return this;
    }

    public WidgetView show() {
        Intent intent = new Intent(getContext(), WidgetViewActivity.class);
        getContext().startActivity(intent);
        return this;
    }

    public WidgetView hide() {
        if (onHideHandler != null) {
            onHideHandler.handle();
        }

        getContext().sendBroadcast(new Intent("close_widget_view"));

        return this;
    }

    public WidgetView collapse() {
        // TODO: to implement
        return this;
    }

    public WidgetView expand() {
        getContext().sendBroadcast(new Intent("maximize_widget_view"));
        return this;
    }

    public WidgetView restore() {
        getContext().sendBroadcast(new Intent("restore_widget_view"));
        return this;
    }

    public WidgetView onHide(OnHideHandler handler) {
        onHideHandler = handler;
        return this;
    }

    public WidgetView onSignUp(OnSignUpHandler handler) {
        onSignUpHandler = handler;
        registerOnSignUpHandler();
        return this;
    }

    public WidgetView onSignIn(OnSignInHandler handler) {
        onSignInHandler = handler;
        registerOnSignInHandler();
        return this;
    }

    public WidgetView onProcessNonFungibleReward(OnProcessNonFungibleRewardHandler handler) {
        onProcessNonFungibleRewardHandler = handler;
        registerOnProcessNonFungibleRewardHandler();
        return this;
    }

    public WidgetView onGetClaimedRewards(OnGetClaimedRewardsHandler handler) {
        onGetClaimedRewardsHandler = handler;
        registerOnGetClaimedRewardsHandler();
        return this;
    }

    public WidgetView onGetUserByEmail(OnGetUserByEmailHandler handler) {
        onGetUserByEmailHandler = handler;
        registerOnGetUserByEmailHandler();
        return this;
    }

    private void registerOnSignUpHandler() {
        if (onSignUpHandler != null) {
            bridgeWebView.registerHandler("onSignUp", (Context context, String data, CallBackFunction function) -> {
                if (data == null || data.equals("null")) {
                    function.onCallBack(null);
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        onSignUpHandler.handle(
                                jsonObject.getString("email"),
                                jsonObject.getString("token"),
                                jsonObject.getString("password"),
                                prepareExtras(jsonObject)
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        function.onCallBack(null);
                    }
                }
            });
        }
    }

    private void registerOnSignInHandler() {
        if (onSignInHandler != null) {
            bridgeWebView.registerHandler("onSignIn", (Context context, String data, CallBackFunction function) -> {
                if (data == null || data.equals("null")) {
                    function.onCallBack(null);
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        onSignInHandler.handle(
                                jsonObject.getString("email"),
                                jsonObject.getString("token"),
                                prepareExtras(jsonObject)
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        function.onCallBack(null);
                    }
                }
            });
        }
    }

    private void registerOnProcessNonFungibleRewardHandler() {
        if (onProcessNonFungibleRewardHandler != null) {
            bridgeWebView.registerHandler("onProcessNonFungibleReward", (Context context, String data, CallBackFunction function) -> {
                onProcessNonFungibleRewardHandler.handle(data);
                function.onCallBack(null);
            });
        }
    }

    private void registerOnGetClaimedRewardsHandler() {
        if (onGetClaimedRewardsHandler != null) {
            bridgeWebView.registerHandler("onGetClaimedRewards", (Context context, String data, CallBackFunction function) -> {
                onGetClaimedRewardsHandler.handle(data1 -> {
                    if (data1 == null) {
                        function.onCallBack("[]");
                    } else {
                        function.onCallBack(data1);
                    }
                });
            });
        }
    }

    private void registerOnGetUserByEmailHandler() {
        if (onGetUserByEmailHandler != null) {
            bridgeWebView.registerHandler("onGetUserByEmail", (Context context, String email, CallBackFunction function) -> {
                onGetUserByEmailHandler.handle(email, exists -> function.onCallBack(exists + ""));
            });
        }
    }

    private void callWidgetJavascript(String method, String data) {
        String jsCommand = "javascript:window.CRBWidget." + method + "(" + (data == null ? "" : data) + ");";
        Log.d(TAG, jsCommand);
        bridgeWebView.loadUrl(jsCommand);
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

    protected void clear() {
        SharedPreferences.Editor prefs = getContext().getSharedPreferences("storage", Context.MODE_PRIVATE).edit();
        for (StorageKeys sk : StorageKeys.values()) {
            prefs.remove(sk.desc());
        }
        prefs.apply();
    }

    protected static WidgetView getInstance() {
        return INSTANCE;
    }

    protected BridgeWebView getBridgeWebView() {
        return bridgeWebView;
    }

    protected void setInitialized(boolean initialized) {
        if (this.initialized != initialized) {
            this.initialized = initialized;

            if (this.initialized) {
                while (!java2JSHandlers.isEmpty()) {
                    Log.d(TAG, "Will process handler queue");
                    java2JSHandlers.remove(0).handle();
                }
            }
        }
    }

    private WidgetView reload() {
        initialized = false;
        clear();
        configureWebView();
        load();
        registerOnSignInHandler();
        registerOnSignUpHandler();
        registerOnProcessNonFungibleRewardHandler();
        registerOnGetClaimedRewardsHandler();
        registerOnGetUserByEmailHandler();
        return this;
    }

    private void configureWebView() {
        if (bridgeWebView != null) {
            bridgeWebView.clearCache(false);
        }

        bridgeWebView = new BridgeWebView(context);
        bridgeWebView.setBackgroundColor(Color.TRANSPARENT);

        for (JS2JavaHandlers handler : JS2JavaHandlers.values()) {
            bridgeWebView.registerHandler(handler.name(), handler.handler());
        }
    }

    private WidgetView load() {
        String jsPostfix = "/static/js/bundle.js";

        String html = generateHTML(this.env.sdkURL() + jsPostfix);
        Log.d(TAG, "Load HTML:\n" + html);

        bridgeWebView.loadDataWithBaseURL(this.env.widgetURL(), html, "text/html", "UTF-8", null);

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

    protected int getRestoreWidth() {
        return restoreWidth;
    }

    protected void setRestoreWidth(int restoreWidth) {
        this.restoreWidth = restoreWidth;
    }

    protected int getRestoreHeight() {
        return restoreHeight;
    }

    protected void setRestoreHeight(int restoreHeight) {
        this.restoreHeight = restoreHeight;
    }

    protected boolean isMaximized() {
        return isMaximized;
    }

    protected void setMaximized(boolean maximized) {
        isMaximized = maximized;
    }

    public interface OnSignInHandler {
        void handle(String email, String token, Map<String, String> extras);
    }

    public interface OnSignUpHandler {
        void handle(String email, String token, String password, Map<String, String> extras);
    }

    public interface OnGetUserByEmailHandler {
        void handle(String email, ResponseCallback callback);

        interface ResponseCallback {
            void handle(boolean exists);
        }
    }

    public interface OnProcessNonFungibleRewardHandler {
        void handle(String url);
    }

    public interface OnGetClaimedRewardsHandler {
        void handle(ResponseCallback callback);

        interface ResponseCallback {
            void handle(String data);
        }
    }

    public interface OnHideHandler {
        void handle();
    }

    private interface Java2JSHandler {
        void handle();
    }

    private void putOrProcessHandler(Java2JSHandler handler) {
        if (initialized) {
            handler.handle();
        } else {
            Log.d(TAG, "Will postpone handler");
            java2JSHandlers.add(handler);
        }
    }

    private enum StorageKeys {
        ACCOUNT("account"),
        PRIVATE_KEY("pk"),
        ENC_PRIVATE_KEY("enc_pk"),
        PUBLIC_KEY("pub_k"),
        TOKEN("token"),
        PASSWORD("password"),
        MNEMONIC("mnemonic"),
        SALT("salt"),
        EMAIL("email");

        private String desc;

        StorageKeys(String desc) {
            this.desc = desc;
        }

        public String desc() { return this.desc; }
    }
}
