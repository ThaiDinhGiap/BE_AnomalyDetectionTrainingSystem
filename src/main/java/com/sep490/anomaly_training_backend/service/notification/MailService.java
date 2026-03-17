package com.sep490.anomaly_training_backend.service.notification;

public interface MailService {
    void sendSimpleMail(String to, String subject, String body);

    void sendHtmlMail(String to, String subject, String htmlBody);

    void sendMailWithCc(String to, String[] cc, String subject, String body);
}
