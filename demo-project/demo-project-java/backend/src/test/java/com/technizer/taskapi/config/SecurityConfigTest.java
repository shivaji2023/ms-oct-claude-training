package com.technizer.taskapi.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void h2ConsoleIsNotPermittedWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/h2-console")).andExpect(status().isForbidden());
  }

  @Test
  void h2ConsoleSubPathsAreNotPermittedWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/h2-console/login.do")).andExpect(status().isForbidden());
  }

  @Test
  void taskEndpointsStillRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/tasks")).andExpect(status().isForbidden());
  }
}
