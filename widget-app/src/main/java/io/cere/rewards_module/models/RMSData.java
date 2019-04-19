package io.cere.rewards_module.models;

import org.json.JSONException;
import org.json.JSONObject;

public class RMSData {
    private double width;
    private double height;
    private double top;
    private double left;

    public double getWidth() {
        return width;
    }

    public RMSData setWidth(double width) {
        this.width = width;
        return this;
    }

    public double getHeight() {
        return height;
    }

    public RMSData setHeight(double height) {
        this.height = height;
        return this;
    }

    public double getTop() {
        return top;
    }

    public RMSData setTop(double top) {
        this.top = top;
        return this;
    }

    public double getLeft() {
        return left;
    }

    public RMSData setLeft(double left) {
        this.left = left;
        return this;
    }

    public static RMSData fromJSON(String json) throws JSONException {
        RMSData data = new RMSData();

        JSONObject jsonObject = new JSONObject(json);
        data.setWidth(jsonObject.has("width") ? jsonObject.getDouble("width") : -1);
        data.setHeight(jsonObject.has("height") ? jsonObject.getDouble("height") : -1);
        data.setTop(jsonObject.has("top") ? jsonObject.getDouble("top") : -1);
        data.setLeft(jsonObject.has("left") ? jsonObject.getDouble("left") : -1);

        return data;
    }
}
