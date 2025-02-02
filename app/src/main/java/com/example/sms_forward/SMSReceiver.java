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
            if (intent == null || intent.getAction() == null) return;
            
            if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                Bundle bundle = intent.getExtras();
                if (bundle == null) return;
                
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus == null || pdus.length == 0) return;
                
                String format = bundle.getString("format", "3gpp");
                StringBuilder fullMessage = new StringBuilder();
                String sender = null;
                
                for (Object pdu : pdus) {
                    try {
                        SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu, format);
                        if (message == null) continue;
                        
                        if (sender == null) {
                            sender = message.getDisplayOriginatingAddress();
                            String originatingAddress = message.getOriginatingAddress();
                            
                            showNotification(context, "短信号码信息",
                                String.format("显示号码：%s\n原始号码：%s",
                                    sender,
                                    originatingAddress));
                        }
                        fullMessage.append(message.getMessageBody());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (sender != null && fullMessage.length() > 0) {
                    processMessage(context, sender, fullMessage.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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