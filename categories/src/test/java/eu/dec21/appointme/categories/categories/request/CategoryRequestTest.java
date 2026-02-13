package eu.dec21.appointme.categories.categories.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_noViolations() {
        CategoryRequest request = new CategoryRequest(null, "Test", "desc", null);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void nullName_hasViolation() {
        CategoryRequest request = new CategoryRequest(null, null, "desc", null);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void blankName_hasViolation() {
        CategoryRequest request = new CategoryRequest(null, "", "desc", null);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whitespaceOnlyName_hasViolation() {
        CategoryRequest request = new CategoryRequest(null, "   ", "desc", null);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void withParentId() {
        CategoryRequest request = new CategoryRequest(null, "Sub", "desc", 10L);
        assertThat(request.parentId()).isEqualTo(10L);
    }

    @Test
    void withId() {
        CategoryRequest request = new CategoryRequest(5L, "Test", null, null);
        assertThat(request.id()).isEqualTo(5L);
    }

    @Test
    void nullDescription_isValid() {
        CategoryRequest request = new CategoryRequest(null, "Test", null, null);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}
