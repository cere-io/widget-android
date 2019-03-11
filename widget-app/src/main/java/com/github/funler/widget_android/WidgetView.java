package com.github.funler.widget_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.github.funler.jsbridge.BridgeWebView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.funler.widget_android.WidgetUserDefinedHandlers.onGetClaimedRewards;
import static com.github.funler.widget_android.WidgetUserDefinedHandlers.onGetUserByEmail;
import static com.github.funler.widget_android.WidgetUserDefinedHandlers.onProcessNonFungibleReward;
import static com.github.funler.widget_android.WidgetUserDefinedHandlers.onSignIn;
import static com.github.funler.widget_android.WidgetUserDefinedHandlers.onSignUp;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.close_widget_view;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.initialized_widget_view;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.input_blurred;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.input_focused;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.maximize_widget_view;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.restore_widget_view;

public class WidgetView {

    static final String KEY_STORAGE = "storage";
    static final String KEY_REFERRER = "referrer";

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

    OnSignInHandler onSignInHandler = null;
    OnSignUpHandler onSignUpHandler = null;
    OnProcessNonFungibleRewardHandler onProcessNonFungibleRewardHandler = null;
    OnGetClaimedRewardsHandler onGetClaimedRewardsHandler = null;
    OnGetUserByEmailHandler onGetUserByEmailHandler = null;
    OnInitializationHandler onInitializationHandler = null;

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
        callWidgetJavascript("__showOnNative", null);
        return this;
    }

    public WidgetView hide() {
        if (onHideHandler != null) {
            onHideHandler.handle();
        }

        getContext().sendBroadcast(new Intent(close_widget_view.name()));

        return this;
    }

    public WidgetView collapse() {
        // TODO: to implement
        return this;
    }

    public WidgetView expand() {
        getContext().sendBroadcast(new Intent(maximize_widget_view.name()));
        return this;
    }

    public WidgetView restore() {
        getContext().sendBroadcast(new Intent(restore_widget_view.name()));
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

    public WidgetView onInitializationFinished(OnInitializationHandler handler) {
        onInitializationHandler = handler;
        return this;
    }

    private void registerOnSignUpHandler() {
        if (onSignUpHandler != null) {
            bridgeWebView.registerHandler(onSignUp.name(), onSignUp.handler());
        }
    }

    private void registerOnSignInHandler() {
        if (onSignInHandler != null) {
            bridgeWebView.registerHandler(onSignIn.name(), onSignIn.handler());
        }
    }

    private void registerOnProcessNonFungibleRewardHandler() {
        if (onProcessNonFungibleRewardHandler != null) {
            bridgeWebView.registerHandler(onProcessNonFungibleReward.name(), onProcessNonFungibleReward.handler());
        }
    }

    private void registerOnGetClaimedRewardsHandler() {
        if (onGetClaimedRewardsHandler != null) {
            bridgeWebView.registerHandler(onGetClaimedRewards.name(), onGetClaimedRewards.handler());
        }
    }

    private void registerOnGetUserByEmailHandler() {
        if (onGetUserByEmailHandler != null) {
            bridgeWebView.registerHandler(onGetUserByEmail.name(), onGetUserByEmail.handler());
        }
    }

    private void callWidgetJavascript(String method, String data) {
        String jsCommand = "javascript:window.CRBWidget." + method + "(" + (data == null ? "" : data) + ");";
        Log.d(TAG, jsCommand);
        bridgeWebView.loadUrl(jsCommand);
    }

    protected void clear() {
        SharedPreferences.Editor prefs = getContext().getSharedPreferences(KEY_STORAGE, Context.MODE_PRIVATE).edit();
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

    protected boolean isInitialized() { return initialized; }

    protected void setInitialized(boolean initialized, boolean hasItems) {
        if (this.initialized != initialized) {
            this.initialized = initialized;
            getContext().sendBroadcast(new Intent(initialized_widget_view.name()));

            if (this.initialized) {
                while (!java2JSHandlers.isEmpty()) {
                    Log.d(TAG, "Will process handler queue");
                    java2JSHandlers.remove(0).handle();
                }

                if (onInitializationHandler != null) {
                    onInitializationHandler.handle(hasItems);
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

    int getRestoreWidth() {
        return restoreWidth;
    }

    void setRestoreWidth(int restoreWidth) {
        this.restoreWidth = restoreWidth;
    }

    int getRestoreHeight() {
        return restoreHeight;
    }

    void setRestoreHeight(int restoreHeight) {
        this.restoreHeight = restoreHeight;
    }

    boolean isMaximized() {
        return isMaximized;
    }

    void setMaximized(boolean maximized) {
        isMaximized = maximized;
    }

    void inputFocused(float y) {
        Intent i = new Intent(input_focused.name());
        i.putExtra("y", y);
        getContext().sendBroadcast(i);
    }

    void inputBlurred() {
        getContext().sendBroadcast(new Intent(input_blurred.name()));
    }

    public interface OnSignInHandler {
        void handle(WidgetUser user);
    }

    public interface OnSignUpHandler {
        void handle(WidgetUser user);
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
            void handle(List<ClaimedReward> claimedRewards);
        }
    }

    public interface OnHideHandler {
        void handle();
    }

    public interface OnInitializationHandler {
        void handle(boolean hasItems);
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

    enum StorageKeys {
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
