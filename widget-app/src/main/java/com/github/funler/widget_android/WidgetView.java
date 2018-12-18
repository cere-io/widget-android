package com.github.funler.widget_android;

import android.content.Context;

import com.github.funler.jsbridge.BridgeWebView;

public class WidgetView extends BridgeWebView {

    private WidgetMode mode = WidgetMode.PRODUCTION;
    private String appId = "";
    private String userId = "";
    private String[] sections = {};

    public WidgetView(Context context) {
        super(context);
    }

    public WidgetView(Context context, String appId, String userId, String[] sections) {
        super(context);
        this.appId = appId;
        this.userId = userId;
        this.sections = sections;
    }

    public WidgetView setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public WidgetView setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public WidgetView setSections(String[] sections) {
        this.sections = sections;
        return this;
    }

    public WidgetView load() {
        this.loadUrl(generateUrl());
        return this;
    }

    private String generateUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String section : this.sections) {
            stringBuilder.append(section).append(",");
        }
        String sections = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(","));

        return new StringBuilder("/assets/index.html?")
                .append("env=").append(this.mode.name().toLowerCase())
                .append("&appId=").append(this.appId)
                .append("&userId=").append(this.userId)
                .append("&sections=").append(sections)
                .toString();
    }

    private boolean isProduction() {
        return this.mode == WidgetMode.PRODUCTION;
    }

    private WidgetView setMode(WidgetMode mode) {
        this.mode = mode;
        load();
        return this;
    }
}
