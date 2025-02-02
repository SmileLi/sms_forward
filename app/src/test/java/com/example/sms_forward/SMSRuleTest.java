package com.example.sms_forward;

import org.junit.Test;
import static org.junit.Assert.*;

public class SMSRuleTest {

    @Test
    public void testRegexMatching() {
        // 正则表达式测试用例
        SMSRule.Rule rule;
        
        // 测试：支付宝.*登录
        rule = new SMSRule.Rule("支付宝.*登录", true);
        assertTrue("应该匹配：【支付宝】登录验证码", 
            rule.matches("【支付宝】登录验证码"));
        assertTrue("应该匹配：支付宝账号登录", 
            rule.matches("支付宝账号登录"));
        assertFalse("不应匹配：支付宝余额", 
            rule.matches("支付宝余额"));

        // 测试：.*验证码.*
        rule = new SMSRule.Rule(".*验证码.*", true);
        assertTrue("应该匹配：您的验证码是1234", 
            rule.matches("您的验证码是1234"));
        assertTrue("应该匹配：验证码：1234", 
            rule.matches("验证码：1234"));
        assertFalse("不应匹配：您的密码是1234", 
            rule.matches("您的密码是1234"));

        // 测试特殊字符
        rule = new SMSRule.Rule("\\【支付宝\\】.*", true);
        assertTrue("应该匹配：【支付宝】登录验证码", 
            rule.matches("【支付宝】登录验证码"));
        assertFalse("不应匹配：支付宝登录验证码", 
            rule.matches("支付宝登录验证码"));
    }

    @Test
    public void testKeywordMatching() {
        // 关键词匹配测试用例
        SMSRule.Rule rule;
        
        // 测试普通关键词
        rule = new SMSRule.Rule("支付宝", false);
        assertTrue("应该匹配：【支付宝】登录验证码", 
            rule.matches("【支付宝】登录验证码"));
        assertTrue("应该匹配：您的支付宝账号", 
            rule.matches("您的支付宝账号"));
        assertFalse("不应匹配：支付账号", 
            rule.matches("支付账号"));

        // 测试带特殊字符的关键词
        rule = new SMSRule.Rule("【支付宝】", false);
        assertTrue("应该匹配：【支付宝】登录验证码", 
            rule.matches("【支付宝】登录验证码"));
        assertFalse("不应匹配：支付宝登录验证码", 
            rule.matches("支付宝登录验证码"));

        // 测试空格处理
        rule = new SMSRule.Rule("验证 码", false);
        assertTrue("应该匹配：验证 码是1234", 
            rule.matches("验证 码是1234"));
        assertFalse("不应匹配：验证码是1234", 
            rule.matches("验证码是1234"));
    }

    @Test
    public void testEdgeCases() {
        // 边界情况测试
        SMSRule.Rule rule;
        
        // 测试空字符串
        rule = new SMSRule.Rule("", false);
        assertTrue("空关键词应该匹配任何内容", 
            rule.matches("任何内容"));

        // 测试特殊正则表达式
        rule = new SMSRule.Rule(".*", true);
        assertTrue(".*应该匹配任何内容", 
            rule.matches("任何内容"));

        // 测试无效的正则表达式
        rule = new SMSRule.Rule("[invalid", true);
        assertFalse("无效的正则表达式应该返回false", 
            rule.matches("任何内容"));

        // 测试null值
        rule = new SMSRule.Rule("test", false);
        assertFalse("null消息应该返回false", 
            rule.matches(null));
    }

    @Test
    public void testPartialMatching() {
        // 部分匹配测试用例
        SMSRule.Rule rule;
        
        // 正则表达式部分匹配
        rule = new SMSRule.Rule("支付宝.*登录", true);
        assertTrue("应该匹配：前缀内容【支付宝】登录验证码后缀内容", 
            rule.matches("前缀内容【支付宝】登录验证码后缀内容"));
        
        rule = new SMSRule.Rule("验证码\\d+", true);
        assertTrue("应该匹配：您的验证码123456请勿泄露", 
            rule.matches("您的验证码123456请勿泄露"));
        assertTrue("应该匹配：验证码123456", 
            rule.matches("验证码123456"));
        
        // 关键词部分匹配
        rule = new SMSRule.Rule("支付宝", false);
        assertTrue("应该匹配：这是一条支付宝消息", 
            rule.matches("这是一条支付宝消息"));
        assertTrue("应该匹配：【支付宝】消息", 
            rule.matches("【支付宝】消息"));
    }

    @Test
    public void testRealWorldExamples() {
        // 实际场景测试用例
        SMSRule.Rule rule;
        
        // 验证码场景
        rule = new SMSRule.Rule("验证码.*\\d+", true);
        assertTrue(rule.matches("您的验证码是123456，请勿泄露"));
        assertTrue(rule.matches("验证码：123456"));
        assertFalse(rule.matches("您的验证码即将发送"));

        // 银行短信场景
        rule = new SMSRule.Rule("银行.*支付", true);
        assertTrue(rule.matches("【工商银行】您尾号2345的账户支付100元"));
        assertFalse(rule.matches("【工商银行】您的账户余额100元"));

        // 组合场景
        rule = new SMSRule.Rule("支付宝|微信", true);
        assertTrue(rule.matches("【支付宝】支付成功100元"));
        assertTrue(rule.matches("【微信】支付成功100元"));
        assertFalse(rule.matches("【银行】支付成功100元"));
    }

    @Test
    public void testExactPhraseMatching() {
        SMSRule.Rule rule;
        
        // 测试完整短语匹配
        rule = new SMSRule.Rule("【支付宝】登录验证码", false);
        assertTrue("应该匹配完整短语",
            rule.matches("【支付宝】登录验证码：123456"));
        assertTrue("应该匹配带前缀后缀的短语",
            rule.matches("您的【支付宝】登录验证码：123456，请勿泄露"));
        assertFalse("不应匹配部分短语",
            rule.matches("【支付宝】登录"));

        // 测试带特殊字符的短语
        rule = new SMSRule.Rule("验证码：", false);
        assertTrue("应该匹配带冒号的验证码",
            rule.matches("您的验证码：123456"));
        
        // 测试带空格的短语
        rule = new SMSRule.Rule("您的 验证码", false);
        assertTrue("应该匹配带空格的短语",
            rule.matches("您的 验证码是：123456"));
    }
} 