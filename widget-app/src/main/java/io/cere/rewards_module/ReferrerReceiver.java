package io.cere.rewards_module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import static io.cere.rewards_module.RewardsModule.KEY_REFERRER;
import static io.cere.rewards_module.RewardsModule.KEY_STORAGE;

public class ReferrerReceiver extends BroadcastReceiver {

    private final String TAG = "ReferrerReceiver";
    private final String ACTION_INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Intent is null");
            return;
        }

        if (!ACTION_INSTALL_REFERRER.equals(intent.getAction())) {
            Log.e(TAG, "Wrong action! Expected: " + ACTION_INSTALL_REFERRER + " but was: " + intent.getAction());
            return;
        }

        Bundle extras = intent.getExtras();
        if (intent.getExtras() == null) {
            Log.e(TAG, "No data in intent");
            return;
        }

        if (extras.get(KEY_REFERRER) != null) {
            String referrer = extras.get(KEY_REFERRER).toString();

            Log.d(TAG, "referrer = " + referrer);
            SharedPreferences.Editor prefs = context.getSharedPreferences(KEY_STORAGE, Context.MODE_PRIVATE).edit();
            prefs.putString(KEY_REFERRER, referrer);
            prefs.apply();
        }
    }
}
