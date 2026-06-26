package com.farmily.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// 負責實際寄信
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // 寄件者信箱（用 application.properties 設定的帳號）
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 寄出 Email 驗證信
    // @Async：另開執行緒寄信，不要卡住註冊的回應
    @Async
    public void sendVerifyEmail(String toEmail, String verifyLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily 帳號 Email 驗證");
        message.setText("您好，\n\n"
                + "請點擊以下連結完成 Email 驗證（連結 24 小時內有效）：\n"
                + verifyLink + "\n\n"
                + "若您沒有註冊本平台，請忽略此信。");

        mailSender.send(message);
    }

    // 寄出重設密碼信
    @Async
    public void sendResetPasswordEmail(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily 重設密碼");
        message.setText("您好，\n\n"
                + "請點擊以下連結重設您的密碼（連結 30 分鐘內有效）：\n"
                + resetLink + "\n\n"
                + "若您沒有申請重設密碼，請忽略此信，您的密碼不會被變更。");

        mailSender.send(message);
    }
}