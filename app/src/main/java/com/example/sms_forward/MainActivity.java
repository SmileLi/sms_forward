package com.example.sms_forward;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvRules;
    private RuleAdapter ruleAdapter;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private EditText etSmtpHost, etSmtpPort, etEmail, etPassword;
    private EditText etPattern;
    private SwitchMaterial swRegex;
    private SMSRule smsRule;
    private EmailConfig emailConfig;
    private EditText etForwardPhone;
    private PhoneConfig phoneConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        try {
            // 初始化手机号输入框
            etForwardPhone = findViewById(R.id.etForwardPhone);
            if (etForwardPhone == null) throw new IllegalStateException("找不到转发手机号输入框");
            
            // 保存按钮点击事件
            Button btnSavePhone = findViewById(R.id.btnSavePhone);
            if (btnSavePhone == null) throw new IllegalStateException("找不到保存按钮");
            btnSavePhone.setOnClickListener(v -> savePhoneConfig());

            // 初始化规则列表
            rvRules = findViewById(R.id.rvRules);
            if (rvRules == null) throw new IllegalStateException("找不到规则列表视图");
            
            // 设置布局管理器
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rvRules.setLayoutManager(layoutManager);
            
            // 添加分割线
            DividerItemDecoration divider = new DividerItemDecoration(this, layoutManager.getOrientation());
            rvRules.addItemDecoration(divider);
            
            // 初始化适配器
            ruleAdapter = new RuleAdapter(position -> {
                try {
                    List<SMSRule.Rule> rules = new ArrayList<>(smsRule.getRules());
                    rules.remove(position);
                    smsRule.saveRules(rules);
                    ruleAdapter.setRules(rules);
                    Toast.makeText(this, "规则已删除", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "删除规则失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            rvRules.setAdapter(ruleAdapter);

            // 添加规则相关控件
            etPattern = findViewById(R.id.etPattern);
            if (etPattern == null) throw new IllegalStateException("找不到规则输入框");
            
            swRegex = findViewById(R.id.swRegex);
            if (swRegex == null) throw new IllegalStateException("找不到正则开关");
            
            Button btnAddRule = findViewById(R.id.btnAddRule);
            if (btnAddRule == null) throw new IllegalStateException("找不到添加规则按钮");
            btnAddRule.setOnClickListener(v -> addRule());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "界面初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadSavedConfig() {
        try {
            // 加载转发手机号
            String savedPhone = phoneConfig.getForwardPhone();
            etForwardPhone.setText(savedPhone);
            
            // 加载规则列表
            List<SMSRule.Rule> rules = smsRule.getRules();
            ruleAdapter.setRules(rules);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "加载配置失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
        try {
            String pattern = etPattern.getText().toString().trim();
            if (pattern.isEmpty()) {
                Toast.makeText(this, "请输入匹配规则", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取当前规则列表
            List<SMSRule.Rule> rules = new ArrayList<>(smsRule.getRules());
            
            // 添加新规则
            SMSRule.Rule newRule = new SMSRule.Rule(pattern, swRegex.isChecked());
            rules.add(newRule);
            
            // 保存规则
            smsRule.saveRules(rules);
            
            // 更新显示
            ruleAdapter.setRules(rules);
            
            // 清空输入
            etPattern.setText("");
            swRegex.setChecked(false);
            
            // 显示成功提示
            Toast.makeText(this, "规则添加成功", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "添加规则失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
        try {
            Button btnTest = findViewById(R.id.btnTest);
            if (btnTest == null) return;
            
            btnTest.setOnClickListener(v -> sendTestSMS());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "测试按钮初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendTestSMS() {
        try {
            // 创建测试短信内容
            String sender = "+8613800138000";
            String messageBody = "测试短信内容";
            
            // 创建 PDU 格式的短信
            byte[] pduData = createPduData(sender, messageBody);
            Object[] pdus = new Object[]{pduData};
            
            // 创建广播 Intent
            Intent intent = new Intent();
            intent.setAction("android.provider.Telephony.SMS_RECEIVED");
            
            // 添加短信数据
            Bundle bundle = new Bundle();
            bundle.putSerializable("pdus", pdus);
            bundle.putString("format", "3gpp");
            intent.putExtras(bundle);
            
            // 发送广播前检查权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                // 发送广播
                sendBroadcast(intent);
                Toast.makeText(this, "已发送测试短信", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "缺少接收短信权限", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "发送测试短信失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] createPduData(String sender, String message) {
        try {
            // 这里我们简单返回一个字符串的字节数组
            // 实际的 PDU 格式更复杂，这里只是为了测试
            String pdu = String.format("00%s00%s", 
                sender.replace("+", ""),
                message);
            return pdu.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}