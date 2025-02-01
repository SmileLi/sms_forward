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
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        preferences.edit()
            .putString(KEY_FORWARD_PHONE, phone.trim())
            .apply();
    }

    public String getForwardPhone() {
        return preferences.getString(KEY_FORWARD_PHONE, "");
    }

    public boolean isConfigured() {
        String phone = getForwardPhone();
        return phone != null && !phone.trim().isEmpty() && phone.matches("^1\\d{10}$");
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
} 