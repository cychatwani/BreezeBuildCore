package dev.chirag45.breeze_core.security;

import dev.chirag45.breeze_core.exception.ApiErrorResponseWriter;
import dev.chirag45.breeze_core.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserProvisioningStatusTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CoreUserProvisioningFilter filter = new CoreUserProvisioningFilter(
            new SecurityConfig().coreUserRequiredRequestMatcher(),
            userRepository,
            new ApiErrorResponseWriter(new ObjectMapper())
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsProvisionedUserToReachStatusEndpoint() throws Exception {
        authenticate("clerk_user_1");
        when(userRepository.existsByClerkUserId("clerk_user_1")).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/provisioned");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean reachedEndpoint = new AtomicBoolean();
        assertThat(new SecurityConfig().coreUserRequiredRequestMatcher().matches(request)).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();

        filter.doFilter(request, response, (incoming, outgoing) -> {
            reachedEndpoint.set(true);
            ((MockHttpServletResponse) outgoing).setStatus(204);
        });

        assertThat(reachedEndpoint).isTrue();
        assertThat(response.getStatus()).isEqualTo(204);
        verify(userRepository).existsByClerkUserId("clerk_user_1");
    }

    @Test
    void rejectsUnprovisionedUserWithoutCallingEndpoint() throws Exception {
        authenticate("clerk_user_2");
        when(userRepository.existsByClerkUserId("clerk_user_2")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/provisioned");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean reachedEndpoint = new AtomicBoolean();
        assertThat(new SecurityConfig().coreUserRequiredRequestMatcher().matches(request)).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();

        filter.doFilter(request, response, (incoming, outgoing) -> reachedEndpoint.set(true));

        assertThat(reachedEndpoint).isFalse();
        assertThat(response.getStatus()).isEqualTo(428);
        assertThat(response.getContentAsString()).contains("\"errorCode\":\"CORE_USER_NOT_PROVISIONED\"");
    }

    @Test
    void leavesProvisioningEndpointAvailableToNewUsers() throws Exception {
        authenticate("clerk_user_3");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/provision");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean reachedEndpoint = new AtomicBoolean();

        filter.doFilter(request, response, (incoming, outgoing) -> reachedEndpoint.set(true));

        assertThat(reachedEndpoint).isTrue();
        verifyNoInteractions(userRepository);
    }

    private void authenticate(String clerkUserId) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(clerkUserId)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }
}
