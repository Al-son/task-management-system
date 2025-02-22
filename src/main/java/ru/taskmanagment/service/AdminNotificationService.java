package ru.taskmanagment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final JavaMailSender mailSender;

    /**
     * Sends an email notification to the admin.
     *
     * @param fileName The name of the uploaded file.
     */
    public void notifyAdmin(String fileName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("admin@example.com"); // Replace with the admin's email address
        message.setSubject("New File Uploaded");
        message.setText("A new file has been uploaded: " + fileName);
        mailSender.send(message);
    }
}