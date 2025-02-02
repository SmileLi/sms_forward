package com.example.sms_forward;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null || intent.getAction() == null) {
                showNotification(context, "接收异常", "intent 或 action 为空");
                return;
            }
            
            // 记录接收到的广播类型
            showNotification(context, "收到广播", 
                String.format("Action: %s\n时间: %s", 
                    intent.getAction(),
                    new java.util.Date().toString()));

            if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    showNotification(context, "接收异常", "bundle 为空");
                    return;
                }
                
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus == null || pdus.length == 0) {
                    showNotification(context, "接收异常", "pdus 为空或长度为0");
                    return;
                }
                
                String format = bundle.getString("format", "3gpp");
                StringBuilder fullMessage = new StringBuilder();
                String sender = null;
                
                // 记录 PDU 数量
                showNotification(context, "PDU信息", 
                    String.format("PDU数量: %d\n格式: %s", 
                        pdus.length, format));
                
                for (Object pdu : pdus) {
                    try {
                        SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu, format);
                        if (message == null) {
                            showNotification(context, "PDU解析失败", "message 为空");
                            continue;
                        }
                        
                        if (sender == null) {
                            sender = message.getDisplayOriginatingAddress();
                            String originatingAddress = message.getOriginatingAddress();
                            
                            // 记录更详细的短信信息
                            showNotification(context, "短信详细信息",
                                String.format("显示号码: %s\n" +
                                    "原始号码: %s\n" +
                                    "消息类型: %s\n" +
                                    "时间戳: %s\n" +
                                    "协议标识: %d\n" +
                                    "编码方案: %d\n" +
                                    "服务中心: %s",
                                    sender,
                                    originatingAddress,
                                    message.getMessageClass(),
                                    new java.util.Date(message.getTimestampMillis()),
                                    message.getProtocolIdentifier(),
                                    message.getStatusOnIcc(),
                                    message.getServiceCenterAddress()));
                        }
                        
                        String messageBody = message.getMessageBody();
                        fullMessage.append(messageBody);
                        
                        // 记录每个 PDU 的内容
                        showNotification(context, "PDU内容", 
                            String.format("PDU序号: %d\n内容: %s", 
                                fullMessage.length(), messageBody));
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        showNotification(context, "PDU处理异常", e.getMessage());
                    }
                }
                
                if (sender != null && fullMessage.length() > 0) {
                    // 记录最终组装的短信内容
                    showNotification(context, "完整短信", 
                        String.format("发送者: %s\n内容: %s\n长度: %d", 
                            sender, 
                            fullMessage.toString(),
                            fullMessage.length()));
                            
                    processMessage(context, sender, fullMessage.toString());
                } else {
                    showNotification(context, "处理失败", 
                        String.format("sender: %s, message length: %d", 
                            sender, 
                            fullMessage.length()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification(context, "广播接收异常", e.getMessage());
        }
    }

    public void processMessage(Context context, String sender, String messageBody) {
        try {
            SMSRule smsRule = new SMSRule(context);
            PhoneConfig phoneConfig = new PhoneConfig(context);

            showNotification(context, "收到短信", 
                String.format("发送者：[%s]\n内容：%s", sender, messageBody));

            if (smsRule.shouldForward(messageBody) && phoneConfig.isConfigured()) {
                SmsManager smsManager = SmsManager.getDefault();
                String forwardMessage = String.format("来自 %s 的短信：%s", sender, messageBody);
                
                ArrayList<String> parts = smsManager.divideMessage(forwardMessage);
                for (String part : parts) {
                    smsManager.sendTextMessage(
                        phoneConfig.getForwardPhone(),
                        null,
                        part,
                        null,
                        null
                    );
                }
                
                showNotification(context, "短信已转发", forwardMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification(context, "转发失败", e.getMessage());
        }
    }

    private void showNotification(Context context, String title, String message) {
        try {
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager == null) return;
            
            String channelId = "sms_forward_notification";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "SMS Forward Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(channel);
            }
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
            
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}