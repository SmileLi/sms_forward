package com.example.sms_forward;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvRules;
    private RuleAdapter ruleAdapter;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private EditText etSmtpHost, etSmtpPort, etEmail, etPassword;
    private EditText etPattern;
    private Switch swRegex;
    private SMSRule smsRule;
    private EmailConfig emailConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}