package dev.chirag45.breeze_core.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserMdcFilterTests {

    private final AuthenticatedUserMdcFilter filter = new AuthenticatedUserMdcFilter();

    @AfterEach
    void clearRequestContext() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void bindsVerifiedClerkUserIdOnlyForTheCurrentRequest() throws Exception {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("user_123")
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (request, response) -> assertThat(MDC.get(RequestCorrelationFilter.CLERK_USER_ID_MDC_KEY))
                        .isEqualTo("user_123")
        );

        assertThat(MDC.get(RequestCorrelationFilter.CLERK_USER_ID_MDC_KEY)).isNull();
    }

    @Test
    void doesNotBindUserIdWhenThereIsNoAuthenticatedJwt() throws Exception {
        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (request, response) -> assertThat(MDC.get(RequestCorrelationFilter.CLERK_USER_ID_MDC_KEY)).isNull()
        );
    }
}
