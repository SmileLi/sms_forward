package com.example.sms_forward;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class SMSRule {
    private static final String PREF_NAME = "SMSRules";
    private static final String KEY_RULES = "rules";
    private final SharedPreferences preferences;

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
            if (isRegex) {
                return message.matches(pattern);
            } else {
                return message.contains(pattern);
            }
        }
    }

    public SMSRule(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveRules(List<Rule> rules) {
        JSONArray jsonArray = new JSONArray();
        for (Rule rule : rules) {
            JSONArray ruleArray = new JSONArray();
            ruleArray.put(rule.getPattern());
            ruleArray.put(rule.isRegex());
            jsonArray.put(ruleArray);
        }
        preferences.edit().putString(KEY_RULES, jsonArray.toString()).apply();
    }

    public List<Rule> getRules() {
        List<Rule> rules = new ArrayList<>();
        String rulesJson = preferences.getString(KEY_RULES, "[]");
        try {
            JSONArray jsonArray = new JSONArray(rulesJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray ruleArray = jsonArray.getJSONArray(i);
                String pattern = ruleArray.getString(0);
                boolean isRegex = ruleArray.getBoolean(1);
                rules.add(new Rule(pattern, isRegex));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rules;
    }

    public boolean shouldForward(String message) {
        List<Rule> rules = getRules();
        for (Rule rule : rules) {
            if (rule.matches(message)) {
                return true;
            }
        }
        return false;
    }
}