package com.github.funler.widget_android;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.github.funler.jsbridge.BridgeHandler;
import com.github.funler.jsbridge.CallBackFunction;

public enum JS2JavaHandlers {
    logout((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "logout");
        WidgetView.getInstance().clear();
        function.onCallBack("true");
    }),

    collapse((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "collapse, data: " + data);
//        WidgetView widget = WidgetView.getInstance();
//
//        try {
//            JSONArray dataArray = new JSONArray(data);
//            if (dataArray.length() != 0) {
//                int newWidth = dataArray.getInt(0);
//                int newHeight = dataArray.getInt(1);
//
//                float scale = context.getResources().getDisplayMetrics().density;
//                int widthPx = (int) (newWidth * scale + 0.5f);
//                int heightPx = (int) (newHeight * scale + 0.5f);
//
//                widget.resize(widthPx, heightPx);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } finally {
//            function.onCallBack(null);
//        }
        WidgetView.getInstance().setVisibility(View.INVISIBLE);
        function.onCallBack(null);
    }),

    expand((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "expand, data: " + data);
//        WidgetView widget = WidgetView.getInstance();
//        widget.resize(widget.getDefaultWidth(), widget.getDefaultHeight());
//        function.onCallBack(null);
        WidgetView.getInstance().setVisibility(View.VISIBLE);
        function.onCallBack(null);
    }),

    show((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "show");
        WidgetView.getInstance().setVisibility(View.VISIBLE);
        function.onCallBack(null);
    }),

    hide((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "hide");
        WidgetView.getInstance().setVisibility(View.INVISIBLE);
        function.onCallBack(null);
    }),

    initialized((Context context, String data, CallBackFunction function) -> {
        WidgetView.getInstance().setInitialized(true);
        function.onCallBack(null);
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
