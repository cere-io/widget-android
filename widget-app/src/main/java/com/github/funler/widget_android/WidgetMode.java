package com.github.funler.widget_android;

public enum WidgetMode {
    LOCAL(""),
    DEV("https://widget-sdk.dev.cere.io"),
    STAGE("https://widget-sdk.stage.cere.io"),
    PRODUCTION("https://widget-sdk.cere.io");

    private String url;

    WidgetMode(String url) {
        this.url = url;
    }

    public String url() { return this.url; }
}
