package com.example.sms_forward;

import android.content.Context;
import android.content.SharedPreferences;

public class PhoneConfig {
    private static final String PREF_NAME = "PhoneConfig";
    private static final String KEY_FORWARD_PHONE = "forward_phone";
    
    private final SharedPreferences preferences;

    public PhoneConfig(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveForwardPhone(String phone) {
        preferences.edit().putString(KEY_FORWARD_PHONE, phone).apply();
    }

    public String getForwardPhone() {
        return preferences.getString(KEY_FORWARD_PHONE, "");
    }

    public boolean isConfigured() {
        return !getForwardPhone().isEmpty();
    }
} 