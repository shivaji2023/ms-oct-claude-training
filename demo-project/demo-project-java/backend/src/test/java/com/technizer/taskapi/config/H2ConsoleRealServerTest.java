package com.technizer.taskapi.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.technizer.taskapi.auth.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * MockMvc never reaches the H2 servlet, so this runs against a real embedded server to prove the
 * console is unreachable even for a user holding a valid JWT.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "app.jwt.secret=test-only-secret-0123456789-abcdefghij")
class H2ConsoleRealServerTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JwtUtil jwtUtil;

  @Test
  void authenticatedUserCannotReachH2Console() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwtUtil.generateToken(1L, "user@example.com"));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/h2-console/login.do", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND);
  }

  @Test
  void anonymousUserCannotReachH2Console() {
    ResponseEntity<String> response = restTemplate.getForEntity("/h2-console", String.class);

    assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND);
  }
}
