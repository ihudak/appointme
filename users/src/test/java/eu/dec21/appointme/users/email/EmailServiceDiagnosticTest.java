package eu.dec21.appointme.users.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("EmailService Simple Diagnostic Test")
class EmailServiceDiagnosticTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.2-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> maildev = new GenericContainer<>(DockerImageName.parse("maildev/maildev:latest"))
            .withExposedPorts(1080, 1025)
            .waitingFor(Wait.forHttp("/").forPort(1080).forStatusCode(200));

    @Autowired
    private EmailService emailService;

    @Autowired
    private JavaMailSender mailSender;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.mail.host", maildev::getHost);
        registry.add("spring.mail.port", () -> maildev.getMappedPort(1025));
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
        
        registry.add("application.email.no-reply", () -> "noreply@appointme-test.com");
        registry.add("application.name", () -> "AppointMe");
    }

    @Test
    @DisplayName("Should autowire EmailService bean")
    void shouldAutowireEmailService() {
        assertThat(emailService).isNotNull();
        System.out.println("✓ EmailService bean is present");
    }

    @Test
    @DisplayName("Should autowire JavaMailSender bean")
    void shouldAutowireJavaMailSender() {
        assertThat(mailSender).isNotNull();
        System.out.println("✓ JavaMailSender bean is present");
    }

    @Test
    @DisplayName("Should create MimeMessage via JavaMailSender")
    void shouldCreateMimeMessage() {
        MimeMessage message = mailSender.createMimeMessage();
        assertThat(message).isNotNull();
        System.out.println("✓ Can create MimeMessage");
    }

    @Test
    @DisplayName("Should have MailDev container running")
    void shouldHaveMailDevRunning() {
        assertThat(maildev.isRunning()).isTrue();
        System.out.println("✓ MailDev container is running on " + 
                maildev.getHost() + ":" + maildev.getMappedPort(1025));
    }

    @Test
    @DisplayName("Should call sendEmail method without exception")
    void shouldCallSendEmailMethod() {
        try {
            System.out.println("Calling emailService.sendEmail()...");
            emailService.sendEmail(
                    "test@example.com",
                    "Test User",
                    "Test Subject",
                    EmailTemplateName.VERIFY_EMAIL,
                    "http://test.com",
                    "123456"
            );
            System.out.println("✓ sendEmail() call completed (async, no exception thrown)");
            
            // Sleep to allow async to complete
            Thread.sleep(5000);
            System.out.println("✓ Waited 5 seconds for async completion");
            
        } catch (Exception e) {
            System.err.println("✗ Exception calling sendEmail(): " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("sendEmail threw exception", e);
        }
    }
}
