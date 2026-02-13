package eu.dec21.appointme.exceptions.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessErrorCodesTest {

    @ParameterizedTest
    @EnumSource(BusinessErrorCodes.class)
    void allCodes_haveNonNullHttpStatus(BusinessErrorCodes code) {
        assertThat(code.getHttpStatus()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(BusinessErrorCodes.class)
    void allCodes_haveNonBlankDescription(BusinessErrorCodes code) {
        assertThat(code.getDescription()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(BusinessErrorCodes.class)
    void allCodes_havePositiveOrZeroCode(BusinessErrorCodes code) {
        assertThat(code.getCode()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void allCodes_haveUniqueCodeValues() {
        var codes = Arrays.stream(BusinessErrorCodes.values())
                .map(BusinessErrorCodes::getCode)
                .collect(Collectors.toList());
        assertThat(codes).doesNotHaveDuplicates();
    }

    @Test
    void totalEnumValues() {
        assertThat(BusinessErrorCodes.values()).hasSize(23);
    }

    // Verify specific important error codes
    @Test
    void accountLocked_hasCorrectProperties() {
        assertThat(BusinessErrorCodes.ACCOUNT_LOCKED.getCode()).isEqualTo(1001);
        assertThat(BusinessErrorCodes.ACCOUNT_LOCKED.getHttpStatus()).isEqualTo(HttpStatus.LOCKED);
    }

    @Test
    void invalidCredentials_hasCorrectProperties() {
        assertThat(BusinessErrorCodes.INVALID_CREDENTIALS.getCode()).isEqualTo(1005);
        assertThat(BusinessErrorCodes.INVALID_CREDENTIALS.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void badRequestParameters_hasCorrectProperties() {
        assertThat(BusinessErrorCodes.BAD_REQUEST_PARAMETERS.getCode()).isEqualTo(4001);
        assertThat(BusinessErrorCodes.BAD_REQUEST_PARAMETERS.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resourceNotFound_hasCorrectProperties() {
        assertThat(BusinessErrorCodes.RESOURCE_NOT_FOUND.getCode()).isEqualTo(4006);
        assertThat(BusinessErrorCodes.RESOURCE_NOT_FOUND.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void serverError_hasCorrectProperties() {
        assertThat(BusinessErrorCodes.SERVER_ERROR.getCode()).isEqualTo(5000);
        assertThat(BusinessErrorCodes.SERVER_ERROR.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void operationNotPermitted_hasForbiddenStatus() {
        assertThat(BusinessErrorCodes.OPERATION_NOT_PERMITTED.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void activationToken_hasUnauthorizedStatus() {
        assertThat(BusinessErrorCodes.ACTIVATION_TOKEN.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void accountDisabled_hasForbiddenStatus() {
        assertThat(BusinessErrorCodes.ACCOUNT_DISABLED.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void noCode_hasNotImplementedStatus() {
        assertThat(BusinessErrorCodes.NO_CODE.getCode()).isEqualTo(0);
        assertThat(BusinessErrorCodes.NO_CODE.getHttpStatus()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }

    // Code range verification
    @Test
    void authenticationCodes_areIn1000Range() {
        assertThat(BusinessErrorCodes.ACCOUNT_LOCKED.getCode()).isBetween(1000, 1999);
        assertThat(BusinessErrorCodes.INVALID_TOKEN.getCode()).isBetween(1000, 1999);
        assertThat(BusinessErrorCodes.ACTIVATION_TOKEN.getCode()).isBetween(1000, 1999);
        assertThat(BusinessErrorCodes.EXPIRED_TOKEN.getCode()).isBetween(1000, 1999);
        assertThat(BusinessErrorCodes.INVALID_CREDENTIALS.getCode()).isBetween(1000, 1999);
        assertThat(BusinessErrorCodes.ACCOUNT_DISABLED.getCode()).isBetween(1000, 1999);
    }

    @Test
    void paymentCodes_areIn2000Range() {
        assertThat(BusinessErrorCodes.INSUFFICIENT_FUNDS.getCode()).isBetween(2000, 2999);
        assertThat(BusinessErrorCodes.QUOTA_EXCEEDED.getCode()).isBetween(2000, 2999);
        assertThat(BusinessErrorCodes.PAYMENT_METHOD_DECLINED.getCode()).isBetween(2000, 2999);
        assertThat(BusinessErrorCodes.ALREADY_PAID.getCode()).isBetween(2000, 2999);
    }

    @Test
    void clientErrorCodes_areIn4000Range() {
        assertThat(BusinessErrorCodes.BAD_REQUEST_PARAMETERS.getCode()).isBetween(4000, 4999);
        assertThat(BusinessErrorCodes.OPERATION_NOT_PERMITTED.getCode()).isBetween(4000, 4999);
        assertThat(BusinessErrorCodes.RESOURCE_CONFLICT.getCode()).isBetween(4000, 4999);
        assertThat(BusinessErrorCodes.RESOURCE_NOT_FOUND.getCode()).isBetween(4000, 4999);
    }

    @Test
    void serverErrorCodes_areIn5000Range() {
        assertThat(BusinessErrorCodes.SERVER_ERROR.getCode()).isBetween(5000, 5999);
        assertThat(BusinessErrorCodes.SERVICE_NOT_AVAILABLE.getCode()).isBetween(5000, 5999);
        assertThat(BusinessErrorCodes.TIMEOUT_OCCURRED.getCode()).isBetween(5000, 5999);
        assertThat(BusinessErrorCodes.SERVER_BUSY.getCode()).isBetween(5000, 5999);
    }
}
