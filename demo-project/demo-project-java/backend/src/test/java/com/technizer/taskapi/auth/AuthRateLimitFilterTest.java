package com.technizer.taskapi.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    private AuthRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthRateLimitFilter(5, Duration.ofMinutes(1));
    }

    private MockHttpServletRequest request(String method, String uri, String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    private MockHttpServletResponse call(MockHttpServletRequest request, MockFilterChain chain) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    void sixthRequestFromSameIpIsRejectedWith429() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockFilterChain chain = new MockFilterChain();
            MockHttpServletResponse response = call(request("POST", "/api/auth/login", "10.0.0.1"), chain);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(chain.getRequest()).isNotNull();
        }

        MockFilterChain blockedChain = new MockFilterChain();
        MockHttpServletResponse blocked = call(request("POST", "/api/auth/login", "10.0.0.1"), blockedChain);

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(Long.parseLong(blocked.getHeader("Retry-After"))).isPositive();
        assertThat(blocked.getContentType()).startsWith("application/json");
        assertThat(blocked.getContentAsString()).contains("\"error\"").contains("Too many requests");
        assertThat(blockedChain.getRequest()).isNull();
    }

    @Test
    void differentIpIsStillAllowedAfterFirstIpIsLimited() throws Exception {
        for (int i = 0; i < 6; i++) {
            call(request("POST", "/api/auth/login", "10.0.0.1"), new MockFilterChain());
        }

        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = call(request("POST", "/api/auth/login", "10.0.0.2"), chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void nonAuthPathsAreNeverRateLimited() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockFilterChain chain = new MockFilterChain();
            MockHttpServletResponse response = call(request("GET", "/api/tasks", "10.0.0.1"), chain);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(chain.getRequest()).isNotNull();
        }
    }

    @Test
    void optionsPreflightIsNotCounted() throws Exception {
        for (int i = 0; i < 10; i++) {
            call(request("OPTIONS", "/api/auth/login", "10.0.0.1"), new MockFilterChain());
        }

        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = call(request("POST", "/api/auth/login", "10.0.0.1"), new MockFilterChain());
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    @Test
    void remainingHeaderDecrementsFromFourToZero() throws Exception {
        for (int expected = 4; expected >= 0; expected--) {
            MockHttpServletResponse response = call(request("POST", "/api/auth/login", "10.0.0.1"), new MockFilterChain());

            assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo(String.valueOf(expected));
        }
    }

    @Test
    void registerEndpointSharesTheSameBucketAsLogin() throws Exception {
        for (int i = 0; i < 5; i++) {
            call(request("POST", "/api/auth/login", "10.0.0.1"), new MockFilterChain());
        }

        MockHttpServletResponse response = call(request("POST", "/api/auth/register", "10.0.0.1"), new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(429);
    }
}
