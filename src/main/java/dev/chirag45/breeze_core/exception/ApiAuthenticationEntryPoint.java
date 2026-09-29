package dev.chirag45.breeze_core.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(ApiAuthenticationEntryPoint.class);

    private final ApiErrorResponseWriter apiErrorResponseWriter;

    public ApiAuthenticationEntryPoint(ApiErrorResponseWriter apiErrorResponseWriter) {
        this.apiErrorResponseWriter = apiErrorResponseWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {
        log.debug(
                "authentication_failed exceptionType={}",
                authenticationException.getClass().getSimpleName()
        );
        apiErrorResponseWriter.write(
                response,
                HttpStatus.UNAUTHORIZED,
                "Authentication is required.",
                "UNAUTHENTICATED"
        );
    }
}
