package dev.chirag45.breeze_core.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthenticatedUserMdcFilter extends OncePerRequestFilter {

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

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(RequestCorrelationFilter.CLERK_USER_ID_MDC_KEY);
        }
    }
}
