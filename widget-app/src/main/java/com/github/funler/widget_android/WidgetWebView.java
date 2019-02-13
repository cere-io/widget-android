package com.github.funler.widget_android;

import android.content.Context;
import android.util.AttributeSet;

import com.github.funler.jsbridge.BridgeWebView;
import com.github.funler.jsbridge.BridgeWebViewClient;

class WidgetWebView extends BridgeWebView {

    public WidgetWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetWebView(Context context) {
        super(context);
    }

    public WidgetWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected BridgeWebViewClient generateBridgeWebViewClient() {
        return new WidgetWebViewClient(this);
    }
}
