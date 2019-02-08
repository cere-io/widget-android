package com.github.funler.widget_android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.github.funler.jsbridge.BridgeHandler;
import com.github.funler.jsbridge.CallBackFunction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public enum JS2JavaHandlers {
    logout((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "logout");
        WidgetView.getInstance().logout();
        function.onCallBack("true");
    }),

    collapse((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "collapse, data: " + data);
        WidgetView.getInstance().collapse();
        function.onCallBack(null);
    }),

    expand((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "expand, data: " + data);
        WidgetView.getInstance().expand();
        function.onCallBack(null);
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
        WidgetView.getInstance().setInitialized(true);
        function.onCallBack(null);
    }),

    getAppsInfo((Context context, String data, CallBackFunction function) -> {
        List<AppInfo> appsInfo = new ApkInfoExtractor(context).getInstalledAppsInfo();

        JSONArray array = new JSONArray();
        for (AppInfo appInfo : appsInfo) {
            array.put(appInfo.toJSON());
        }

        function.onCallBack(array.toString());
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
