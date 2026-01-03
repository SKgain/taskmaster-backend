package com.dhrubok.taskmaster.common.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Generic method to send HTML email with placeholders
    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, String> placeholders)
            throws MessagingException, IOException {

        ClassPathResource resource = new ClassPathResource("templates/" + templateName);
        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            template = template.replace(entry.getKey(), entry.getValue());
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(template, true);

        mailSender.send(message);
    }

    // Verification Email
    public void sendVerificationEmail(String recipientEmail, String verificationUrl)
            throws MessagingException, IOException {

        sendHtmlEmail(
                recipientEmail,
                "TaskMaster Account Verification",
                "verification-email.html",
                Map.of("{{VERIFICATION_URL}}", verificationUrl)
        );
    }

    // Password Reset Email
    public void sendPasswordResetEmail(String recipientEmail, String resetUrl)
            throws MessagingException, IOException {

        sendHtmlEmail(
                recipientEmail,
                "TaskMaster Password Reset",
                "password-reset-email.html",
                Map.of("{{RESET_URL}}", resetUrl)
        );
    }

    // Welcome Email
    public void sendWelcomeEmail(String recipientEmail, String dashboardUrl)
            throws MessagingException, IOException {

        sendHtmlEmail(
                recipientEmail,
                "Welcome to TaskMaster",
                "welcome-email.html",
                Map.of("{{DASHBOARD_URL}}", dashboardUrl)
        );
    }

    // Task Assigned Email
    public void sendTaskAssignedEmail(String recipientEmail,
                                      String taskName,
                                      String dueDate,
                                      String assignedBy,
                                      String taskUrl) throws MessagingException, IOException {

        sendHtmlEmail(
                recipientEmail,
                "New Task Assigned",
                "task-assigned-email.html",
                Map.of(
                        "{{TASK_NAME}}", taskName,
                        "{{DUE_DATE}}", dueDate,
                        "{{ASSIGNED_BY}}", assignedBy,
                        "{{TASK_URL}}", taskUrl
                )
        );
    }

    // Notification Email
    public void sendNotificationEmail(String recipientEmail, String message, String actionUrl)
            throws MessagingException, IOException {

        sendHtmlEmail(
                recipientEmail,
                "TaskMaster Notification",
                "notification-email.html",
                Map.of(
                        "{{MESSAGE}}", message,
                        "{{ACTION_URL}}", actionUrl
                )
        );
    }

    public void sendMemberWelcomeEmail(String email, String fullName, String generatedPassword, String verificationUrl)
            throws MessagingException, IOException {

        sendHtmlEmail(
                email,
                "🎉 Welcome to TaskMaster - Your Account Details",
                "member-welcome-email.html",
                Map.of(
                        "{{FULL_NAME}}", fullName,
                        "{{EMAIL}}", email,
                        "{{PASSWORD}}", generatedPassword,
                        "{{VERIFICATION_URL}}", verificationUrl
                )
        );
    }

    // Meeting Invitation Email
    public void sendMeetingInvitationEmail(String recipientEmail,
                                           String title,
                                           String scheduledTime,
                                           String duration,
                                           String organizer,
                                           String locationOrLink,
                                           String joinLink) throws MessagingException, IOException {

        sendHtmlEmail(
                recipientEmail,
                "Invitation: " + title,
                "meeting-invitation.html", // Make sure file is in src/main/resources/templates/
                Map.of(
                        "{{MEETING_TITLE}}", title,
                        "{{SCHEDULED_TIME}}", scheduledTime,
                        "{{DURATION}}", duration,
                        "{{ORGANIZER}}", organizer,
                        "{{LOCATION_OR_LINK}}", locationOrLink,
                        "{{JOIN_LINK}}", joinLink
                )
        );
    }
}
