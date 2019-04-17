package com.github.funler.widget_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.github.funler.jsbridge.BridgeHandler;
import com.github.funler.jsbridge.CallBackFunction;

import org.json.JSONException;
import org.json.JSONObject;

import static com.github.funler.widget_android.WidgetView.KEY_REFERRER;
import static com.github.funler.widget_android.WidgetView.KEY_STORAGE;

public enum JS2JavaHandlers {
    logout((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "logout");
        WidgetView.getInstance().logout();
        function.onCallBack("true");
    }),

    restore((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "restore, data: " + data);
        WidgetView.getInstance().restore();
        function.onCallBack(null);
    }),

    show((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "show");
        WidgetView.getInstance().show();
        function.onCallBack(null);
    }),

    hide((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "hide");
        WidgetView.getInstance().hide();
        function.onCallBack(null);
    }),

    initialized((Context context, String data, CallBackFunction function) -> {
        try {
            WidgetView.getInstance().setInitialized(true, WidgetRMSData.fromJSON(data));
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            function.onCallBack(null);
        }
    }),

    shareWith((Context context, String data, CallBackFunction function) -> {
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONObject app = jsonObject.getJSONObject("app");

            String packageName = app.getString("androidId");
            String dataToShare = jsonObject.getString("data");

            if (isAppAvailable(context, packageName)) {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setPackage(packageName);
                send.putExtra(Intent.EXTRA_TEXT, dataToShare);
                send.setType("text/plain");
                Intent chooser = Intent.createChooser(send, "Share");
                context.startActivity(chooser);
            } else {
                Intent googlePlay = new Intent(Intent.ACTION_VIEW);
                googlePlay.setData(Uri.parse("market://details?id=" + packageName));
                context.startActivity(googlePlay);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        function.onCallBack(null);
    }),

    share((Context context, String data, CallBackFunction function) -> {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.putExtra(Intent.EXTRA_TEXT, data);
        send.setType("text/plain");

        Intent chooser = Intent.createChooser(send, "Share");
        context.startActivity(chooser);
        function.onCallBack(null);
    }),

    getReferralsInfo((Context context, String data, CallBackFunction function) -> {
        SharedPreferences prefsReader = context.getSharedPreferences(KEY_STORAGE, Context.MODE_PRIVATE);
        String userId = prefsReader.getString(KEY_REFERRER, "");

        if (!userId.isEmpty()) {
            context.getSharedPreferences(KEY_STORAGE, Context.MODE_PRIVATE)
                    .edit()
                    .remove(KEY_REFERRER)
                    .apply();
        }

        function.onCallBack(userId);
    }),

    showNativeMessage((Context context, String data, CallBackFunction function) -> {
        Toast.makeText(context, data, Toast.LENGTH_LONG).show();
        function.onCallBack(null);
    }),

    inputFocused((Context context, String data, CallBackFunction function) -> {
        WidgetView.getInstance().inputFocused(Float.parseFloat(data));
        function.onCallBack(null);
    }),

    inputBlurred((Context context, String data, CallBackFunction function) -> {
        WidgetView.getInstance().inputBlurred();
        function.onCallBack(null);
    });

    private BridgeHandler handler;

    JS2JavaHandlers(BridgeHandler handler) {
        this.handler = handler;
    }

    public BridgeHandler handler() {
        return handler;
    }

    private static boolean isAppAvailable(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static String getTag() {
        return "WidgetJS2Handler";
    }
}
