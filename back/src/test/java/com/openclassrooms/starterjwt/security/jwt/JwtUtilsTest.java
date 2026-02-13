package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour JwtUtils
 *
 * Ce qu'on teste :
 * - generateJwtToken() : génération d'un token JWT
 * - getUserNameFromJwtToken() : extraction du username depuis le token
 * - validateJwtToken() : validation du token (valide, expiré, malformé, etc.)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtils - Tests Unitaires")
class JwtUtilsTest {

  @InjectMocks
  private JwtUtils jwtUtils;

  private String jwtSecret = "testSecretKeyForJwtTokenGenerationMustBeLongEnough";
  private int jwtExpirationMs = 3600000; // 1 heure

  private UserDetailsImpl userDetails;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    // Injecter les valeurs de configuration dans JwtUtils
    ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
    ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);

    // Créer un UserDetails
    userDetails = UserDetailsImpl.builder()
      .id(1L)
      .username("test@test.com")
      .firstName("John")
      .lastName("Doe")
      .password("password123")
      .admin(false)
      .build();
  }

  private Authentication createMockAuthentication() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(userDetails);
    return auth;
  }

  // ========== TESTS GENERATE JWT TOKEN ==========

  @Test
  @DisplayName("Devrait générer un token JWT valide")
  void testGenerateJwtToken() {
    // ARRANGE
    Authentication auth = createMockAuthentication();

    // ACT
    String token = jwtUtils.generateJwtToken(auth);

    // ASSERT
    assertThat(token).isNotNull();
    assertThat(token).isNotEmpty();
    assertThat(token.split("\\.")).hasSize(3); // JWT a 3 parties séparées par des points
  }

  @Test
  @DisplayName("Devrait générer un token contenant le username")
  void testGenerateJwtToken_ContainsUsername() {
    // ARRANGE
    Authentication auth = createMockAuthentication();

    // ACT
    String token = jwtUtils.generateJwtToken(auth);

    // ASSERT
    String username = jwtUtils.getUserNameFromJwtToken(token);
    assertThat(username).isEqualTo("test@test.com");
  }

  @Test
  @DisplayName("Devrait extraire le username depuis un token valide")
  void testGetUserNameFromJwtToken() {
    // ARRANGE
    Authentication auth = createMockAuthentication();
    String token = jwtUtils.generateJwtToken(auth);

    // ACT
    String username = jwtUtils.getUserNameFromJwtToken(token);

    // ASSERT
    assertThat(username).isEqualTo("test@test.com");
  }

  @Test
  @DisplayName("Devrait extraire le username depuis un token créé manuellement")
  void testGetUserNameFromJwtToken_ManualToken() {
    // ARRANGE
    String manualToken = Jwts.builder()
      .setSubject("manual@test.com")
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
      .signWith(SignatureAlgorithm.HS512, jwtSecret)
      .compact();

    // ACT
    String username = jwtUtils.getUserNameFromJwtToken(manualToken);

    // ASSERT
    assertThat(username).isEqualTo("manual@test.com");
  }

  // ========== TESTS VALIDATE JWT TOKEN ==========

  @Test
  @DisplayName("Devrait valider un token JWT correct")
  void testValidateJwtToken_Valid() {
    // ARRANGE
    Authentication auth = createMockAuthentication();
    String token = jwtUtils.generateJwtToken(auth);

    // ACT
    boolean isValid = jwtUtils.validateJwtToken(token);

    // ASSERT
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Devrait rejeter un token avec une mauvaise signature")
  void testValidateJwtToken_InvalidSignature() {
    // ARRANGE - Token signé avec un mauvais secret
    String invalidToken = Jwts.builder()
      .setSubject("test@test.com")
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
      .signWith(SignatureAlgorithm.HS512, "wrongSecret")
      .compact();

    // ACT
    boolean isValid = jwtUtils.validateJwtToken(invalidToken);

    // ASSERT
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Devrait rejeter un token expiré")
  void testValidateJwtToken_Expired() {
    // ARRANGE - Token expiré (date passée)
    String expiredToken = Jwts.builder()
      .setSubject("test@test.com")
      .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
      .setExpiration(new Date(System.currentTimeMillis() - 5000))
      .signWith(SignatureAlgorithm.HS512, jwtSecret)
      .compact();

    // ACT
    boolean isValid = jwtUtils.validateJwtToken(expiredToken);

    // ASSERT
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Devrait rejeter un token malformé")
  void testValidateJwtToken_Malformed() {
    // ARRANGE
    String malformedToken = "this.is.not.a.valid.jwt.token";

    // ACT
    boolean isValid = jwtUtils.validateJwtToken(malformedToken);

    // ASSERT
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Devrait rejeter un token vide")
  void testValidateJwtToken_Empty() {
    // ACT
    boolean isValid = jwtUtils.validateJwtToken("");

    // ASSERT
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Devrait rejeter un token null")
  void testValidateJwtToken_Null() {
    // ACT
    boolean isValid = jwtUtils.validateJwtToken(null);

    // ASSERT
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Devrait rejeter un token sans les bonnes parties")
  void testValidateJwtToken_IncompleteToken() {
    // ARRANGE
    String incompleteToken = "header.payload"; // Manque la signature

    // ACT
    boolean isValid = jwtUtils.validateJwtToken(incompleteToken);

    // ASSERT
    assertThat(isValid).isFalse();
  }
}
