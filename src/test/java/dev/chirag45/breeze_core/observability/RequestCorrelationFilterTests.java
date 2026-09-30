package dev.chirag45.breeze_core.observability;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestCorrelationFilterTests {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter("test-instance");

    @AfterEach
    void clearRequestContext() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void propagatesValidInboundRequestIdAndBindsRequestContextDuringFilterChain() throws Exception {
        String requestId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/provision");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, requestId);

        FilterChain chain = (servletRequest, servletResponse) -> {
            assertThat(MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY)).isEqualTo(requestId);
            assertThat(MDC.get(RequestCorrelationFilter.INSTANCE_ID_MDC_KEY)).isEqualTo("test-instance");
            assertThat(MDC.get(RequestCorrelationFilter.HTTP_METHOD_MDC_KEY)).isEqualTo("POST");
            assertThat(MDC.get(RequestCorrelationFilter.HTTP_PATH_MDC_KEY)).isEqualTo("/api/users/provision");
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER)).isEqualTo(requestId);
        assertThat(MDC.getCopyOfContextMap()).isNull();
    }

    @Test
    void generatesRequestIdWhenInboundHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/provision");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        String responseRequestId = response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER);
        assertThat(responseRequestId).isNotBlank();
        assertThatCodeCanBeParsedAsUuid(responseRequestId);
        assertThat(MDC.getCopyOfContextMap()).isNull();
    }

    private void assertThatCodeCanBeParsedAsUuid(String requestId) {
        UUID.fromString(requestId);
    }
}
