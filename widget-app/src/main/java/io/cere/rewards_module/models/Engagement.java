package io.cere.rewards_module.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Engagement {
    private int id;
    private int campaignId;
    private int placementId;
    private String placementName;
    private String key;
    private List<RewardItem> rewardItems = Collections.EMPTY_LIST;
    private List<SocialTask> socialTasks = Collections.EMPTY_LIST;

    public int getId() {
        return id;
    }

    public Engagement setId(int id) {
        this.id = id;
        return this;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public Engagement setCampaignId(int campaignId) {
        this.campaignId = campaignId;
        return this;
    }

    public int getPlacementId() {
        return placementId;
    }

    public Engagement setPlacementId(int placementId) {
        this.placementId = placementId;
        return this;
    }

    public String getPlacementName() {
        return placementName;
    }

    public Engagement setPlacementName(String placementName) {
        this.placementName = placementName;
        return this;
    }

    public List<RewardItem> getRewardItems() {
        return rewardItems;
    }

    public Engagement setRewardItems(List<RewardItem> rewardItems) {
        this.rewardItems = rewardItems;
        return this;
    }

    public List<SocialTask> getSocialTasks() {
        return socialTasks;
    }

    public Engagement setSocialTasks(List<SocialTask> socialTasks) {
        this.socialTasks = socialTasks;
        return this;
    }

    public String getKey() {
        return key;
    }

    public Engagement setKey(String key) {
        this.key = key;
        return this;
    }

    public static Engagement fromJson(String json) throws JSONException {
        Engagement e = new Engagement();

        JSONObject jsonObject = new JSONObject(json);
        e.setId(jsonObject.has("engagement_id") ? jsonObject.getInt("engagement_id") : 0);
        e.setCampaignId(jsonObject.has("campaign_id") ? jsonObject.getInt("campaign_id") : 0);
        e.setPlacementId(jsonObject.has("placement_id") ? jsonObject.getInt("placement_id") : 0);
        e.setPlacementName(jsonObject.has("placement_name") ? jsonObject.getString("placement_name") : "");
        e.setKey(jsonObject.has("key") ? jsonObject.getString("key") : "");

        if (jsonObject.has("reward_items")) {
            List<RewardItem> items = new ArrayList<>();
            JSONArray rewardItems = jsonObject.getJSONArray("reward_items");

            for (int i = 0; i < rewardItems.length(); i++) {
                items.add(RewardItem.fromJson(rewardItems.getString(i)));
            }

            e.setRewardItems(items);
        }

        if (jsonObject.has("social_tasks")) {
            List<SocialTask> items = new ArrayList<>();
            JSONArray socialTasks = jsonObject.getJSONArray("social_tasks");

            for (int i = 0; i < socialTasks.length(); i++) {
                items.add(SocialTask.fromJson(socialTasks.getString(i)));
            }

            e.setSocialTasks(items);
        }

        return e;
    }
}