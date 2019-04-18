package io.cere.rewards_module.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class User {

    private String id;
    private String email;
    private String token;
    private String password;
    private Map<String, String> extras = Collections.EMPTY_MAP;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getToken() {
        return token;
    }

    public User setToken(String token) {
        this.token = token;
        return this;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public User setExtras(Map<String, String> extras) {
        this.extras = extras;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONObject extrasJson = new JSONObject();

        try {
            json.put("id", getId());
            json.put("email", this.getEmail());
            json.put("token", this.getToken());
            json.put("password", this.getPassword());
            if (!this.getExtras().isEmpty()) {
                for (String key : this.getExtras().keySet()) {
                    extrasJson.put(key, this.getExtras().get(key));
                }
            }
            json.put("extras", extrasJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static User fromJson(String json) throws JSONException {
        User user = new User();

        JSONObject jsonObject = new JSONObject(json);
        user.setId(jsonObject.optString("id"));
        user.setEmail(jsonObject.getString("email"));
        user.setToken(jsonObject.getString("token"));
        user.setPassword(jsonObject.optString("password"));
        user.setExtras(prepareExtras(jsonObject));

        return user;
    }

    @Override
    public String toString() {
        return "{id=" + getId() +
                ", email=" + getEmail() +
                ", token=" + getToken() +
                ", extras=" + getExtras() +
                "}";
    }

    private static Map<String, String> prepareExtras(JSONObject jsonObject) throws JSONException {
        Map<String, String> extras = Collections.EMPTY_MAP;

        if (jsonObject.has("extras")) {
            JSONObject extrasJson = jsonObject.getJSONObject("extras");
            extras = new HashMap<>();

            Iterator<String> iterator = extrasJson.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                extras.put(key, extrasJson.getString(key));
            }
        }

        return extras;
    }
}
