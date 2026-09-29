package dev.chirag45.breeze_core.security;

import dev.chirag45.breeze_core.dto.response.wrapper.ApiResponse;
import dev.chirag45.breeze_core.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CoreUserProvisioningFilter extends OncePerRequestFilter {

    private final RequestMatcher coreUserRequiredRequestMatcher;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public CoreUserProvisioningFilter(
            @Qualifier("coreUserRequiredRequestMatcher")
            RequestMatcher coreUserRequiredRequestMatcher,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.coreUserRequiredRequestMatcher = coreUserRequiredRequestMatcher;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
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

            if (!userRepository.existsByClerkUserId(clerkUserId)) {
                response.setStatus(HttpStatus.PRECONDITION_REQUIRED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(), ApiResponse.error(
                        "The Clerk user has not been provisioned in Breeze Core.",
                        "CORE_USER_NOT_PROVISIONED"
                ));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
