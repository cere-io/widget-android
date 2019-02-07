package com.github.funler.widget_android;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;

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
    private Dialog dialog = null;
    private OnHideHandler onHideHandler = null;

    public WidgetView(Context context) {
        this.context = context;
        bridgeWebView = new BridgeWebView(context);
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(bridgeWebView);
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
        putOrProcessHandler(() -> bridgeWebView.callHandler("logout", "", (String data) -> Log.d(TAG, "logged out")));
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
        dialog.show();
        return this;
    }

    public WidgetView hide() {
        dialog.hide();

        if (onHideHandler != null) {
            onHideHandler.handle();
        }

        return this;
    }

    public WidgetView collapse() {
        // TODO: to implement
        return this;
    }

    public WidgetView expand() {
        // TODO: to implement
        return this;
    }

    public WidgetView restore() {
        // TODO: to implement
        return this;
    }

    public WidgetView onHide(OnHideHandler handler) {
        onHideHandler = handler;
        return this;
    }

    public WidgetView onSignUp(OnSignUpHandler handler) {
        bridgeWebView.registerHandler("onSignUp", (Context context, String data, CallBackFunction function) -> {
            try {
                JSONObject jsonObject = new JSONObject(data);
                handler.handle(
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
        });

        return this;
    }

    public WidgetView onSignIn(OnSignInHandler handler) {
        bridgeWebView.registerHandler("onSignIn", (Context context, String data, CallBackFunction function) -> {
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

    public WidgetView onProcessNonFungibleReward(OnProcessNonFungibleRewardHandler handler) {
        bridgeWebView.registerHandler("onProcessNonFungibleReward", (Context context, String data, CallBackFunction function) -> {
            handler.handle(data);
            function.onCallBack(null);
        });

        return this;
    }

    public WidgetView onGetClaimedRewards(OnGetClaimedRewards handler) {
        bridgeWebView.registerHandler("onGetClaimedRewards", (Context context, String data, CallBackFunction function) -> {
            handler.handle(data1 -> {
                if (data1 == null) {
                    function.onCallBack("[]");
                } else {
                    function.onCallBack(data1);
                }
            });
        });

        return this;
    }

    public WidgetView onGetUserByEmail(OnGetUserByEmail handler) {
        bridgeWebView.registerHandler("onGetUserByEmail", (Context context, String email, CallBackFunction function) -> {
            handler.handle(email, exists -> function.onCallBack(exists + ""));
        });

        return this;
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

    private WidgetView load() {
        INSTANCE = this;

        for (JS2JavaHandlers handler : JS2JavaHandlers.values()) {
            bridgeWebView.registerHandler(handler.name(), handler.handler());
        }

        bridgeWebView.setBackgroundColor(Color.TRANSPARENT);
        bridgeWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

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

    public interface OnSignInHandler {
        void handle(String email, String token, Map<String, String> extras);
    }

    public interface OnSignUpHandler {
        void handle(String email, String token, String password, Map<String, String> extras);
    }

    public interface OnGetUserByEmail {
        void handle(String email, ResponseCallback callback);

        interface ResponseCallback {
            void handle(boolean exists);
        }
    }

    public interface OnProcessNonFungibleRewardHandler {
        void handle(String url);
    }

    public interface OnGetClaimedRewards {
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
