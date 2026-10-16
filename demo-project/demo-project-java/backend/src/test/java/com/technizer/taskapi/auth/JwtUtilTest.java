package com.technizer.taskapi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
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

  @Test
  void rejectsWhitespaceOnlySecretOfSufficientLength() {
    assertThatThrownBy(() -> new JwtUtil(" ".repeat(32), 60))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("app.jwt.secret");
  }

  @Test
  void rejectionMessageStatesMinimumLength() {
    assertThatThrownBy(() -> new JwtUtil("a".repeat(31), 60)).hasMessageContaining("32");
  }

  @Test
  void acceptsLongerSecret() {
    assertThat(new JwtUtil("a".repeat(64), 60)).isNotNull();
  }

  @Test
  void tokenCarriesEmailAndExpiryClaims() {
    JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, 60);
    SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));

    var claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwtUtil.generateToken(7L, "user@example.com"))
            .getPayload();

    assertThat(claims.get("email", String.class)).isEqualTo("user@example.com");
    long ttl = claims.getExpiration().getTime() - new Date().getTime();
    assertThat(ttl).isBetween(59 * 60 * 1000L, 60 * 60 * 1000L);
  }

  @Test
  void extractUserIdThrowsOnExpiredToken() {
    JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, -1);

    String token = jwtUtil.generateToken(1L, "user@example.com");

    assertThatThrownBy(() -> jwtUtil.extractUserId(token)).isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void extractUserIdThrowsOnTamperedToken() {
    JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, 60);
    String token = jwtUtil.generateToken(1L, "user@example.com");
    String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

    assertThatThrownBy(() -> jwtUtil.extractUserId(tampered))
        .isInstanceOf(SignatureException.class);
  }

  @Test
  void extractUserIdThrowsOnMalformedToken() {
    JwtUtil jwtUtil = new JwtUtil(VALID_SECRET, 60);

    assertThatThrownBy(() -> jwtUtil.extractUserId("not-a-jwt"))
        .isInstanceOf(MalformedJwtException.class);
  }

  @Test
  void extractUserIdThrowsOnTokenFromDifferentSecret() {
    String token = new JwtUtil("b".repeat(32), 60).generateToken(1L, "user@example.com");

    assertThatThrownBy(() -> new JwtUtil(VALID_SECRET, 60).extractUserId(token))
        .isInstanceOf(SignatureException.class);
  }
}
