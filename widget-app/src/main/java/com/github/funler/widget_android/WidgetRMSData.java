package com.github.funler.widget_android;

import org.json.JSONException;
import org.json.JSONObject;

class WidgetRMSData {
    private double width;
    private double height;
    private double top;
    private double left;
    private boolean hasItems;


    public double getWidth() {
        return width;
    }

    public WidgetRMSData setWidth(double width) {
        this.width = width;
        return this;
    }

    public double getHeight() {
        return height;
    }

    public WidgetRMSData setHeight(double height) {
        this.height = height;
        return this;
    }

    public double getTop() {
        return top;
    }

    public WidgetRMSData setTop(double top) {
        this.top = top;
        return this;
    }

    public double getLeft() {
        return left;
    }

    public WidgetRMSData setLeft(double left) {
        this.left = left;
        return this;
    }

    public boolean isHasItems() {
        return hasItems;
    }

    public WidgetRMSData setHasItems(boolean hasItems) {
        this.hasItems = hasItems;
        return this;
    }

    public static WidgetRMSData fromJSON(String json) throws JSONException {
        WidgetRMSData data = new WidgetRMSData();

        JSONObject jsonObject = new JSONObject(json);
        data.setHasItems(jsonObject.has("hasItems") && jsonObject.getBoolean("hasItems"));
        data.setWidth(jsonObject.has("width") ? jsonObject.getDouble("width") : -1);
        data.setHeight(jsonObject.has("height") ? jsonObject.getDouble("height") : -1);
        data.setTop(jsonObject.has("top") ? jsonObject.getDouble("top") : -1);
        data.setLeft(jsonObject.has("left") ? jsonObject.getDouble("left") : -1);

        return data;
    }
}
