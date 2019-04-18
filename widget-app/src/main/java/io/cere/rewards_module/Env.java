package io.cere.rewards_module;

public enum Env {
    DEVELOPMENT("", ""),
    LOCAL("http://192.168.100.11:3011", "http://192.168.100.11:3002"),
    DEV1("https://widget-sdk.dev.cere.io", "https://widget.dev.cere.io"),
    STAGE("https://widget-sdk.stage.cere.io", "https://widget.stage.cere.io"),
    PRODUCTION("https://widget-sdk.cere.io", "https://widget.cere.io");

    private String sdkURL;
    private String widgetURL;

    Env(String sdkURL, String widgetURL) {
        this.sdkURL = sdkURL;
        this.widgetURL = widgetURL;
    }

    public String sdkURL() { return this.sdkURL; }
    public String widgetURL() { return this.widgetURL; }
}
