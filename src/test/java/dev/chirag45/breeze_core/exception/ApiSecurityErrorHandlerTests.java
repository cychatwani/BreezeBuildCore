package dev.chirag45.breeze_core.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ApiSecurityErrorHandlerTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiErrorResponseWriter responseWriter = new ApiErrorResponseWriter(objectMapper);
    private final ApiAuthenticationEntryPoint authenticationEntryPoint =
            new ApiAuthenticationEntryPoint(responseWriter);
    private final ApiAccessDeniedHandler accessDeniedHandler = new ApiAccessDeniedHandler(responseWriter);

    @Test
    void returnsStandardApiResponseForUnauthenticatedRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationEntryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new InsufficientAuthenticationException("missing token")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString())
                .contains("\"code\":0")
                .contains("\"errorCode\":\"UNAUTHENTICATED\"");
    }

    @Test
    void returnsStandardApiResponseForForbiddenRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("forbidden")
        );

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString())
                .contains("\"code\":0")
                .contains("\"errorCode\":\"ACCESS_DENIED\"");
    }
}
