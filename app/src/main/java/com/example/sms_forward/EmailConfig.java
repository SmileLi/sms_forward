package com.example.sms_forward;

import android.content.Context;
import android.content.SharedPreferences;

public class EmailConfig {
    private static final String PREF_NAME = "EmailConfig";
    private static final String KEY_SMTP_HOST = "smtp_host";
    private static final String KEY_SMTP_PORT = "smtp_port";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    private final SharedPreferences preferences;

    public EmailConfig(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveConfig(String smtpHost, int smtpPort, String email, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_SMTP_HOST, smtpHost);
        editor.putInt(KEY_SMTP_PORT, smtpPort);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public String getSmtpHost() {
        return preferences.getString(KEY_SMTP_HOST, "");
    }

    public int getSmtpPort() {
        return preferences.getInt(KEY_SMTP_PORT, 587);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getPassword() {
        return preferences.getString(KEY_PASSWORD, "");
    }

    public boolean isConfigured() {
        return !getEmail().isEmpty() && !getPassword().isEmpty() && !getSmtpHost().isEmpty();
    }
}