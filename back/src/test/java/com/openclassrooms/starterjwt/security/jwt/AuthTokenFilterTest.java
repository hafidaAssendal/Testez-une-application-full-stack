package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthTokenFilter
 *
 * Ce qu'on teste :
 * - doFilterInternal() : filtrage des requêtes et validation du JWT
 * - parseJwt() : extraction du token depuis le header Authorization
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenFilter - Tests Unitaires")
class AuthTokenFilterTest {

  @Mock
  private JwtUtils jwtUtils;

  @Mock
  private UserDetailsServiceImpl userDetailsService;

  @InjectMocks
  private AuthTokenFilter authTokenFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private MockFilterChain filterChain;
  private UserDetailsImpl userDetails;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    filterChain = new MockFilterChain();

    // Nettoyer le SecurityContext
    SecurityContextHolder.clearContext();

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

  // ========== TESTS DO FILTER INTERNAL ==========

  @Test
  @DisplayName("Devrait authentifier l'utilisateur avec un token JWT valide")
  void testDoFilterInternal_ValidToken() throws ServletException, IOException {
    // ARRANGE
    String validToken = "valid.jwt.token";
    request.addHeader("Authorization", "Bearer " + validToken);

    when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
    when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn("test@test.com");
    when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    // Vérifier que l'utilisateur est authentifié
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
    assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();

    // Vérifier que la chaîne de filtres continue
    verify(jwtUtils, times(1)).validateJwtToken(validToken);
    verify(jwtUtils, times(1)).getUserNameFromJwtToken(validToken);
    verify(userDetailsService, times(1)).loadUserByUsername("test@test.com");
  }

  @Test
  @DisplayName("Ne devrait pas authentifier avec un token invalide")
  void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
    // ARRANGE
    String invalidToken = "invalid.jwt.token";
    request.addHeader("Authorization", "Bearer " + invalidToken);

    when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    verify(jwtUtils, times(1)).validateJwtToken(invalidToken);
    verify(jwtUtils, never()).getUserNameFromJwtToken(any());
    verify(userDetailsService, never()).loadUserByUsername(any());
  }

  @Test
  @DisplayName("Ne devrait pas authentifier sans header Authorization")
  void testDoFilterInternal_NoAuthorizationHeader() throws ServletException, IOException {
    // ARRANGE
    // Pas de header Authorization

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    verify(jwtUtils, never()).validateJwtToken(any());
    verify(userDetailsService, never()).loadUserByUsername(any());
  }

  @Test
  @DisplayName("Ne devrait pas authentifier avec un header Authorization vide")
  void testDoFilterInternal_EmptyAuthorizationHeader() throws ServletException, IOException {
    // ARRANGE
    request.addHeader("Authorization", "");

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    verify(jwtUtils, never()).validateJwtToken(any());
  }

  @Test
  @DisplayName("Ne devrait pas authentifier avec un header sans Bearer")
  void testDoFilterInternal_NoBearerPrefix() throws ServletException, IOException {
    // ARRANGE
    request.addHeader("Authorization", "valid.jwt.token");

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    verify(jwtUtils, never()).validateJwtToken(any());
  }

  @Test
  @DisplayName("Devrait continuer la chaîne de filtres même en cas d'exception")
  void testDoFilterInternal_ExceptionHandling() throws ServletException, IOException {
    // ARRANGE
    String validToken = "valid.jwt.token";
    request.addHeader("Authorization", "Bearer " + validToken);

    when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
    when(jwtUtils.getUserNameFromJwtToken(validToken)).thenThrow(new RuntimeException("JWT parsing error"));

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    // L'utilisateur ne doit pas être authentifié
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    // La chaîne de filtres doit quand même continuer
    // (vérifier que ça ne lance pas d'exception)
  }

  @Test
  @DisplayName("Devrait gérer correctement le format Bearer avec espace")
  void testDoFilterInternal_BearerWithSpace() throws ServletException, IOException {
    // ARRANGE
    String validToken = "valid.jwt.token";
    request.addHeader("Authorization", "Bearer " + validToken);

    when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
    when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn("test@test.com");
    when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    verify(jwtUtils, times(1)).validateJwtToken(validToken);
  }

  @Test
  @DisplayName("Ne devrait pas authentifier si le header est 'Bearer ' sans token")
  void testDoFilterInternal_EmptyBearerToken() throws ServletException, IOException {
    // ARRANGE
    request.addHeader("Authorization", "Bearer ");

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    // Le token vide ("") sera quand même envoyé à validateJwtToken
    // On vérifie juste qu'il n'y a pas d'authentification
  }

  @Test
  @DisplayName("Devrait extraire correctement le username du token")
  void testDoFilterInternal_ExtractsCorrectUsername() throws ServletException, IOException {
    // ARRANGE
    String validToken = "valid.jwt.token";
    String expectedUsername = "john@example.com";
    request.addHeader("Authorization", "Bearer " + validToken);

    when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
    when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn(expectedUsername);
    when(userDetailsService.loadUserByUsername(expectedUsername)).thenReturn(userDetails);

    // ACT
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // ASSERT
    verify(jwtUtils, times(1)).getUserNameFromJwtToken(validToken);
    verify(userDetailsService, times(1)).loadUserByUsername(expectedUsername);
  }
}
