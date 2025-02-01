package com.example.sms_forward;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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
    private EditText etForwardPhone;
    private PhoneConfig phoneConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化配置
        phoneConfig = new PhoneConfig(this);
        smsRule = new SMSRule(this);

        // 初始化视图
        initViews();
        
        // 加载已保存的配置
        loadSavedConfig();

        // 请求权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.FOREGROUND_SERVICE
                },
                PERMISSION_REQUEST_CODE);
        } else {
            startForwardService();
        }

        setupTestButton();
    }

    private void initViews() {
        // 初始化手机号输入框
        etForwardPhone = findViewById(R.id.etForwardPhone);
        
        // 保存按钮点击事件
        Button btnSavePhone = findViewById(R.id.btnSavePhone);
        btnSavePhone.setOnClickListener(v -> savePhoneConfig());

        // 初始化规则列表
        rvRules = findViewById(R.id.rvRules);
        rvRules.setLayoutManager(new LinearLayoutManager(this));
        ruleAdapter = new RuleAdapter(position -> {
            // 删除规则的处理
            List<SMSRule.Rule> rules = smsRule.getRules();
            rules.remove(position);
            smsRule.saveRules(rules);
            ruleAdapter.setRules(rules);
        });
        rvRules.setAdapter(ruleAdapter);

        // 添加规则按钮点击事件
        etPattern = findViewById(R.id.etPattern);
        swRegex = findViewById(R.id.swRegex);
        Button btnAddRule = findViewById(R.id.btnAddRule);
        btnAddRule.setOnClickListener(v -> addRule());
    }

    private void loadSavedConfig() {
        // 加载转发手机号
        etForwardPhone.setText(phoneConfig.getForwardPhone());
        
        // 加载规则列表
        ruleAdapter.setRules(smsRule.getRules());
    }

    private void savePhoneConfig() {
        String phone = etForwardPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "请输入转发手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        phoneConfig.saveForwardPhone(phone);
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    }

    private void addRule() {
        String pattern = etPattern.getText().toString().trim();
        if (pattern.isEmpty()) {
            Toast.makeText(this, "请输入匹配规则", Toast.LENGTH_SHORT).show();
            return;
        }

        List<SMSRule.Rule> rules = smsRule.getRules();
        rules.add(new SMSRule.Rule(pattern, swRegex.isChecked()));
        smsRule.saveRules(rules);
        ruleAdapter.setRules(rules);
        
        // 清空输入
        etPattern.setText("");
        swRegex.setChecked(false);
    }

    private void startForwardService() {
        Intent serviceIntent = new Intent(this, SMSForwardService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
                startForwardService();
            } else {
                Toast.makeText(this, "需要相关权限才能转发短信", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupTestButton() {
        Button btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(v -> {
            // 模拟接收短信
            Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
            Bundle bundle = new Bundle();
            Object[] pdus = new Object[1];
            byte[] pdu = createTestPdu();  // 创建测试短信的PDU
            pdus[0] = pdu;
            bundle.putSerializable("pdus", pdus);
            bundle.putString("format", "3gpp");
            intent.putExtras(bundle);
            sendBroadcast(intent);
        });
    }

    private byte[] createTestPdu() {
        // 这里简化处理，实际PDU格式更复杂
        String sender = "+8613800138000";
        String message = "测试短信";
        return (sender + "," + message).getBytes();
    }
}