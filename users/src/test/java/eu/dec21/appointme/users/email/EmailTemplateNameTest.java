package eu.dec21.appointme.users.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateNameTest {

    @Test
    void verifyEmail_hasCorrectName() {
        assertThat(EmailTemplateName.VERIFY_EMAIL.getName()).isEqualTo("verify-email");
    }

    @Test
    void resetPassword_hasCorrectName() {
        assertThat(EmailTemplateName.RESET_PASSWORD.getName()).isEqualTo("reset-password");
    }

    @Test
    void values_containsAllExpected() {
        assertThat(EmailTemplateName.values()).hasSize(2);
    }

    @Test
    void valueOf_works() {
        assertThat(EmailTemplateName.valueOf("VERIFY_EMAIL")).isEqualTo(EmailTemplateName.VERIFY_EMAIL);
    }
}
