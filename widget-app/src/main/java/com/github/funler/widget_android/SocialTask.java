package com.github.funler.widget_android;

import org.json.JSONException;
import org.json.JSONObject;

class SocialTask {
    private int id;


    public int getId() {
        return id;
    }

    public SocialTask setId(int id) {
        this.id = id;
        return this;
    }

    public static SocialTask fromJson(String json) throws JSONException {
        SocialTask st = new SocialTask();

        JSONObject jsonObject = new JSONObject(json);
        st.setId(jsonObject.has("id") ? Integer.parseInt(jsonObject.getString("id")) : 0);

        return st;
    }
}
