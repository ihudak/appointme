package eu.dec21.appointme.users.email;

import jakarta.annotation.Nonnull;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
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
    ) throws MessagingException {
        String templateName = template.getName();
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

        helper.setText(html, true);
        mailSender.send(mailMessage.getMimeMessage());
    }
}
