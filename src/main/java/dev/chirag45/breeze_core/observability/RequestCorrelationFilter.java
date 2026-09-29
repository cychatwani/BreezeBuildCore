package dev.chirag45.breeze_core.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String INSTANCE_ID_MDC_KEY = "instanceId";
    public static final String CLERK_USER_ID_MDC_KEY = "clerkUserId";
    public static final String HTTP_METHOD_MDC_KEY = "httpMethod";
    public static final String HTTP_PATH_MDC_KEY = "httpPath";
    public static final String HTTP_STATUS_MDC_KEY = "httpStatus";
    public static final String REQUEST_DURATION_MS_MDC_KEY = "requestDurationMs";

    private static final Logger log = LoggerFactory.getLogger(RequestCorrelationFilter.class);

    private final String instanceId;

    public RequestCorrelationFilter(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
        long startNanos = System.nanoTime();

        response.setHeader(REQUEST_ID_HEADER, requestId);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        MDC.put(INSTANCE_ID_MDC_KEY, instanceId);
        MDC.put(HTTP_METHOD_MDC_KEY, request.getMethod());
        MDC.put(HTTP_PATH_MDC_KEY, request.getRequestURI());

        log.trace("request_mdc_initialized");
        log.info("http_request_started");

        try {
            filterChain.doFilter(request, response);
        } finally {
            addAuthenticatedClerkUserIdToMdc();

            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            MDC.put(HTTP_STATUS_MDC_KEY, Integer.toString(response.getStatus()));
            MDC.put(REQUEST_DURATION_MS_MDC_KEY, Long.toString(durationMs));

            log.info("http_request_completed");
            log.trace("request_mdc_clearing");
            MDC.clear();
        }
    }

    private String resolveRequestId(String requestIdHeader) {
        if (requestIdHeader == null || requestIdHeader.isBlank()) {
            return UUID.randomUUID().toString();
        }

        try {
            return UUID.fromString(requestIdHeader).toString();
        } catch (IllegalArgumentException ignored) {
            return UUID.randomUUID().toString();
        }
    }

    private void addAuthenticatedClerkUserIdToMdc() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                && jwtAuthentication.isAuthenticated()) {
            MDC.put(CLERK_USER_ID_MDC_KEY, jwtAuthentication.getToken().getSubject());
        }
    }
}
