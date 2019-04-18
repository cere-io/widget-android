package io.cere.rewards_module;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.cere.funler.jsbridge.BridgeHandler;
import com.cere.funler.jsbridge.CallBackFunction;

import org.json.JSONException;
import org.json.JSONObject;

import io.cere.rewards_module.models.RMSData;

import static io.cere.rewards_module.RewardsModule.KEY_REFERRER;
import static io.cere.rewards_module.RewardsModule.KEY_STORAGE;

public enum JS2JavaHandlers {
    logout((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "logout");
        RewardsModule.getInstance().logout();
        function.onCallBack("true");
    }),

    show((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "show");
        RewardsModule.getInstance().show();
        function.onCallBack(null);
    }),

    hide((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "hide");
        RewardsModule.getInstance().hide();
        function.onCallBack(null);
    }),

    initialized((Context context, String data, CallBackFunction function) -> {
        try {
            RewardsModule.getInstance().setInitialized(true, RMSData.fromJSON(data));
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
        RewardsModule.getInstance().inputFocused(Float.parseFloat(data));
        function.onCallBack(null);
    }),

    inputBlurred((Context context, String data, CallBackFunction function) -> {
        RewardsModule.getInstance().inputBlurred();
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
