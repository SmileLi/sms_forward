package com.example.sms_forward;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SMSRule {
    private static final String PREF_NAME = "SMSRules";
    private static final String KEY_RULES = "rules";
    private final SharedPreferences preferences;

    public SMSRule(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public List<Rule> getRules() {
        String rulesJson = preferences.getString(KEY_RULES, "[]");
        List<Rule> rules = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(rulesJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                rules.add(new Rule(
                    obj.getString("pattern"),
                    obj.getBoolean("isRegex")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rules;
    }

    public void saveRules(List<Rule> rules) {
        JSONArray array = new JSONArray();
        for (Rule rule : rules) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("pattern", rule.getPattern());
                obj.put("isRegex", rule.isRegex());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        preferences.edit().putString(KEY_RULES, array.toString()).apply();
    }

    public boolean shouldForward(String message) {
        for (Rule rule : getRules()) {
            if (rule.matches(message)) {
                return true;
            }
        }
        return false;
    }

    public static class Rule {
        private final String pattern;
        private final boolean isRegex;

        public Rule(String pattern, boolean isRegex) {
            this.pattern = pattern;
            this.isRegex = isRegex;
        }

        public String getPattern() {
            return pattern;
        }

        public boolean isRegex() {
            return isRegex;
        }

        public boolean matches(String message) {
            if (message == null || pattern == null) return false;
            try {
                if (isRegex) {
                    return java.util.regex.Pattern.compile(pattern)
                        .matcher(message)
                        .find();
                } else {
                    return message.contains(pattern);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}