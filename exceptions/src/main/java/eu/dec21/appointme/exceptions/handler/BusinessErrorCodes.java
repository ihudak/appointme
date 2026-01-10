package eu.dec21.appointme.exceptions.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum BusinessErrorCodes {

    NO_CODE(0, NOT_IMPLEMENTED, "No error code defined"),
    ACCOUNT_LOCKED(1001, LOCKED, "The user account is locked"),
    INVALID_TOKEN(1002, UNAUTHORIZED, "The provided token is invalid"),
    ACTIVATION_TOKEN(1003, UNAUTHORIZED, "The provided activation token is invalid"),
    EXPIRED_TOKEN(1004, UNAUTHORIZED, "The provided token has expired"),
    INVALID_CREDENTIALS(1005, UNAUTHORIZED, "The provided credentials are invalid"),
    ACCOUNT_DISABLED(1006, FORBIDDEN, "The user account is disabled"),
    INSUFFICIENT_FUNDS(2001, PAYMENT_REQUIRED, "Insufficient funds for the transaction"),
    QUOTA_EXCEEDED(2002, CONTENT_TOO_LARGE, "The user has exceeded their quota limits"),
    PAYMENT_METHOD_DECLINED(2003, PAYMENT_REQUIRED, "The payment method was declined"),
    ALREADY_PAID(2004, CONFLICT, "The invoice has already been paid"),
    DEPENDENCY_FAILURE(3001, FAILED_DEPENDENCY, "A required dependency has failed"),
    BAD_REQUEST_PARAMETERS(4001, BAD_REQUEST, "The request parameters are invalid"),
    OPERATION_NOT_PERMITTED(4002, FORBIDDEN, "The operation is not permitted"),
    RESOURCE_CONFLICT(4003, CONFLICT, "There is a conflict with the current state of the resource"),
    RESOURCE_LOCKED(4004, LOCKED, "The resource is locked"),
    DATA_INTEGRITY_VIOLATION(4005, CONFLICT, "Data integrity violation occurred"),
    RESOURCE_NOT_FOUND(4006, NOT_FOUND, "The requested resource was not found"),
    OPERATION_NOT_SUPPORTED(4007, NOT_IMPLEMENTED, "The requested operation is not supported"),
    SERVER_ERROR(5000, INTERNAL_SERVER_ERROR, "An internal server error occurred"),
    SERVICE_NOT_AVAILABLE(5001, SERVICE_UNAVAILABLE, "The service is currently unavailable"),
    TIMEOUT_OCCURRED(5002, GATEWAY_TIMEOUT, "A timeout occurred while processing the request"),
    SERVER_BUSY(5003, TOO_MANY_REQUESTS, "Too many requests have been made in a short period");

    @Getter
    private final int code;
    @Getter
    private final HttpStatus httpStatus;
    @Getter
    private final String description;

    BusinessErrorCodes(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.description = description;
    }
}
