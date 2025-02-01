package com.example.sms_forward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

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

                if (smsRule.shouldForward(messageBody)) {
                    EmailConfig config = new EmailConfig(context);
                    if (config.isConfigured()) {
                        EmailSender emailSender = new EmailSender(
                            config.getSmtpHost(),
                            config.getSmtpPort(),
                            config.getEmail(),
                            config.getPassword()
                        );

                        String subject = "新短信来自: " + sender;
                        emailSender.sendEmail(subject, messageBody);
                    }
                }
            }
        }
    }
}