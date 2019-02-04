package com.github.funler.widget_android;

import org.json.JSONException;
import org.json.JSONObject;

public class AppInfo {

    private String appPackageName;
    private String appName;
    private String icon;

    public AppInfo(String appPackageName, String appName, String icon) {
        this.appPackageName = appPackageName;
        this.appName = appName;
        this.icon = icon;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("app_package_name", appPackageName);
            jsonObject.put("app_name", appName);
            jsonObject.put("app_icon", icon);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
