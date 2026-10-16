package com.technizer.taskapi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JwtUtilTest {

  private static final String VALID_SECRET = "a".repeat(32);

  @Test
  void rejectsNullSecret() {
    assertThatThrownBy(() -> new JwtUtil(null, 60))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("app.jwt.secret");
  }

  @Test
  void rejectsBlankSecret() {
    assertThatThrownBy(() -> new JwtUtil("   ", 60)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void rejectsEmptySecret() {
    assertThatThrownBy(() -> new JwtUtil("", 60)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void rejects31ByteSecret() {
    assertThatThrownBy(() -> new JwtUtil("a".repeat(31), 60))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void measuresSecretLengthInBytesNotCharacters() {
    // 16 two-byte characters = 32 bytes in UTF-8, so this must be accepted.
    assertThat(new JwtUtil("é".repeat(16), 60)).isNotNull();
    // 15 two-byte characters + 1 ASCII = 31 bytes, so this must be rejected.
    assertThatThrownBy(() -> new JwtUtil("é".repeat(15) + "a", 60))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void accepts32ByteSecretAndRoundTripsToken() {
    JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, 60);

    String token = jwtUtil.generateToken(42L, "user@example.com");

    assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
  }
}
