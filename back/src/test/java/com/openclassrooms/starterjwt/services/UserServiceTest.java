package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - UserService")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {

    testUser = User.builder()
      .id(1L)
      .email("test@example.com")
      .firstName("Jean")
      .lastName("Dupuis")
      .password("hashedPassword123")
      .admin(false)
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();
  }


  @Test
  @DisplayName("delete() doit supprimer un utilisateur par son ID")
  void testDelete_ShouldCallRepositoryDeleteById() {

    Long userId = 1L;

    // doNothing() est optionnel pour les méthodes void
     doNothing().when(userRepository).deleteById(userId);

    userService.delete(userId);
    verify(userRepository, times(1)).deleteById(userId);
  }

  @Test
  @DisplayName("delete() doit gérer correctement un ID null")
  void testDelete_ShouldHandleNullId() {

    doNothing().when(userRepository).deleteById(null);

    userService.delete(null);

    verify(userRepository, times(1)).deleteById(null);
  }

  @Test
  @DisplayName("findById() doit retourner l'utilisateur correspondant à l'ID")
  void testFindById_ShouldReturnUser_WhenIdExists() {

    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    User actualUser = userService.findById(userId);

    assertThat(actualUser).isNotNull();
    assertThat(actualUser.getId()).isEqualTo(userId);
    assertThat(actualUser.getEmail()).isEqualTo("test@example.com");
    assertThat(actualUser.getFirstName()).isEqualTo("Jean");
    assertThat(actualUser.getLastName()).isEqualTo("Dupuis");
    assertThat(actualUser.isAdmin()).isFalse();

    verify(userRepository, times(1)).findById(userId);
  }


  @Test
  @DisplayName("findById() doit retourner null si l'ID n'existe pas")
  void testFindById_ShouldReturnNull_WhenIdDoesNotExist() {

    Long nonExistentId = 999L;
    when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());


    User actualUser = userService.findById(nonExistentId);


    assertThat(actualUser).isNull();
    verify(userRepository, times(1)).findById(nonExistentId);
  }


  @Test
  @DisplayName("findById() doit gérer correctement un ID null")
  void testFindById_ShouldHandleNullId() {

    when(userRepository.findById(null)).thenReturn(Optional.empty());

    User actualUser = userService.findById(null);

    assertThat(actualUser).isNull();
    verify(userRepository, times(1)).findById(null);
  }


  @Test
  @DisplayName("findById() doit retourner correctement un utilisateur admin")
  void testFindById_ShouldReturnAdminUser() {

    User adminUser = User.builder()
      .id(2L)
      .email("admin@example.com")
      .firstName("Admin")
      .lastName("Système")
      .password("hashedAdminPassword")
      .admin(true)
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));


    User actualUser = userService.findById(2L);


    assertThat(actualUser).isNotNull();
    assertThat(actualUser.isAdmin()).isTrue();
    verify(userRepository, times(1)).findById(2L);
  }
}
