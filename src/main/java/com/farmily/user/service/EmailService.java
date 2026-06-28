package com.farmily.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// 負責處理信件內容 (驗證+重設密碼)
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // 寄件者信箱（用 application.properties 設定的帳號）
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 寄出 Email 驗證信 (會員)
    // @Async：另開執行緒寄信，不要卡住註冊的回應
    @Async
    public void sendVerifyEmail(String toEmail, String verifyLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily 帳號 - Email 驗證");
        message.setText("您好，\n\n"
                + "請點擊以下連結完成 Email 驗證（連結 24 小時內有效）：\n"
                + verifyLink);

        mailSender.send(message);
    }

    // 寄出小農啟用驗證信件
    @Async
    public void sendFarmerVerifyEmail(String toEmail, String verifyLink){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily 小農帳號啟用通知");
        message.setText("您好，\n\n"
                + "您申請的小農帳號已審核通過！\n"
                + "請點擊以下連結，驗證並啟用小農帳號（連結 24 小時內有效）：\n"
                + verifyLink);

        mailSender.send(message);
    }

    // 寄出重設密碼信 (會員/小農)
    @Async
    public void sendResetPasswordEmail(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily - 重設密碼");
        message.setText("您好，\n\n"
                + "請點擊以下連結重設您的密碼（連結 30 分鐘內有效）：\n"
                + resetLink);

        mailSender.send(message);
    }

    // 寄出「密碼已變更」純通知信（會員/小農）
    @Async
    public void sendPasswordChangedNotice(String toEmail) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily - 密碼變更通知");
        message.setText("您好，\n\n"
                + "您的 Farmily 帳號密碼剛剛已被變更，若為本人操作，請忽略此信。\n"
                + "若非本人操作，您的帳號可能有風險，請立即聯繫客服：supportfarmily@gmail.com\n");

        mailSender.send(message);
    }
}