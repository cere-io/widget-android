package io.cere.rewards_module;

import android.content.Context;

import com.github.funler.jsbridge.BridgeHandler;
import com.github.funler.jsbridge.CallBackFunction;

import org.json.JSONArray;
import org.json.JSONException;

import io.cere.rewards_module.models.ClaimedReward;
import io.cere.rewards_module.models.User;

public enum UserDefinedHandlers {

    onGetUserByEmail((Context context, String email, CallBackFunction function) -> {
        RewardsModule.getInstance().onGetUserByEmailHandler.handle(email, exists -> function.onCallBack(exists + ""));
    }),

    onGetClaimedRewards((Context context, String data, CallBackFunction function) -> {
        RewardsModule.getInstance().onGetClaimedRewardsHandler.handle(claimedRewards -> {
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

    onSignIn((Context context, String data, CallBackFunction function) -> {
        if (data == null || data.equals("null")) {
            function.onCallBack(null);
        } else {
            try {
                RewardsModule.getInstance().onSignInHandler.handle(User.fromJson(data));
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
                RewardsModule.getInstance().onSignUpHandler.handle(User.fromJson(data));
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                function.onCallBack(null);
            }
        }
    });

    private BridgeHandler handler;

    UserDefinedHandlers(BridgeHandler handler) {
        this.handler = handler;
    }

    BridgeHandler handler() { return this.handler; }
}
