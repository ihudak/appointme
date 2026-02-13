package eu.dec21.appointme.users.email;

import jakarta.annotation.Nonnull;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${application.email.no-reply}")
    private String emailFrom;

    @Value("${application.name}")
    private String appName;

    @Async
    public void sendEmail(
            @Nonnull String to,
            @Nonnull String userName,
            @Nonnull String subject,
            @Nonnull EmailTemplateName template,
            String confirmationLink,
            String activationCode
    ) {
        log.info("📧 Starting async email send to: {}", to);
        try {
            String templateName = template.getName();
            log.debug("Using template: {}", templateName);
            
            MimeMailMessage mailMessage = new MimeMailMessage(mailSender.createMimeMessage());
            MimeMessageHelper helper = new MimeMessageHelper(
                    mailMessage.getMimeMessage(),
                    MULTIPART_MODE_MIXED,
                    UTF_8.name()
            );
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("confirmationUrl", confirmationLink);
            variables.put("activationCode", activationCode);

            Context context = new Context();
            context.setVariables(variables);

            try {
                helper.setFrom(emailFrom, appName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(emailFrom);
            }

            helper.setTo(to);
            helper.setSubject(subject);

            String html = templateEngine.process(templateName, context);
            log.debug("Template rendered successfully");

            helper.setText(html, true);
            
            log.info("📤 Sending email via SMTP...");
            mailSender.send(mailMessage.getMimeMessage());
            log.info("✅ Email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("❌ Failed to send email to: {}", to, e);
            // Log the error - async methods can't propagate checked exceptions
            throw new RuntimeException("Failed to send email to: " + to, e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email to: {}", to, e);
            throw new RuntimeException("Unexpected error sending email to: " + to, e);
        }
    }
}
