package dev.chirag45.breeze_core.security;

import dev.chirag45.breeze_core.exception.ApiErrorResponseWriter;
import dev.chirag45.breeze_core.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CoreUserProvisioningFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CoreUserProvisioningFilter.class);

    private final RequestMatcher coreUserRequiredRequestMatcher;
    private final UserRepository userRepository;
    private final ApiErrorResponseWriter apiErrorResponseWriter;

    public CoreUserProvisioningFilter(
            @Qualifier("coreUserRequiredRequestMatcher")
            RequestMatcher coreUserRequiredRequestMatcher,
            UserRepository userRepository,
            ApiErrorResponseWriter apiErrorResponseWriter
    ) {
        this.coreUserRequiredRequestMatcher = coreUserRequiredRequestMatcher;
        this.userRepository = userRepository;
        this.apiErrorResponseWriter = apiErrorResponseWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !coreUserRequiredRequestMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                && jwtAuthentication.isAuthenticated()) {

            String clerkUserId = jwtAuthentication.getToken().getSubject();

            log.debug("core_user_check_started");
            log.trace("core_user_repository_lookup_started");
            boolean coreUserExists = userRepository.existsByClerkUserId(clerkUserId);
            log.trace("core_user_repository_lookup_completed coreUserExists={}", coreUserExists);

            if (!coreUserExists) {
                log.info("core_user_not_provisioned");
                apiErrorResponseWriter.write(
                        response,
                        HttpStatus.PRECONDITION_REQUIRED,
                        "The Clerk user has not been provisioned in Breeze Core.",
                        "CORE_USER_NOT_PROVISIONED"
                );
                return;
            }

            log.debug("core_user_check_completed");
        }

        filterChain.doFilter(request, response);
    }
}
