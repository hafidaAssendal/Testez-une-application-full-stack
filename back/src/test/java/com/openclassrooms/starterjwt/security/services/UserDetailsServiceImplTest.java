package com.openclassrooms.starterjwt.security.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserDetailsServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl - Tests Unitaires")
class UserDetailsServiceImplTest {

  @Mock
  private UserRepository userRepository;

  private UserDetailsServiceImpl userDetailsService;

  private User user;

  @BeforeEach
  void setUp() {
    // Créer le service avec le repository mocké
    userDetailsService = new UserDetailsServiceImpl(userRepository);

    // Créer un utilisateur de test
    user = new User();
    user.setId(1L);
    user.setEmail("test@test.com");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setPassword("password123");
    user.setAdmin(false);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("Devrait charger un utilisateur par son email")
  void testLoadUserByUsername_Success() {
    // ARRANGE
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

    // ACT
    UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

    // ASSERT
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
    assertThat(userDetails.getPassword()).isEqualTo("password123");

    assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);

    UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
    assertThat(userDetailsImpl.getId()).isEqualTo(1L);
    assertThat(userDetailsImpl.getFirstName()).isEqualTo("John");
    assertThat(userDetailsImpl.getLastName()).isEqualTo("Doe");

    verify(userRepository, times(1)).findByEmail("test@test.com");
  }

  @Test
  @DisplayName("Devrait lancer UsernameNotFoundException si l'utilisateur n'existe pas")
  void testLoadUserByUsername_UserNotFound() {
    // ARRANGE
    when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

    // ACT & ASSERT
    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("notfound@test.com"))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessageContaining("User Not Found with email: notfound@test.com");

    verify(userRepository, times(1)).findByEmail("notfound@test.com");
  }

  @Test
  @DisplayName("Devrait charger un utilisateur admin")
  void testLoadUserByUsername_AdminUser() {
    // ARRANGE
    user.setAdmin(true);
    user.setEmail("admin@test.com");
    when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

    // ACT
    UserDetails userDetails = userDetailsService.loadUserByUsername("admin@test.com");

    // ASSERT
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo("admin@test.com");
  }

  @Test
  @DisplayName("Devrait mapper correctement tous les champs de User vers UserDetailsImpl")
  void testLoadUserByUsername_CorrectMapping() {
    // ARRANGE
    user.setEmail("marie@yoga.com");
    user.setFirstName("Marie");
    user.setLastName("Martin");
    user.setPassword("securePassword");
    user.setAdmin(true);

    when(userRepository.findByEmail("marie@yoga.com")).thenReturn(Optional.of(user));

    // ACT
    UserDetails userDetails = userDetailsService.loadUserByUsername("marie@yoga.com");

    // ASSERT
    assertThat(userDetails).isNotNull();
    UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
    assertThat(userDetailsImpl.getId()).isEqualTo(1L);
    assertThat(userDetailsImpl.getUsername()).isEqualTo("marie@yoga.com");
    assertThat(userDetailsImpl.getFirstName()).isEqualTo("Marie");
    assertThat(userDetailsImpl.getLastName()).isEqualTo("Martin");
    assertThat(userDetailsImpl.getPassword()).isEqualTo("securePassword");
  }

  @Test
  @DisplayName("Devrait gérer des emails avec différents formats")
  void testLoadUserByUsername_DifferentEmailFormats() {
    // ARRANGE
    String email = "simple@test.com";
    user.setEmail(email);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    // ACT
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    // ASSERT
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(email);
  }

  @Test
  @DisplayName("Devrait lancer une exception avec un message personnalisé")
  void testLoadUserByUsername_CustomExceptionMessage() {
    // ARRANGE
    String email = "nonexistent@test.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // ACT & ASSERT
    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessage("User Not Found with email: " + email);
  }

  @Test
  @DisplayName("Devrait appeler le repository une seule fois")
  void testLoadUserByUsername_SingleRepositoryCall() {
    // ARRANGE
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

    // ACT
    userDetailsService.loadUserByUsername("test@test.com");

    // ASSERT
    verify(userRepository, times(1)).findByEmail("test@test.com");
    verifyNoMoreInteractions(userRepository);
  }
}
