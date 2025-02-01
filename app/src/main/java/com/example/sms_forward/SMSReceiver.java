package com.example.sms_forward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages != null && messages.length > 0) {
                StringBuilder fullMessage = new StringBuilder();
                String sender = messages[0].getDisplayOriginatingAddress();

                for (SmsMessage message : messages) {
                    fullMessage.append(message.getMessageBody());
                }

                String messageBody = fullMessage.toString();
                SMSRule smsRule = new SMSRule(context);
                PhoneConfig phoneConfig = new PhoneConfig(context);

                if (smsRule.shouldForward(messageBody) && phoneConfig.isConfigured()) {
                    // 发送短信
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(
                        phoneConfig.getForwardPhone(),
                        null,
                        "来自 " + sender + " 的短信：" + messageBody,
                        null,
                        null
                    );
                }
            }
        }
    }
}