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

    OnSignInHandler onSignInHandler = user -> setMode(WidgetMode.REWARDS);
    OnSignUpHandler onSignUpHandler = user -> setMode(WidgetMode.REWARDS);
    OnProcessNonFungibleRewardHandler onProcessNonFungibleRewardHandler = url -> {};
    OnGetClaimedRewardsHandler onGetClaimedRewardsHandler = callback -> callback.handle(Collections.EMPTY_LIST);
    OnGetUserByEmailHandler onGetUserByEmailHandler = (email, callback) -> callback.handle(false);
    OnInitializationHandler onInitializationHandler = hasItems -> {};

    private OnHideHandler onHideHandler = null;

    /**
     * Initializes a newly created {@code WidgetView} object without initialization.
     * @param context Context - Interface to global information about an application environment.
     */
    public WidgetView(Context context) {
        this.context = context;
        configureWebView();
        INSTANCE = this;
    }

    /**
     * Return a {@code Context} provided in constructor
     * @return Context Interface to global information about an application environment.
     */
    public Context getContext() { return this.context; }

    /**
     * Initializes and loads Widget. Note, that after initialization Widget is still invisible.
     * @param appId Application ID from RMS.
     * @param sections List of sections you want to display in Widget.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView init(String appId, String userId, List<String> sections) {
        return init(appId, userId, sections, WidgetEnv.PRODUCTION);
    }

    /**
     * Logging out user from Widget and cleaning up all stored data.
     */
    public void logout() {
        reload();
    }

    /**
     * Sends data to {@code WidgetView} field on SignUp/SignIn pages. For example, you know user email
     * and don't want to let user enter his email again. So, you can do that for user by invoking this
     * method.
     * @param fieldName The field name you want to populate. Examples: "email", "password".
     * @param value The field value.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView sendDataToField(String fieldName, String value) {
        putOrProcessHandler(() -> callWidgetJavascript("sendToField", "'" + fieldName + "', '" + value + "'"));
        return this;
    }

    /**
     * Switches {@code WidgetView} mode.
     * @param mode Current available modes are: WidgetMode.LOGIN, WidgetView.REWARDS.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView setMode(WidgetMode mode) {
        this.mode = mode;

        putOrProcessHandler(() -> callWidgetJavascript("setMode", "'" + mode.toString().toLowerCase() + "'"));
        return this;
    }

    public WidgetView setUserData(JSONObject jsonObject) {
        putOrProcessHandler(() -> callWidgetJavascript("setUserData", jsonObject.toString()));
        return this;
    }

    /**
     * Opens Widget in provided {@code WidgetMode}. Default mode - WidgetMode.REWARDS.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView show() {
        Intent intent = new Intent(getContext(), WidgetViewActivity.class);
        getContext().startActivity(intent);
        callWidgetJavascript("__showOnNative", null);
        return this;
    }

    /**
     * Closes Widget.
     * @return current instance of {@code WidgetView}.
     */
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

    /**
     * Expands Widget to Fullscreen.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView expand() {
        getContext().sendBroadcast(new Intent(maximize_widget_view.name()));
        return this;
    }

    /**
     * Returns Widget to initial size.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView restore() {
        getContext().sendBroadcast(new Intent(restore_widget_view.name()));
        return this;
    }

    /**
     * Optional callback which will be fired after {@code WidgetView} hide method.
     * @param handler instance of {@code OnHideHandler}.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView onHide(OnHideHandler handler) {
        onHideHandler = handler;
        return this;
    }

    /**
     * Optional callback which will be fired after {@code WidgetView} finished sign up.
     * @param handler instance of {@code OnSignUpHandler}.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView onSignUp(OnSignUpHandler handler) {
        onSignUpHandler = handler;
        return this;
    }

    /**
     * Optional callback which will be fired after {@code WidgetView} finished sign in.
     * @param handler instance of {@code OnSignInHandler}.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView onSignIn(OnSignInHandler handler) {
        onSignInHandler = handler;
        return this;
    }

    /**
     * Optional callback which will be fired after {@code WidgetView} processed non-fungible reward.
     * @param handler instance of {@code OnProcessNonFungibleRewardHandler}.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView onProcessNonFungibleReward(OnProcessNonFungibleRewardHandler handler) {
        onProcessNonFungibleRewardHandler = handler;
        return this;
    }

    /**
     * Optional callback which should return {@code List<ClaimedReward>} which user already bought.
     * @param handler instance of {@code OnGetClaimedRewardsHandler}.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView onGetClaimedRewards(OnGetClaimedRewardsHandler handler) {
        onGetClaimedRewardsHandler = handler;
        return this;
    }

    /**
     * Optional callback which should return is user already exists in your system.
     * @param handler instance of {@code OnGetUserByEmailHandler}
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView onGetUserByEmail(OnGetUserByEmailHandler handler) {
        onGetUserByEmailHandler = handler;
        return this;
    }

    /**
     * Optional callback which will be fired after {@code WidgetView} initialized.
     * @param handler instance of {@code OnInitializationHandler}.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView onInitializationFinished(OnInitializationHandler handler) {
        onInitializationHandler = handler;
        return this;
    }

    /**
     * Interface used to callback after sign in.
     */
    public interface OnSignInHandler {
        void handle(WidgetUser user);
    }

    /**
     * Interface used to callback after sign up.
     */
    public interface OnSignUpHandler {
        void handle(WidgetUser user);
    }

    /**
     * Interface used to determine is user already exists in external system.
     */
    public interface OnGetUserByEmailHandler {
        void handle(String email, ResponseCallback callback);

        interface ResponseCallback {
            void handle(boolean exists);
        }
    }

    /**
     * Interface used to process non-fungible rewards.
     */
    public interface OnProcessNonFungibleRewardHandler {
        void handle(String url);
    }

    /**
     * Interface used for getting rewards user already bought.
     */
    public interface OnGetClaimedRewardsHandler {
        void handle(ResponseCallback callback);

        interface ResponseCallback {
            void handle(List<ClaimedReward> claimedRewards);
        }
    }

    /**
     * Interface used after {@code WidgetView} hide method.
     */
    public interface OnHideHandler {
        void handle();
    }

    /**
     * Interface user after {@code WidgetView} init method.
     */
    public interface OnInitializationHandler {
        void handle(boolean hasItems);
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

                onInitializationHandler.handle(hasItems);
            }
        }
    }

    private void callWidgetJavascript(String method, String data) {
        String jsCommand = "javascript:window.CRBWidget." + method + "(" + (data == null ? "" : data) + ");";
        Log.d(TAG, jsCommand);
        bridgeWebView.loadUrl(jsCommand);
    }

    private WidgetView reload() {
        initialized = false;
        clear();
        configureWebView();
        load();
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

        for (WidgetUserDefinedHandlers handler : WidgetUserDefinedHandlers.values()) {
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

    private WidgetView init(String appId, String userId, List<String> sections, WidgetEnv env) {
        this.appId = appId;
        this.userId = userId;
        this.sections = sections;
        this.env = env;
        load();

        return this;
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
