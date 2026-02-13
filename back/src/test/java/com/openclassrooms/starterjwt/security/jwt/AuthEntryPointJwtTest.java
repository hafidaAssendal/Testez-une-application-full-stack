package com.openclassrooms.starterjwt.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour AuthEntryPointJwt
 *
 * Ce qu'on teste :
 * - commence() : gestion des erreurs d'authentification (401 Unauthorized)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthEntryPointJwt - Tests Unitaires")
class AuthEntryPointJwtTest {

  @InjectMocks
  private AuthEntryPointJwt authEntryPointJwt;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    objectMapper = new ObjectMapper();
  }

  // ========== TESTS COMMENCE ==========

  @Test
  @DisplayName("Devrait retourner 401 avec message d'erreur approprié")
  void testCommence_ReturnsUnauthorized() throws IOException, ServletException {
    // ARRANGE
    AuthenticationException authException = new BadCredentialsException("Invalid credentials");
    request.setServletPath("/api/user/1");

    // ACT
    authEntryPointJwt.commence(request, response, authException);

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

    // Vérifier le contenu JSON de la réponse
    String jsonResponse = response.getContentAsString();
    assertThat(jsonResponse).isNotEmpty();

    @SuppressWarnings("unchecked")
    Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);

    assertThat(body.get("status")).isEqualTo(401);
    assertThat(body.get("error")).isEqualTo("Unauthorized");
    assertThat(body.get("message")).isEqualTo("Invalid credentials");
    assertThat(body.get("path")).isEqualTo("/api/user/1");
  }

  @Test
  @DisplayName("Devrait gérer différents types d'exceptions d'authentification")
  void testCommence_DifferentExceptions() throws IOException, ServletException {
    // ARRANGE
    AuthenticationException authException = new AuthenticationException("Token expired") {};
    request.setServletPath("/api/session");

    // ACT
    authEntryPointJwt.commence(request, response, authException);

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(401);

    String jsonResponse = response.getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);

    assertThat(body.get("message")).isEqualTo("Token expired");
    assertThat(body.get("path")).isEqualTo("/api/session");
  }

  @Test
  @DisplayName("Devrait retourner le bon chemin dans la réponse")
  void testCommence_CorrectPath() throws IOException, ServletException {
    // ARRANGE
    AuthenticationException authException = new BadCredentialsException("Access denied");
    request.setServletPath("/api/auth/login");

    // ACT
    authEntryPointJwt.commence(request, response, authException);

    // ASSERT
    String jsonResponse = response.getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);

    assertThat(body.get("path")).isEqualTo("/api/auth/login");
  }

  @Test
  @DisplayName("Devrait gérer un message d'exception null")
  void testCommence_NullMessage() throws IOException, ServletException {
    // ARRANGE
    AuthenticationException authException = new AuthenticationException(null) {};
    request.setServletPath("/api/test");

    // ACT
    authEntryPointJwt.commence(request, response, authException);

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(401);

    String jsonResponse = response.getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);

    assertThat(body.get("status")).isEqualTo(401);
    assertThat(body.get("error")).isEqualTo("Unauthorized");
  }

  @Test
  @DisplayName("Devrait retourner le bon Content-Type")
  void testCommence_CorrectContentType() throws IOException, ServletException {
    // ARRANGE
    AuthenticationException authException = new BadCredentialsException("Test");

    // ACT
    authEntryPointJwt.commence(request, response, authException);

    // ASSERT
    assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
  }
}
