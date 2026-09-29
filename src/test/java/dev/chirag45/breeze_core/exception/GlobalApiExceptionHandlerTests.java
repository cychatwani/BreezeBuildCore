package dev.chirag45.breeze_core.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalApiExceptionHandlerTests {

    private final GlobalApiExceptionHandler exceptionHandler = new GlobalApiExceptionHandler();

    @Test
    void returnsStandardResponseForMalformedRequestBody() {
        var response = exceptionHandler.handleUnreadableRequest(
                new HttpMessageNotReadableException("Malformed JSON", new MockHttpInputMessage(new byte[0]))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getCode()).isZero();
            assertThat(body.getErrorCode()).isEqualTo("MALFORMED_REQUEST");
            assertThat(body.getData()).isNull();
        });
    }

    @Test
    void returnsStandardResponseForMissingRequestParameter() {
        var response = exceptionHandler.handleMissingParameter(
                new MissingServletRequestParameterException("page", "integer")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getCode()).isZero();
            assertThat(body.getErrorCode()).isEqualTo("MISSING_REQUEST_PARAMETER");
            assertThat(body.getData()).isNull();
        });
    }

    @Test
    void returnsSafeStandardResponseForUnexpectedException() {
        var response = exceptionHandler.handleUnexpectedException(new IllegalStateException("internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body.getCode()).isZero();
            assertThat(body.getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
            assertThat(body.getMessage()).isEqualTo("An unexpected error occurred.");
            assertThat(body.getData()).isNull();
        });
    }
}
