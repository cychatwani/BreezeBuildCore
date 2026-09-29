package dev.chirag45.breeze_core.exception;

import dev.chirag45.breeze_core.dto.response.wrapper.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        log.debug("request_validation_failed");
        return error(HttpStatus.BAD_REQUEST, "The request contains invalid fields.", "INVALID_REQUEST");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException exception) {
        log.debug("request_parameter_missing parameter={}", exception.getParameterName());
        return error(HttpStatus.BAD_REQUEST, "A required request parameter is missing.", "MISSING_REQUEST_PARAMETER");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.debug("request_parameter_invalid parameter={}", exception.getName());
        return error(HttpStatus.BAD_REQUEST, "A request parameter has an invalid value.", "INVALID_REQUEST_PARAMETER");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        log.debug("request_body_unreadable exceptionType={}", exception.getClass().getSimpleName());
        return error(HttpStatus.BAD_REQUEST, "The request body is malformed or unreadable.", "MALFORMED_REQUEST");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        log.debug("request_method_not_supported method={}", exception.getMethod());
        return error(HttpStatus.METHOD_NOT_ALLOWED, "The HTTP method is not supported for this endpoint.", "METHOD_NOT_ALLOWED");
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception exception) {
        log.debug("request_endpoint_not_found exceptionType={}", exception.getClass().getSimpleName());
        return error(HttpStatus.NOT_FOUND, "The requested endpoint was not found.", "ENDPOINT_NOT_FOUND");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error(
                "unhandled_api_exception exceptionType={}",
                exception.getClass().getName()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred.",
                        "INTERNAL_SERVER_ERROR"
                ));
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, errorCode));
    }
}
