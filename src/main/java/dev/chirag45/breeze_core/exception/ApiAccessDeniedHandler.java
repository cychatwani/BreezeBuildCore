package dev.chirag45.breeze_core.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiAccessDeniedHandler.class);

    private final ApiErrorResponseWriter apiErrorResponseWriter;

    public ApiAccessDeniedHandler(ApiErrorResponseWriter apiErrorResponseWriter) {
        this.apiErrorResponseWriter = apiErrorResponseWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        log.debug(
                "access_denied exceptionType={}",
                accessDeniedException.getClass().getSimpleName()
        );
        apiErrorResponseWriter.write(
                response,
                HttpStatus.FORBIDDEN,
                "Access is denied.",
                "ACCESS_DENIED"
        );
    }
}
