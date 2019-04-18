package io.cere.rewards_module.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardItem {
    private int id;
    private List<Map<String, String>> params = Collections.EMPTY_LIST;

    public int getId() {
        return id;
    }

    public RewardItem setId(int id) {
        this.id = id;
        return this;
    }

    public List<Map<String, String>> getParams() {
        return params;
    }

    public RewardItem setParams(List<Map<String, String>> params) {
        this.params = params;
        return this;
    }

    public static RewardItem fromJson(String json) throws JSONException {
        RewardItem ri = new RewardItem();

        JSONObject jsonObject = new JSONObject(json);
        ri.setId(jsonObject.has("id") ? Integer.parseInt(jsonObject.getString("id")) : 0);

        if (jsonObject.has("params")) {
            List<Map<String, String>> paramsList = new ArrayList<>();

            JSONArray params = jsonObject.getJSONArray("params");
            for (int i = 0; i < params.length(); i++) {
                Map<String, String> param = new HashMap<>();
                JSONObject p = params.getJSONObject(i);
                String key = p.keys().next();
                param.put(key, p.getString(key));
                paramsList.add(param);
            }

            ri.setParams(paramsList);
        }

        return ri;
    }
}
