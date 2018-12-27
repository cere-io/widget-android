package com.github.funler.widget_android;

import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;

import com.github.funler.jsbridge.BridgeHandler;
import com.github.funler.jsbridge.CallBackFunction;

import org.json.JSONArray;
import org.json.JSONException;

public enum JS2JavaHandlers {
    logout((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "logout");
        WidgetView.getInstance().reloadWidgetView();
        function.onCallBack("true");
    }),

    collapse((Context context, String data, CallBackFunction function) -> {
        Log.d(getTag(), "collapse, data: " + data);
        WidgetView widget = WidgetView.getInstance();
        try {
            JSONArray dataArray = new JSONArray(data);
            if (dataArray.length() != 0) {
                int newWidth = dataArray.getInt(0);
                int newHeight = dataArray.getInt(1);
                if (widget.getDefaultWidth() == 0 || widget.getDefaultHeight() == 0) {
                    Log.d(getTag(), "width: " + widget.getWidth() + ", height: " + widget.getHeight());
                    widget.setDefaultWidth(widget.getWidth());
                    widget.setDefaultHeight(widget.getHeight());
                }
                widget.setLayoutParams(new RelativeLayout.LayoutParams(newWidth, newHeight));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            function.onCallBack(null);
        }
    }),

    expand((Context context, String data, CallBackFunction function) -> {
        WidgetView widget = WidgetView.getInstance();
        int width = widget.getDefaultWidth();
        int height = widget.getDefaultHeight();

        if (width != 0 || height != 0) {
            Log.d(getTag(), "expand to width: " + width + ", height: " + height);
            widget.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        } else {
            widget.setDefaultWidth(width);
            widget.setDefaultHeight(height);
        }

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
