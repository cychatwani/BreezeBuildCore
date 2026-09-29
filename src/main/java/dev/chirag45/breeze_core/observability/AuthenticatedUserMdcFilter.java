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

public class AuthenticatedUserMdcFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatedUserMdcFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || !jwtAuthentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        MDC.put(
                RequestCorrelationFilter.CLERK_USER_ID_MDC_KEY,
                jwtAuthentication.getToken().getSubject()
        );
        log.trace("authenticated_clerk_user_mdc_bound");

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.trace("authenticated_clerk_user_mdc_clearing");
            MDC.remove(RequestCorrelationFilter.CLERK_USER_ID_MDC_KEY);
        }
    }
}
