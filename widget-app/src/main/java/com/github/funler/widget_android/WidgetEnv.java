package com.github.funler.widget_android;

public enum WidgetEnv {
    LOCAL("", ""),
    DEV("https://widget-sdk.dev.cere.io", "https://widget.dev.cere.io"),
    STAGE("https://widget-sdk.stage.cere.io", "https://widget.stage.cere.io"),
    PRODUCTION("https://widget-sdk.cere.io", "https://widget.cere.io");

    private String sdkURL;
    private String widgetURL;

    WidgetEnv(String sdkURL, String widgetURL) {
        this.sdkURL = sdkURL;
        this.widgetURL = widgetURL;
    }

    public String sdkURL() { return this.sdkURL; }
    public String widgetURL() { return this.widgetURL; }
}
