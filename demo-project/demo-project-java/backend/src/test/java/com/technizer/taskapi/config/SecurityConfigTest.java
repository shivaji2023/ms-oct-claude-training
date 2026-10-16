package com.technizer.taskapi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.technizer.taskapi.auth.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.jwt.secret=test-only-secret-0123456789-abcdefghij")
@AutoConfigureMockMvc
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtUtil jwtUtil;

  @Test
  void h2ConsoleIsNotPermittedWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/h2-console")).andExpect(status().isForbidden());
  }

  @Test
  void h2ConsoleSubPathsAreNotPermittedWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/h2-console/login.do")).andExpect(status().isForbidden());
  }

  @Test
  void h2ConsoleIsDeniedEvenWithValidToken() throws Exception {
    String token = jwtUtil.generateToken(1L, "user@example.com");

    mockMvc
        .perform(get("/h2-console/login.do").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void taskEndpointsStillRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/tasks")).andExpect(status().isForbidden());
  }

  @Test
  void authEndpointsRemainPublic() throws Exception {
    for (String path : new String[] {"/api/auth/login", "/api/auth/register"}) {
      int status =
          mockMvc
              .perform(
                  post(path)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(
                          "{\"email\":\"nobody@example.com\",\"password\":\"x\",\"displayName\":\"x\"}"))
              .andReturn()
              .getResponse()
              .getStatus();
      assertThat(status).as(path).isNotEqualTo(403);
    }
  }

  @Test
  void responsesCarryXFrameOptionsDeny() throws Exception {
    mockMvc.perform(get("/api/tasks")).andExpect(header().string("X-Frame-Options", "DENY"));
  }

  @Test
  void malformedBearerTokenIsRejected() throws Exception {
    mockMvc
        .perform(get("/api/tasks").header("Authorization", "Bearer not-a-jwt"))
        .andExpect(status().isForbidden());
  }

  @Test
  void tokenSignedWithDifferentKeyIsRejected() throws Exception {
    String foreign =
        new JwtUtil("another-secret-0123456789-abcdefghijk", 60)
            .generateToken(1L, "user@example.com");

    mockMvc
        .perform(get("/api/tasks").header("Authorization", "Bearer " + foreign))
        .andExpect(status().isForbidden());
  }
}
