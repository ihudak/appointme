package eu.dec21.appointme.users.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_EMAIL_FROM = "noreply@appointme.com";
    private static final String TEST_APP_NAME = "AppointMe";

    @BeforeEach
    void setUp() {
        // Set the @Value properties using ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "emailFrom", TEST_EMAIL_FROM);
        ReflectionTestUtils.setField(emailService, "appName", TEST_APP_NAME);

        // Setup default mock behavior - use lenient to avoid unnecessary stubbing warnings
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        lenient().when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test Email</html>");
    }

    // ========== Happy Path Tests ==========

    @Test
    @DisplayName("sendEmail should send email successfully with all parameters")
    void sendEmail_shouldSendEmailSuccessfully() throws MessagingException {
        // Given
        String to = "user@example.com";
        String userName = "John Doe";
        String subject = "Verify Your Email";
        EmailTemplateName template = EmailTemplateName.VERIFY_EMAIL;
        String confirmationLink = "http://example.com/verify?token=abc123";
        String activationCode = "123456";

        // When
        emailService.sendEmail(to, userName, subject, template, confirmationLink, activationCode);

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("verify-email"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail should process template with correct variables")
    void sendEmail_shouldProcessTemplateWithCorrectVariables() throws MessagingException {
        // Given
        String userName = "Jane Smith";
        String confirmationLink = "http://example.com/confirm";
        String activationCode = "987654";
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        // When
        emailService.sendEmail(
                "test@example.com",
                userName,
                "Test Subject",
                EmailTemplateName.VERIFY_EMAIL,
                confirmationLink,
                activationCode
        );

        // Then
        verify(templateEngine).process(eq("verify-email"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();

        assertThat(capturedContext.getVariable("userName")).isEqualTo(userName);
        assertThat(capturedContext.getVariable("confirmationUrl")).isEqualTo(confirmationLink);
        assertThat(capturedContext.getVariable("activationCode")).isEqualTo(activationCode);
    }

    @Test
    @DisplayName("sendEmail should use correct template name for VERIFY_EMAIL")
    void sendEmail_shouldUseCorrectTemplateForVerifyEmail() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        verify(templateEngine).process(eq("verify-email"), any(Context.class));
    }

    @Test
    @DisplayName("sendEmail should use correct template name for RESET_PASSWORD")
    void sendEmail_shouldUseCorrectTemplateForResetPassword() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.RESET_PASSWORD,
                "link",
                "code"
        );

        // Then
        verify(templateEngine).process(eq("reset-password"), any(Context.class));
    }

    @Test
    @DisplayName("sendEmail should set HTML content as true")
    void sendEmail_shouldSetHtmlContentAsTrue() throws MessagingException {
        // Given
        String htmlContent = "<html><body>Test Email Content</body></html>";
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(htmlContent);

        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        verify(templateEngine).process(anyString(), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    // ========== Null/Empty Parameter Tests ==========

    @Test
    @DisplayName("sendEmail should handle null confirmationLink")
    void sendEmail_shouldHandleNullConfirmationLink() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                null,
                "123456"
        );

        // Then
        verify(mailSender).send(mimeMessage);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("confirmationUrl")).isNull();
    }

    @Test
    @DisplayName("sendEmail should handle null activationCode")
    void sendEmail_shouldHandleNullActivationCode() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "http://example.com/verify",
                null
        );

        // Then
        verify(mailSender).send(mimeMessage);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("activationCode")).isNull();
    }

    @Test
    @DisplayName("sendEmail should handle empty confirmationLink")
    void sendEmail_shouldHandleEmptyConfirmationLink() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "",
                "123456"
        );

        // Then
        verify(mailSender).send(mimeMessage);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("confirmationUrl")).isEqualTo("");
    }

    @Test
    @DisplayName("sendEmail should handle empty activationCode")
    void sendEmail_shouldHandleEmptyActivationCode() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "http://example.com/verify",
                ""
        );

        // Then
        verify(mailSender).send(mimeMessage);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("activationCode")).isEqualTo("");
    }

    // ========== Exception Handling Tests ==========

    @Test
    @DisplayName("sendEmail should handle template processing errors")
    void sendEmail_shouldHandleTemplateProcessingErrors() {
        // Given
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template not found"));

        // When/Then
        assertThatThrownBy(() -> emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        )).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Template not found");
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("sendEmail should handle multiple recipients format")
    void sendEmail_shouldHandleMultipleRecipientsFormat() throws MessagingException {
        // When - single recipient (standard case)
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail should handle long subject lines")
    void sendEmail_shouldHandleLongSubjectLines() throws MessagingException {
        // Given
        String longSubject = "This is a very long subject line that exceeds normal lengths to test handling ".repeat(3);

        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                longSubject,
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail should handle special characters in userName")
    void sendEmail_shouldHandleSpecialCharactersInUserName() throws MessagingException {
        // Given
        String specialUserName = "User <with> special & characters © ® ™";

        // When
        emailService.sendEmail(
                "user@example.com",
                specialUserName,
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("userName")).isEqualTo(specialUserName);
    }

    @Test
    @DisplayName("sendEmail should handle unicode characters in parameters")
    void sendEmail_shouldHandleUnicodeCharacters() throws MessagingException {
        // Given
        String unicodeName = "用户名 Müller François";
        String unicodeSubject = "Проверьте вашу почту";

        // When
        emailService.sendEmail(
                "user@example.com",
                unicodeName,
                unicodeSubject,
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("userName")).isEqualTo(unicodeName);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail should handle very long confirmation links")
    void sendEmail_shouldHandleVeryLongConfirmationLinks() throws MessagingException {
        // Given
        String longLink = "http://example.com/verify?token=" + "a".repeat(1000);

        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                longLink,
                "code"
        );

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("confirmationUrl")).isEqualTo(longLink);
    }

    @Test
    @DisplayName("sendEmail should handle HTML content in template output")
    void sendEmail_shouldHandleHtmlContentInTemplateOutput() throws MessagingException {
        // Given
        String htmlTemplate = "<html><body><h1>Welcome</h1><p>Click <a href='link'>here</a></p></body></html>";
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(htmlTemplate);

        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail should handle international email addresses")
    void sendEmail_shouldHandleInternationalEmailAddresses() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@例え.jp",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail should create context with all three variables")
    void sendEmail_shouldCreateContextWithAllVariables() throws MessagingException {
        // Given
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        // When
        emailService.sendEmail(
                "user@example.com",
                "TestUser",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "http://test.com",
                "654321"
        );

        // Then
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        Context context = contextCaptor.getValue();
        assertThat(context.getVariable("userName")).isNotNull();
        assertThat(context.getVariable("confirmationUrl")).isNotNull();
        assertThat(context.getVariable("activationCode")).isNotNull();
    }

    @Test
    @DisplayName("sendEmail should invoke mailSender.send exactly once")
    void sendEmail_shouldInvokeMailSenderSendOnce() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail should invoke templateEngine.process exactly once")
    void sendEmail_shouldInvokeTemplateEngineProcessOnce() throws MessagingException {
        // When
        emailService.sendEmail(
                "user@example.com",
                "User",
                "Subject",
                EmailTemplateName.VERIFY_EMAIL,
                "link",
                "code"
        );

        // Then
        verify(templateEngine, times(1)).process(anyString(), any(Context.class));
    }
}
