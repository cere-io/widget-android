package com.github.funler.widget_android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClaimedReward {

    private String title;
    private String img;
    private String price;
    private String redemptionInstructions;
    private List<String> additionalInfo = Collections.EMPTY_LIST;

    public String getTitle() {
        return title;
    }

    public ClaimedReward setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImg() {
        return img;
    }

    public ClaimedReward setImg(String img) {
        this.img = img;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public ClaimedReward setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getRedemptionInstructions() { return this.redemptionInstructions; }

    public ClaimedReward setRedemptionInstructions(String text) {
        this.redemptionInstructions = text;
        return this;
    }

    public List<String> getAdditionalInfo() {
        return additionalInfo;
    }

    public ClaimedReward setAdditionalInfo(List<String> additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            json.put("title", getTitle());
            json.put("img", getImg());
            json.put("price", getPrice());
            json.put("redemptionInstructions", getRedemptionInstructions());
            for (String info : getAdditionalInfo()) {
                jsonArray.put(info);
            }
            json.put("additionalInfo", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static ClaimedReward fromJson(String jsonString) throws JSONException {
        ClaimedReward reward = new ClaimedReward();

        JSONObject json = new JSONObject(jsonString);
        reward.setTitle(json.getString("title"));
        reward.setImg(json.getString("img"));
        reward.setPrice(json.getString("price"));

        if (json.has("redemptionInstructions")) {
            reward.setRedemptionInstructions(json.getString("redemptionInstructions"));
        }

        JSONArray jsonArray = new JSONArray();
        if (json.has("additionalInfo")) {
            jsonArray = json.getJSONArray("additionalInfo");
        }

        if (jsonArray.length() != 0) {
            List<String> info = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                info.add(jsonArray.get(i).toString());
            }
            reward.setAdditionalInfo(info);
        }

        return reward;
    }
}
