package eu.dec21.appointme.users.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("EmailService Integration Tests with MailDev")
@Import(AsyncTestConfig.class)
class EmailServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.2-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> maildev = new GenericContainer<>(DockerImageName.parse("maildev/maildev:latest"))
            .withExposedPorts(1080, 1025)  // Web UI on 1080, SMTP on 1025
            .waitingFor(Wait.forHttp("/").forPort(1080).forStatusCode(200));

    @Autowired
    private EmailService emailService;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String maildevApiUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Mail properties - point to MailDev container
        registry.add("spring.mail.host", maildev::getHost);
        registry.add("spring.mail.port", () -> maildev.getMappedPort(1025));
        registry.add("spring.mail.username", () -> "");
        registry.add("spring.mail.password", () -> "");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> false);
        registry.add("spring.mail.properties.mail.smtp.starttls.enabled", () -> false);
        
        // Application properties required by EmailService
        registry.add("application.email.no-reply", () -> "noreply@appointme-test.com");
        registry.add("application.name", () -> "AppointMe");
    }

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        objectMapper = new ObjectMapper();
        maildevApiUrl = String.format("http://%s:%d/email",
                maildev.getHost(),
                maildev.getMappedPort(1080));

        // Clear all emails before each test (ignore errors if no emails exist)
        try {
            deleteAllEmails();
        } catch (Exception e) {
            // Ignore - mailbox might already be empty
        }
    }

    // ========== Happy Path Tests ==========

    @Test
    @DisplayName("Should send email successfully and verify it arrives in MailDev")
    void sendEmail_shouldArriveInMailDev() throws Exception {
        // Given
        String to = "test@example.com";
        String userName = "John Doe";
        String subject = "Test Email";
        EmailTemplateName template = EmailTemplateName.VERIFY_EMAIL;
        String confirmationLink = "http://localhost/activate?token=abc123";
        String activationCode = "123456";

        // When
        emailService.sendEmail(to, userName, subject, template, confirmationLink, activationCode);

        // Then - Wait for async email to arrive (max 10 seconds)
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).isNotEmpty();
                    assertThat(emails.size()).isEqualTo(1);

                    JsonNode email = emails.get(0);
                    assertThat(email.get("subject").asText()).isEqualTo(subject);
                    assertThat(email.get("to").get(0).get("address").asText()).isEqualTo(to);
                    assertThat(email.get("from").get(0).get("name").asText()).isEqualTo("AppointMe");
                });
    }

    @Test
    @DisplayName("Should send email with HTML content rendered from template")
    void sendEmail_shouldContainRenderedHtmlTemplate() throws Exception {
        // Given
        String to = "user@test.com";
        String userName = "Jane Smith";
        String subject = "Activate Your Account";
        EmailTemplateName template = EmailTemplateName.VERIFY_EMAIL;
        String confirmationLink = "http://localhost/confirm";
        String activationCode = "999888";

        // When
        emailService.sendEmail(to, userName, subject, template, confirmationLink, activationCode);

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);

                    JsonNode email = emails.get(0);
                    String html = email.get("html").asText();

                    // Verify template variables were substituted
                    assertThat(html).contains(userName);
                    assertThat(html).contains(confirmationLink);
                    assertThat(html).contains(activationCode);
                    assertThat(html).contains("<html"); // Is HTML
                });
    }

    @Test
    @DisplayName("Should send multiple emails asynchronously")
    void sendEmail_shouldSendMultipleEmailsAsync() throws Exception {
        // Given
        int emailCount = 3;

        // When - Send multiple emails
        for (int i = 0; i < emailCount; i++) {
            emailService.sendEmail(
                    "user" + i + "@test.com",
                    "User " + i,
                    "Subject " + i,
                    EmailTemplateName.VERIFY_EMAIL,
                    "http://link" + i,
                    "code" + i
            );
        }

        // Then - All emails should arrive
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(emailCount);
                });
    }

    // ========== Character Encoding Tests ==========

    @Test
    @DisplayName("Should handle Unicode characters in email content")
    void sendEmail_shouldHandleUnicodeCharacters() throws Exception {
        // Given
        String to = "unicode@test.com";
        String userName = "测试用户 🎉"; // Chinese + emoji
        String subject = "Тест Тема"; // Cyrillic
        String confirmationLink = "http://localhost/activate";
        String activationCode = "áéíóú"; // Accented characters

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, confirmationLink, activationCode);

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);

                    JsonNode email = emails.get(0);
                    assertThat(email.get("subject").asText()).isEqualTo(subject);
                    
                    String html = email.get("html").asText();
                    assertThat(html).contains(userName);
                    assertThat(html).contains(activationCode);
                });
    }

    @Test
    @DisplayName("Should handle international email addresses")
    void sendEmail_shouldHandleInternationalEmailAddresses() throws Exception {
        // Given - International domain
        String to = "user@münchen.de";
        String userName = "Franz Müller";
        String subject = "Test";

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, "http://link", "code");

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                });
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Should handle very long subject line")
    void sendEmail_shouldHandleLongSubject() throws Exception {
        // Given
        String to = "test@example.com";
        String userName = "Test User";
        String subject = "A".repeat(200); // Very long subject

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, "http://link", "code");

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                    assertThat(emails.get(0).get("subject").asText()).isEqualTo(subject);
                });
    }

    @Test
    @DisplayName("Should handle very long username in template")
    void sendEmail_shouldHandleLongUsername() throws Exception {
        // Given
        String to = "test@example.com";
        String userName = "VeryLongUsername".repeat(20); // 320 characters
        String subject = "Test";

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, "http://link", "code");

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                    
                    String html = emails.get(0).get("html").asText();
                    assertThat(html).contains(userName);
                });
    }

    @Test
    @DisplayName("Should handle special characters in subject")
    void sendEmail_shouldHandleSpecialCharactersInSubject() throws Exception {
        // Given
        String to = "test@example.com";
        String userName = "Test User";
        String subject = "Test <>&\"'@#$%^&*()";

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, "http://link", "code");

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                    assertThat(emails.get(0).get("subject").asText()).isEqualTo(subject);
                });
    }

    @Test
    @DisplayName("Should handle URLs with query parameters in confirmation link")
    void sendEmail_shouldHandleComplexUrls() throws Exception {
        // Given
        String to = "test@example.com";
        String userName = "Test User";
        String subject = "Test";
        String confirmationLink = "http://localhost/activate?token=abc123&user=john%20doe&redirect=%2Fdashboard";

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, confirmationLink, "code");

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                    
                    String html = emails.get(0).get("html").asText();
                    assertThat(html).contains("token=abc123");
                    assertThat(html).contains("user=john%20doe");
                });
    }

    // ========== Email Format Validation Tests ==========

    @Test
    @DisplayName("Should set correct MIME type for HTML email")
    void sendEmail_shouldSetHtmlMimeType() throws Exception {
        // Given
        String to = "test@example.com";
        String userName = "Test User";
        String subject = "Test";

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, "http://link", "code");

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                    
                    JsonNode email = emails.get(0);
                    assertThat(email.has("html")).isTrue();
                    assertThat(email.get("html").asText()).startsWith("<");
                });
    }

    @Test
    @DisplayName("Should set correct sender name and address")
    void sendEmail_shouldSetCorrectSenderInfo() throws Exception {
        // Given
        String to = "test@example.com";
        String userName = "Test User";
        String subject = "Test";

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, "http://link", "code");

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                    
                    JsonNode email = emails.get(0);
                    JsonNode from = email.get("from").get(0);
                    
                    // Verify sender name is AppointMe
                    assertThat(from.get("name").asText()).isEqualTo("AppointMe");
                    
                    // Verify sender address contains noreply
                    assertThat(from.get("address").asText()).contains("noreply");
                });
    }

    @Test
    @DisplayName("Should send email with null optional parameters")
    void sendEmail_shouldHandleNullOptionalParameters() throws Exception {
        // Given - Only required parameters, optional ones are null
        String to = "test@example.com";
        String userName = "Test User";
        String subject = "Test";

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, null, null);

        // Then - Email should still be sent
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                    
                    JsonNode email = emails.get(0);
                    assertThat(email.get("subject").asText()).isEqualTo(subject);
                });
    }

    // ========== Template Tests ==========

    @Test
    @DisplayName("Should render ACTIVATE_ACCOUNT template correctly")
    void sendEmail_shouldRenderActivateAccountTemplate() throws Exception {
        // Given
        String to = "test@example.com";
        String userName = "John Doe";
        String subject = "Activate Account";
        String confirmationLink = "http://localhost/activate";
        String activationCode = "123456";

        // When
        emailService.sendEmail(to, userName, subject, EmailTemplateName.VERIFY_EMAIL, confirmationLink, activationCode);

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    JsonNode emails = getAllEmails();
                    assertThat(emails).hasSize(1);
                    
                    String html = emails.get(0).get("html").asText();
                    
                    // Verify all template variables are present
                    assertThat(html).contains(userName);
                    assertThat(html).contains(confirmationLink);
                    assertThat(html).contains(activationCode);
                    
                    // Verify it's valid HTML
                    assertThat(html).contains("<html");
                    assertThat(html).contains("</html>");
                });
    }

    // ========== Helper Methods ==========

    private JsonNode getAllEmails() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(maildevApiUrl))
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        if (body == null || body.trim().isEmpty() || body.equals("[]")) {
            return objectMapper.createArrayNode();
        }

        return objectMapper.readTree(body);
    }

    private void deleteAllEmails() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(maildevApiUrl + "/all"))
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }
}
