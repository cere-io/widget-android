package com.github.funler.widget_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.github.funler.jsbridge.BridgeWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.close_widget_view;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.initialized_widget_view;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.input_blurred;
import static com.github.funler.widget_android.WidgetViewActivity.ActivityEvents.input_focused;

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
 *              widgetView.onInitializationFinished(() -> {
 *                  widgetView.show("YOUR_PLACEMENT");
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
 *                  widgetView.show("YOUR_PLACEMENT");
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
    private Map<String, Engagement> engagementMap = new HashMap<>();

    private String appId = "";

    private boolean initialized = false;
    private List<Java2JSHandler> java2JSHandlers = new ArrayList<>();

    private Context context;
    private BridgeWebView bridgeWebView;
    private int widthPx = 0;
    private int heightPx = 0;
    private int topPx = -1;
    private int leftPx = -1;
    private double widthPercentage = 0;
    private double heightPercentage = 0;
    private double topPercentage = 0;
    private double leftPercentage = 0;
    private boolean isMaximized = false;

    OnSignInHandler onSignInHandler = user -> setMode(WidgetMode.REWARDS);
    OnSignUpHandler onSignUpHandler = user -> setMode(WidgetMode.REWARDS);
    OnGetClaimedRewardsHandler onGetClaimedRewardsHandler = callback -> callback.handle(Collections.EMPTY_LIST);
    OnGetUserByEmailHandler onGetUserByEmailHandler = (email, callback) -> callback.handle(false);
    OnInitializationHandler onInitializationHandler = () -> {};

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
     * Initializes a newly created {@code WidgetView} object without initialization,
     * but with desired width and height.
     * @param context Context - Interface to global information about an application environment.
     * @param widthInPercents desired width for {@code WidgetView} in percents.
     * @param heightInPercents desired height for {@code WidgetView} in percents.
     */
    public WidgetView(Context context, float widthInPercents, float heightInPercents) {
        this.context = context;
        setWidth(widthInPercents);
        setHeight(heightInPercents);
        configureWebView();
        INSTANCE = this;
    }

    /**
     * Initializes a newly created {@code WidgetView} object without initialization,
     * but with desired width and height.
     * @param context Context - Interface to global information about an application environment.
     * @param widthInPercents desired width for {@code WidgetView} in percents.
     * @param heightInPercents desired height for {@code WidgetView} in percents.
     * @param topInPercents desired top margin for {@code WidgetView} in percents.
     * @param leftInPercents desired left margin for {@code WidgetView} in percents.
     */
    public WidgetView(Context context, float widthInPercents, float heightInPercents, float topInPercents, float leftInPercents) {
        this.context = context;
        setWidth(widthInPercents);
        setHeight(heightInPercents);
        setTop(topInPercents);
        setLeft(leftInPercents);
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
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView init(String appId) {
        return init(appId, WidgetEnv.PRODUCTION);
    }

    /**
     * Logging out user from Widget and cleaning up all stored data.
     */
    public void logout() {
        reload();
    }

    /**
     * Sends email to {@code WidgetView} field on SignUp/SignIn pages. For example, you know user email
     * and don't want to let user enter his email again. So, you can do that for user by invoking this
     * method.
     * @param value The field value.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView setEmail(String value) {
        putOrProcessHandler(() -> callWidgetJavascript("sendToField", "'email', " + "'" + value + "'"));
        return this;
    }

    public WidgetView setUserData(JSONObject jsonObject) {
        putOrProcessHandler(() -> callWidgetJavascript("setUserData", jsonObject.toString()));
        return this;
    }

    /**
     * Returns is {@code WidgetView} has reward items or social tasks for given placement.
     * @param placement Placement.
     * @return boolean.
     */
    public boolean hasItems(String placement) {
        if (engagementMap.containsKey(placement)) {
            return engagementMap.get(placement).getRewardItems().size() > 0 ||
                    engagementMap.get(placement).getSocialTasks().size() > 0;
        }

        return false;
    }

    /**
     * Returns a {@code Set} of available placements.
     * @return Set of available placements.
     */
    public Set<String> getPlacements() {
        return engagementMap.keySet();
    }

    /**
     * Opens {@code WidgetView} in on boarding mode .
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView showOnBoarding() {
        callWidgetJavascript("showOnBoarding", null);
        return this;
    }

    /**
     * Opens {@code WidgetView} with given placement.
     * @param placement Placement configured in RMS.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView show(String placement) {
        callWidgetJavascript("show", "'" + placement + "'");
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

    /**
     * Set desired width for {@code WidgetView} in percents.
     * @param widthInPercents value in percents.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView setWidth(double widthInPercents) {
        this.widthPercentage = widthInPercents;
        setWidthPx((int) Math.round(WidgetUtil.pxWidthFromPercents(getContext(), widthInPercents)));
        return this;
    }

    /**
     * Set desired height for {@code WidgetView} in percents.
     * @param heightInPercents value in percents.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView setHeight(double heightInPercents) {
        this.heightPercentage = heightInPercents;
        setHeightPx((int) Math.round(WidgetUtil.pxHeightFromPercents(getContext(), heightInPercents)));
        return this;
    }

    /**
     * Set desired top margin for {@code WidgetView} in percents.
     * @param topInPercents value in percents.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView setTop(double topInPercents) {
        this.topPercentage = topInPercents;
        setTopPx((int) Math.round(WidgetUtil.pxHeightFromPercents(getContext(), topInPercents)));
        return this;
    }

    /**
     * Set desired left margin for {@code WidgetView} in percents.
     * @param leftInPercents value in percents.
     * @return current instance of {@code WidgetView}.
     */
    public WidgetView setLeft(double leftInPercents) {
        this.leftPercentage = leftInPercents;
        setLeftPx((int) Math.round(WidgetUtil.pxWidthFromPercents(getContext(), leftInPercents)));
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
        void handle();
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

    protected void setInitialized(boolean initialized, WidgetRMSData data) {
        if (this.initialized != initialized) {
            this.initialized = initialized;
            getContext().sendBroadcast(new Intent(initialized_widget_view.name()));

            if (this.initialized) {
                while (!java2JSHandlers.isEmpty()) {
                    Log.d(TAG, "Will process handler queue");
                    java2JSHandlers.remove(0).handle();
                }

                if (data.getWidth() != -1) {
                    setWidth(data.getWidth());
                }

                if (data.getHeight() != -1) {
                    setHeight(data.getHeight());
                }

                if (data.getTop() != -1) {
                    setTop(data.getTop());
                }

                if (data.getLeft() != -1) {
                    setLeft(data.getLeft());
                }

                bridgeWebView.evaluateJavascript("window.CRBWidget.__getEngagements()", (String value) -> {
                    try {
                        JSONObject engs = new JSONObject(value);
                        Iterator<String> keys = engs.keys();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            engagementMap.put(key, Engagement.fromJson(engs.getString(key)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    onInitializationHandler.handle();
                });
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
                "&mode=" + mode.name().toLowerCase() +
                "&env=" + env.name().toLowerCase());

        return this;
    }

    int getWidthPx() {
        return widthPx;
    }

    void setWidthPx(int widthPx) {
        this.widthPx = widthPx;
    }

    int getHeightPx() {
        return heightPx;
    }

    void setHeightPx(int heightPx) {
        this.heightPx = heightPx;
    }

    int getTopPx() {
        return topPx;
    }

    void setTopPx(int topPx) {
        this.topPx = topPx;
    }

    int getLeftPx() {
        return leftPx;
    }

    void setLeftPx(int leftPx) {
        this.leftPx = leftPx;
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

    WidgetView show() {
        Intent intent = new Intent(getContext(), WidgetViewActivity.class);
        getContext().startActivity(intent);
        callWidgetJavascript("__showOnNative", null);
        return this;
    }

    WidgetView setMode(WidgetMode mode) {
        this.mode = mode;

        putOrProcessHandler(() -> callWidgetJavascript("setMode", "'" + mode.toString().toLowerCase() + "'"));
        return this;
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

    private WidgetView init(String appId, WidgetEnv env) {
        this.appId = appId;
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
