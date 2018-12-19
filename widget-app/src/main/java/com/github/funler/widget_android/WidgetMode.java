package com.github.funler.widget_android;

public enum WidgetMode {
    LOCAL(""),
    DEV("https://widget-sdk.dev.cere.io/static/js/bundle.js"),
    STAGE("https://widget-sdk.stage.cere.io/static/js/bundle.js"),
    PRODUCTION("https://widget-sdk.cere.io/static/js/bundle.js");

    private String url;

    WidgetMode(String url) {
        this.url = url;
    }

    public String url() { return this.url; }
}
