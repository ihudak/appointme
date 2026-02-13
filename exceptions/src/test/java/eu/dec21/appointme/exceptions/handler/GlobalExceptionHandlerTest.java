package eu.dec21.appointme.exceptions.handler;

import eu.dec21.appointme.exceptions.ActivationTokenException;
import eu.dec21.appointme.exceptions.OperationNotPermittedException;
import eu.dec21.appointme.exceptions.ResourceNotFoundException;
import eu.dec21.appointme.exceptions.UserAuthenticationException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // === LockedException ===

    @Test
    void handleLockedException_returnsLockedStatus() {
        LockedException ex = new LockedException("Account is locked");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.ACCOUNT_LOCKED.getCode());
        assertThat(response.getBody().getBusinessErrorDescription()).isEqualTo(BusinessErrorCodes.ACCOUNT_LOCKED.getDescription());
        assertThat(response.getBody().getError()).isEqualTo("Account is locked");
    }

    @Test
    void handleLockedException_withNullMessage() {
        LockedException ex = new LockedException(null);

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isNull();
    }

    // === ResourceNotFoundException ===

    @Test
    void handleResourceNotFoundException_returnsNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Business not found");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.RESOURCE_NOT_FOUND.getCode());
        assertThat(response.getBody().getBusinessErrorDescription()).isEqualTo(BusinessErrorCodes.RESOURCE_NOT_FOUND.getDescription());
        // ResponseStatusException.getMessage() includes status prefix
        assertThat(response.getBody().getError()).contains("Business not found");
    }

    // === EntityNotFoundException ===

    @Test
    void handleEntityNotFoundException_returnsNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Entity not found");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.RESOURCE_NOT_FOUND.getCode());
        assertThat(response.getBody().getError()).isEqualTo("Entity not found");
    }

    @Test
    void handleEntityNotFoundException_withNullMessage() {
        EntityNotFoundException ex = new EntityNotFoundException();

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isNull();
    }

    // === MethodArgumentTypeMismatchException ===

    @Test
    void handleMethodArgumentTypeMismatch_returnsBadRequest() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("businessId");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.BAD_REQUEST_PARAMETERS.getCode());
        assertThat(response.getBody().getError()).isEqualTo("Invalid parameter: businessId");
    }

    // === DisabledException ===

    @Test
    void handleDisabledException_returnsForbidden() {
        DisabledException ex = new DisabledException("Account is disabled");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.ACCOUNT_DISABLED.getCode());
        assertThat(response.getBody().getBusinessErrorDescription()).isEqualTo(BusinessErrorCodes.ACCOUNT_DISABLED.getDescription());
        assertThat(response.getBody().getError()).isEqualTo("Account is disabled");
    }

    // === UserAuthenticationException ===

    @Test
    void handleUserAuthenticationException_returnsUnauthorized() {
        UserAuthenticationException ex = new UserAuthenticationException("Authentication failed");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.INVALID_CREDENTIALS.getCode());
        // ResponseStatusException.getMessage() includes status prefix
        assertThat(response.getBody().getError()).contains("Authentication failed");
    }

    // === BadCredentialsException ===

    @Test
    void handleBadCredentials_returnsUnauthorized() {
        ResponseEntity<ExceptionResponse> response = handler.handleException();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.INVALID_CREDENTIALS.getCode());
        assertThat(response.getBody().getError()).isEqualTo("Credentials are incorrect");
    }

    // === MessagingException ===

    @Test
    void handleMessagingException_returnsServerError() {
        MessagingException ex = new MessagingException("SMTP connection failed");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.SERVER_ERROR.getCode());
        assertThat(response.getBody().getError()).isEqualTo("SMTP connection failed");
    }

    // === ActivationTokenException ===

    @Test
    void handleActivationTokenException_returnsUnauthorized() {
        ActivationTokenException ex = new ActivationTokenException("Token expired");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.ACTIVATION_TOKEN.getCode());
        assertThat(response.getBody().getError()).isEqualTo("Token expired");
    }

    // === OperationNotPermittedException ===

    @Test
    void handleOperationNotPermitted_returnsForbidden() {
        OperationNotPermittedException ex = new OperationNotPermittedException("Cannot delete");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.OPERATION_NOT_PERMITTED.getCode());
        assertThat(response.getBody().getError()).isEqualTo("Cannot delete");
    }

    @Test
    void handleOperationNotPermitted_defaultMessage() {
        OperationNotPermittedException ex = new OperationNotPermittedException();

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError()).isEqualTo("Operation not permitted");
    }

    // === AccessDeniedException ===

    @Test
    void handleAccessDenied_returnsForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.OPERATION_NOT_PERMITTED.getCode());
        assertThat(response.getBody().getError()).isEqualTo("Access denied");
    }

    // === MethodArgumentNotValidException ===

    @Test
    void handleMethodArgumentNotValid_returnsBadRequestWithValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "name", "Name is required");
        FieldError fieldError2 = new FieldError("object", "email", "Email is invalid");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ExceptionResponse> response = handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.BAD_REQUEST_PARAMETERS.getCode());
        assertThat(response.getBody().getError()).isEqualTo("Validation failed for one or more parameters");
        assertThat(response.getBody().getValidationErrors()).containsExactlyInAnyOrder("Name is required", "Email is invalid");
    }

    @Test
    void handleMethodArgumentNotValid_singleError() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "name", "Name is required");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ExceptionResponse> response = handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getBody().getValidationErrors()).hasSize(1);
        assertThat(response.getBody().getValidationErrors()).contains("Name is required");
    }

    @Test
    void handleMethodArgumentNotValid_duplicateMessages_deduplicatedBySet() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "name", "Required");
        FieldError fieldError2 = new FieldError("object", "title", "Required");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ExceptionResponse> response = handler.handleMethodArgumentNotValidException(ex);

        // Duplicate messages should be deduplicated by HashSet
        assertThat(response.getBody().getValidationErrors()).hasSize(1);
        assertThat(response.getBody().getValidationErrors()).contains("Required");
    }

    // === MissingServletRequestParameterException ===

    @Test
    void handleMissingRequestParameter_returnsBadRequest() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("page", "int");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Missing required parameter: page");
    }

    // === HttpMessageNotReadableException ===

    @Test
    void handleHttpMessageNotReadable_returnsBadRequest() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Malformed or missing request body");
    }

    // === HandlerMethodValidationException ===

    @Test
    void handleHandlerMethodValidation_returnsBadRequest() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        org.springframework.context.MessageSourceResolvable error1 = mock(org.springframework.context.MessageSourceResolvable.class);
        org.springframework.context.MessageSourceResolvable error2 = mock(org.springframework.context.MessageSourceResolvable.class);
        when(error1.getDefaultMessage()).thenReturn("must be positive");
        when(error2.getDefaultMessage()).thenReturn("must be at least 1");
        doReturn(List.of(error1, error2)).when(ex).getAllErrors();

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).containsExactlyInAnyOrder("must be positive", "must be at least 1");
    }

    // === ConstraintViolationException ===

    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolation_returnsBadRequest() {
        ConstraintViolation<Object> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<Object> violation2 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("must be positive");
        when(violation2.getMessage()).thenReturn("must not be null");
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation2.getPropertyPath()).thenReturn(path2);

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation1, violation2));

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.BAD_REQUEST_PARAMETERS.getCode());
        assertThat(response.getBody().getValidationErrors()).contains("must be positive", "must not be null");
    }

    // === Generic Exception (catch-all) ===

    @Test
    void handleGenericException_returnsServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessErrorCode()).isEqualTo(BusinessErrorCodes.SERVER_ERROR.getCode());
        assertThat(response.getBody().getError()).isEqualTo("Unexpected error");
    }

    @Test
    void handleGenericException_withNullMessage() {
        Exception ex = new RuntimeException((String) null);

        ResponseEntity<ExceptionResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isNull();
    }

    // === Response structure verification ===

    @Test
    void allHandlers_neverReturnNullBody() {
        // Verify every handler returns a non-null body
        assertThat(handler.handleException(new LockedException("x")).getBody()).isNotNull();
        assertThat(handler.handleException(new ResourceNotFoundException("x")).getBody()).isNotNull();
        assertThat(handler.handleException(new EntityNotFoundException("x")).getBody()).isNotNull();
        assertThat(handler.handleException(new DisabledException("x")).getBody()).isNotNull();
        assertThat(handler.handleException(new UserAuthenticationException("x")).getBody()).isNotNull();
        assertThat(handler.handleException().getBody()).isNotNull();
        assertThat(handler.handleException(new MessagingException("x")).getBody()).isNotNull();
        assertThat(handler.handleException(new ActivationTokenException("x")).getBody()).isNotNull();
        assertThat(handler.handleException(new OperationNotPermittedException("x")).getBody()).isNotNull();
        assertThat(handler.handleException(new AccessDeniedException("x")).getBody()).isNotNull();
        assertThat(handler.handleException(new RuntimeException("x")).getBody()).isNotNull();
    }
}
