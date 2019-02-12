package com.github.funler.widget_android;

import android.content.Context;

import com.github.funler.jsbridge.BridgeHandler;
import com.github.funler.jsbridge.CallBackFunction;

import org.json.JSONArray;
import org.json.JSONException;

public enum WidgetUserDefinedHandlers {

    onGetUserByEmail((Context context, String email, CallBackFunction function) -> {
        WidgetView.getInstance().onGetUserByEmailHandler.handle(email, exists -> function.onCallBack(exists + ""));
    }),

    onGetClaimedRewards((Context context, String data, CallBackFunction function) -> {
        WidgetView.getInstance().onGetClaimedRewardsHandler.handle(claimedRewards -> {
            if (claimedRewards == null || claimedRewards.isEmpty()) {
                function.onCallBack("[]");
            } else {
                JSONArray jsonArray = new JSONArray();
                for (ClaimedReward reward : claimedRewards) {
                    jsonArray.put(reward.toJson());
                }
                function.onCallBack(jsonArray.toString());
            }
        });
    }),

    onProcessNonFungibleReward((Context context, String data, CallBackFunction function) -> {
        WidgetView.getInstance().onProcessNonFungibleRewardHandler.handle(data);
        function.onCallBack(null);
    }),

    onSignIn((Context context, String data, CallBackFunction function) -> {
        if (data == null || data.equals("null")) {
            function.onCallBack(null);
        } else {
            try {
                WidgetView.getInstance().onSignInHandler.handle(WidgetUser.fromJson(data));
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                function.onCallBack(null);
            }
        }
    }),

    onSignUp((Context context, String data, CallBackFunction function) -> {
        if (data == null || data.equals("null")) {
            function.onCallBack(null);
        } else {
            try {
                WidgetView.getInstance().onSignUpHandler.handle(WidgetUser.fromJson(data));
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                function.onCallBack(null);
            }
        }
    });

    private BridgeHandler handler;

    WidgetUserDefinedHandlers(BridgeHandler handler) {
        this.handler = handler;
    }

    BridgeHandler handler() { return this.handler; }
}
