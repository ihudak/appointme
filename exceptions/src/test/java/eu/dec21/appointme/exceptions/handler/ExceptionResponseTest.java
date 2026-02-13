package eu.dec21.appointme.exceptions.handler;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionResponseTest {

    @Test
    void builder_withAllFields() {
        ExceptionResponse response = ExceptionResponse.builder()
                .businessErrorCode(1001)
                .businessErrorDescription("Account locked")
                .error("Your account is locked")
                .validationErrors(Set.of("error1", "error2"))
                .errors(Map.of("field1", "message1"))
                .build();

        assertThat(response.getBusinessErrorCode()).isEqualTo(1001);
        assertThat(response.getBusinessErrorDescription()).isEqualTo("Account locked");
        assertThat(response.getError()).isEqualTo("Your account is locked");
        assertThat(response.getValidationErrors()).containsExactlyInAnyOrder("error1", "error2");
        assertThat(response.getErrors()).containsEntry("field1", "message1");
    }

    @Test
    void builder_withMinimalFields() {
        ExceptionResponse response = ExceptionResponse.builder()
                .error("Something went wrong")
                .build();

        assertThat(response.getError()).isEqualTo("Something went wrong");
        assertThat(response.getBusinessErrorCode()).isNull();
        assertThat(response.getBusinessErrorDescription()).isNull();
        assertThat(response.getValidationErrors()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void noArgsConstructor() {
        ExceptionResponse response = new ExceptionResponse();

        assertThat(response.getBusinessErrorCode()).isNull();
        assertThat(response.getBusinessErrorDescription()).isNull();
        assertThat(response.getError()).isNull();
        assertThat(response.getValidationErrors()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void allArgsConstructor() {
        ExceptionResponse response = new ExceptionResponse(
                1001, "Description", "Error", Set.of("v1"), Map.of("k", "v"));

        assertThat(response.getBusinessErrorCode()).isEqualTo(1001);
        assertThat(response.getBusinessErrorDescription()).isEqualTo("Description");
        assertThat(response.getError()).isEqualTo("Error");
        assertThat(response.getValidationErrors()).hasSize(1);
        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void setters() {
        ExceptionResponse response = new ExceptionResponse();
        response.setBusinessErrorCode(5000);
        response.setBusinessErrorDescription("Server error");
        response.setError("Internal error");
        response.setValidationErrors(Set.of("e1"));
        response.setErrors(Map.of("f1", "m1"));

        assertThat(response.getBusinessErrorCode()).isEqualTo(5000);
        assertThat(response.getBusinessErrorDescription()).isEqualTo("Server error");
        assertThat(response.getError()).isEqualTo("Internal error");
        assertThat(response.getValidationErrors()).contains("e1");
        assertThat(response.getErrors()).containsEntry("f1", "m1");
    }

    @Test
    void builder_emptyValidationErrors() {
        ExceptionResponse response = ExceptionResponse.builder()
                .validationErrors(Set.of())
                .build();

        assertThat(response.getValidationErrors()).isEmpty();
    }

    @Test
    void builder_emptyErrors() {
        ExceptionResponse response = ExceptionResponse.builder()
                .errors(Map.of())
                .build();

        assertThat(response.getErrors()).isEmpty();
    }
}
