package com.github.funler.widget_android;

import android.content.Context;
import android.util.Log;

import com.github.funler.jsbridge.BridgeHandler;
import com.github.funler.jsbridge.CallBackFunction;

public enum JS2JavaHandlers {
    logout((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "logout");
        WidgetView.getInstance().reloadWidgetView();
        function.onCallBack("true");
    });

    private BridgeHandler handler;

    JS2JavaHandlers(BridgeHandler handler) {
        this.handler = handler;
    }

    public BridgeHandler handler() {
        return handler;
    }

    private static String getTag() {
        return JS2JavaHandlers.class.getSimpleName();
    }
}
