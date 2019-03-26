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

/**
 * This is the main class which incapsulates all logic (opening/closing activity etc) and
 * provides high-level methods to manipulate with.
 *
 * <p>Almost all methods of this class supports "method chaining".</p>
 *
 * <p>All you need to start working with the class is to instantiate <tt>WidgetView</tt> once and
 * initialize it with few required params. Example:
 * </p>
 *
 * <p>
 *     <pre>
 *         {@code
 *              List<String> sections = new ArrayList<>();
 *              sections.add("YOUR_SECTION_1");
 *              sections.add("YOUR_SECTION_2");
 *              sections.add("YOUR_SECTION_3");
 *
 *              WidgetView widgetView = new WidgetView(context);
 *              widgetView.init("YOUR_APP_ID", sections);
 *         }
 *     </pre>
 * </p>
 *
 * <p>Parameter `sections` depends on your RMS configuration. If you use `default` placement for rewards you can omit this parameter.</p>
 *
 * <p>
 *     <pre>
 *         {@code
 *              WidgetView widgetView = new WidgetView(context);
 *              widgetView.init("YOUR_APP_ID");
 *         }
 *     </pre>
 * </p>
 *
 * <p>That's enough for start loading {@code WidgetView}, but note that {@code WidgetView} still
 * remains hidden. Also, first load of {@code WidgetView} takes a some time which depends on
 * network connection quality. That's why you need to init {@code WidgetView} as soon as possible.
 * Next initializations after opening app again will be faster because of caching.
 * </p>
 *
 * <p>If you want to show {@code WidgetView} right after it has initialized, you can add listener
 * {@see OnInitializationHandler} implementation which will invoke method <tt>show</tt> on
 * {@code WidgetView} instance. Example:
 * </p>
 *
 * <p>
 *     <pre>
 *         {@code
 *              widgetView.onInitializationFinished(hasItems -> {
 *                  widgetView.show(); // we can show widget without checking is it has rewards configured in RMS
 *              });
 *         }
 *     </pre>
 * </p>
 *
 * <p>
 *     <pre>
 *         {@code
 *              widgetView.onInitializationFinished(hasItems -> {
 *                  if (hasItems) { // we can show empty widget if it has no rewards configured in RMS
 *                      widgetView.show();
 *                  }
 *              });
 *         }
 *     </pre>
 * </p>
 *
 * <p>But the most common way is assigning <tt>OnClickListener</tt> to some {@code View}. Example:</p>
 *
 * <p>
 *     <pre>
 *         {@code
 *              button.setOnClickListener(view -> {
 *                  widgetView.show();
 *              });
 *         }
 *     </pre>
 * </p>
 *
 * @author  Mikhail Chachkouski
 *
 * Also see another callbacks you can provide to {@code WidgetView}:
 * @see OnSignInHandler
 * @see OnSignUpHandler
 * @see OnGetClaimedRewardsHandler
 * @see OnGetUserByEmailHandler
 * @see OnInitializationHandler
 */
public class WidgetView {

    static final String KEY_STORAGE = "storage";
    static final String KEY_REFERRER = "referrer";

    private static String TAG = "WidgetView";
    private static WidgetView INSTANCE;

    private WidgetEnv env = WidgetEnv.PRODUCTION;
    private WidgetMode mode = WidgetMode.REWARDS;

    private String appId = "";
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
    public WidgetView init(String appId, List<String> sections) {
        return init(appId, sections, WidgetEnv.PRODUCTION);
    }

    /**
     * Initializes and loads Widget. Note, that after initialization Widget is still invisible.
     * @param appId Application ID from RMS.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView init(String appId) {
        List<String> sections = new ArrayList<>();
        sections.add("default");
        return init(appId, sections, WidgetEnv.PRODUCTION);
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
     * Interface used after {@code WidgetView} init method.
     */
    public interface OnInitializationHandler {
        /**
         * Method to implement for <tt>onInitializationFinished</tt> listener.
         * @param hasItems is {@code WidgetView} received rewards configured in RMS.
         */
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
        bridgeWebView.loadUrl(this.env.widgetURL() + "/native.html?" +
                "platform=android" +
                "&v=" + BuildConfig.VERSION_NAME +
                "&appId=" + appId +
                "&sections=" + getSectionsStr() +
                "&mode=" + mode.name().toLowerCase() +
                "&env=" + env.name().toLowerCase());

        return this;
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

    private WidgetView init(String appId, List<String> sections, WidgetEnv env) {
        this.appId = appId;
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
