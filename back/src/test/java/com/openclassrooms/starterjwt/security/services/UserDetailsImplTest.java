package com.openclassrooms.starterjwt.security.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour UserDetailsImpl
 *
 * Ce qu'on teste :
 * - Construction avec Builder
 * - Getters
 * - Méthodes de UserDetails (isAccountNonExpired, etc.)
 * - equals() et hashCode()
 */
@DisplayName("UserDetailsImpl - Tests Unitaires")
class UserDetailsImplTest {

  private UserDetailsImpl userDetails;

  @BeforeEach
  void setUp() {
    userDetails = UserDetailsImpl.builder()
      .id(1L)
      .username("test@test.com")
      .firstName("John")
      .lastName("Doe")
      .password("password123")
      .admin(false)
      .build();
  }

  // ========== TESTS CONSTRUCTION ==========

  @Test
  @DisplayName("Devrait créer un UserDetailsImpl avec le builder")
  void testBuilder() {
    // ASSERT
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getId()).isEqualTo(1L);
    assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
    assertThat(userDetails.getFirstName()).isEqualTo("John");
    assertThat(userDetails.getLastName()).isEqualTo("Doe");
    assertThat(userDetails.getPassword()).isEqualTo("password123");
    assertThat(userDetails.getAdmin()).isFalse();
  }

  @Test
  @DisplayName("Devrait créer un UserDetailsImpl admin")
  void testBuilder_Admin() {
    // ARRANGE & ACT
    UserDetailsImpl admin = UserDetailsImpl.builder()
      .id(2L)
      .username("admin@test.com")
      .firstName("Admin")
      .lastName("User")
      .password("adminpass")
      .admin(true)
      .build();

    // ASSERT
    assertThat(admin.getAdmin()).isTrue();
  }

  // ========== TESTS GETTERS ==========

  @Test
  @DisplayName("getId devrait retourner l'ID")
  void testGetId() {
    assertThat(userDetails.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("getUsername devrait retourner l'email")
  void testGetUsername() {
    assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
  }

  @Test
  @DisplayName("getFirstName devrait retourner le prénom")
  void testGetFirstName() {
    assertThat(userDetails.getFirstName()).isEqualTo("John");
  }

  @Test
  @DisplayName("getLastName devrait retourner le nom")
  void testGetLastName() {
    assertThat(userDetails.getLastName()).isEqualTo("Doe");
  }

  @Test
  @DisplayName("getPassword devrait retourner le mot de passe")
  void testGetPassword() {
    assertThat(userDetails.getPassword()).isEqualTo("password123");
  }

  @Test
  @DisplayName("getAdmin devrait retourner le statut admin")
  void testGetAdmin() {
    assertThat(userDetails.getAdmin()).isFalse();
  }

  // ========== TESTS USER DETAILS METHODS ==========

  @Test
  @DisplayName("getAuthorities devrait retourner une collection vide")
  void testGetAuthorities() {
    // ACT
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

    // ASSERT
    assertThat(authorities).isNotNull();
    assertThat(authorities).isEmpty();
  }

  @Test
  @DisplayName("isAccountNonExpired devrait retourner true")
  void testIsAccountNonExpired() {
    assertThat(userDetails.isAccountNonExpired()).isTrue();
  }

  @Test
  @DisplayName("isAccountNonLocked devrait retourner true")
  void testIsAccountNonLocked() {
    assertThat(userDetails.isAccountNonLocked()).isTrue();
  }

  @Test
  @DisplayName("isCredentialsNonExpired devrait retourner true")
  void testIsCredentialsNonExpired() {
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
  }

  @Test
  @DisplayName("isEnabled devrait retourner true")
  void testIsEnabled() {
    assertThat(userDetails.isEnabled()).isTrue();
  }

  // ========== TESTS EQUALS & HASHCODE ==========

  @Test
  @DisplayName("Deux UserDetailsImpl avec le même ID doivent être égaux")
  void testEquals_SameId() {
    // ARRANGE
    UserDetailsImpl user1 = UserDetailsImpl.builder()
      .id(1L)
      .username("test1@test.com")
      .firstName("John")
      .lastName("Doe")
      .password("pass1")
      .admin(false)
      .build();

    UserDetailsImpl user2 = UserDetailsImpl.builder()
      .id(1L)
      .username("test2@test.com")
      .firstName("Jane")
      .lastName("Smith")
      .password("pass2")
      .admin(true)
      .build();

    // ASSERT
    assertThat(user1).isEqualTo(user2);
  }

  @Test
  @DisplayName("Deux UserDetailsImpl avec des IDs différents ne doivent pas être égaux")
  void testEquals_DifferentId() {
    // ARRANGE
    UserDetailsImpl user1 = UserDetailsImpl.builder()
      .id(1L)
      .username("test@test.com")
      .firstName("John")
      .lastName("Doe")
      .password("pass")
      .admin(false)
      .build();

    UserDetailsImpl user2 = UserDetailsImpl.builder()
      .id(2L)
      .username("test@test.com")
      .firstName("John")
      .lastName("Doe")
      .password("pass")
      .admin(false)
      .build();

    // ASSERT
    assertThat(user1).isNotEqualTo(user2);
  }

  @Test
  @DisplayName("equals avec lui-même devrait retourner true")
  void testEquals_SameObject() {
    assertThat(userDetails).isEqualTo(userDetails);
  }

  @Test
  @DisplayName("equals avec null devrait retourner false")
  void testEquals_Null() {
    assertThat(userDetails).isNotEqualTo(null);
  }

  @Test
  @DisplayName("equals avec un objet d'une autre classe devrait retourner false")
  void testEquals_DifferentClass() {
    assertThat(userDetails).isNotEqualTo("Not a UserDetailsImpl");
  }

  @Test
  @DisplayName("Devrait créer UserDetailsImpl avec des valeurs null")
  void testBuilder_WithNullValues() {
    // ARRANGE & ACT
    UserDetailsImpl userWithNulls = UserDetailsImpl.builder()
      .id(null)
      .username(null)
      .firstName(null)
      .lastName(null)
      .password(null)
      .admin(null)
      .build();

    // ASSERT
    assertThat(userWithNulls).isNotNull();
    assertThat(userWithNulls.getId()).isNull();
    assertThat(userWithNulls.getUsername()).isNull();
    assertThat(userWithNulls.getAdmin()).isNull();
  }
}
