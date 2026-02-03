package eu.dec21.appointme.exceptions.handler;

import eu.dec21.appointme.exceptions.ActivationTokenException;
import eu.dec21.appointme.exceptions.OperationNotPermittedException;
import eu.dec21.appointme.exceptions.ResourceNotFoundException;
import eu.dec21.appointme.exceptions.UserAuthenticationException;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

import static eu.dec21.appointme.exceptions.handler.BusinessErrorCodes.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exp) {
        return ResponseEntity
                .status(ACCOUNT_LOCKED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_LOCKED.getCode())
                                .businessErrorDescription(ACCOUNT_LOCKED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(ResourceNotFoundException exp) {
        return ResponseEntity
                .status(RESOURCE_NOT_FOUND.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(RESOURCE_NOT_FOUND.getCode())
                                .businessErrorDescription(RESOURCE_NOT_FOUND.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException exp) {
        return ResponseEntity
                .status(ACCOUNT_DISABLED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_DISABLED.getCode())
                                .businessErrorDescription(ACCOUNT_DISABLED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleException(UserAuthenticationException exp) {
        return ResponseEntity
                .status(INVALID_CREDENTIALS.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INVALID_CREDENTIALS.getCode())
                                .businessErrorDescription(INVALID_CREDENTIALS.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException() {
        return ResponseEntity
                .status(INVALID_CREDENTIALS.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INVALID_CREDENTIALS.getCode())
                                .businessErrorDescription(INVALID_CREDENTIALS.getDescription())
                                .error("Credentials are incorrect")
                                .build()
                );
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exp) {
        return ResponseEntity
                .status(SERVER_ERROR.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(SERVER_ERROR.getCode())
                                .businessErrorDescription(SERVER_ERROR.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ActivationTokenException.class)
    public ResponseEntity<ExceptionResponse> handleException(ActivationTokenException exp) {
        return ResponseEntity
                .status(ACTIVATION_TOKEN.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACTIVATION_TOKEN.getCode())
                                .businessErrorDescription(ACTIVATION_TOKEN.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleException(OperationNotPermittedException exp) {
        return ResponseEntity
                .status(OPERATION_NOT_PERMITTED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(OPERATION_NOT_PERMITTED.getCode())
                                .businessErrorDescription(OPERATION_NOT_PERMITTED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleException(AccessDeniedException exp) {
        return ResponseEntity
                .status(OPERATION_NOT_PERMITTED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(OPERATION_NOT_PERMITTED.getCode())
                                .businessErrorDescription(OPERATION_NOT_PERMITTED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exp) {
        Set<String> errors = new HashSet<>();
        exp.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var errorMessage = error.getDefaultMessage();
                    errors.add(errorMessage);
                });

        return ResponseEntity
                .status(BAD_REQUEST_PARAMETERS.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_REQUEST_PARAMETERS.getCode())
                                .businessErrorDescription(BAD_REQUEST_PARAMETERS.getDescription())
                                .error("Validation failed for one or more parameters")
                                .validationErrors(errors)
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp) {
        exp.printStackTrace();
        return ResponseEntity
                .status(SERVER_ERROR.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(SERVER_ERROR.getCode())
                                .businessErrorDescription(SERVER_ERROR.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }
}
