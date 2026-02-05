package com.openclassrooms.starterjwt.controllers.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour UserController
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ObjectMapper objectMapper;

  private User testUser;
  private User adminUser;

  @BeforeEach
  public void setUp() {
    // Nettoyer les données
    userRepository.deleteAll();

    // Créer un utilisateur standard de test
    testUser = User.builder()
      .email("user@test.com")
      .firstName("John")
      .lastName("Doe")
      .password(passwordEncoder.encode("password123"))
      .admin(false)
      .build();
    testUser = userRepository.save(testUser);

    // Créer un utilisateur admin
    adminUser = User.builder()
      .email("admin@test.com")
      .firstName("Admin")
      .lastName("User")
      .password(passwordEncoder.encode("adminpass"))
      .admin(true)
      .build();
    adminUser = userRepository.save(adminUser);
  }

  @AfterEach
  public void tearDown() {
    userRepository.deleteAll();
  }

  // ==================== Tests pour GET /api/user/{id} ====================

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("GET /api/user/{id} - Success")
  public void testFindById_Success() throws Exception {
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(testUser.getId()))
      .andExpect(jsonPath("$.email").value("user@test.com"))
      .andExpect(jsonPath("$.firstName").value("John"))
      .andExpect(jsonPath("$.lastName").value("Doe"))
      .andExpect(jsonPath("$.admin").value(false));
  }

  @Test
  @WithMockUser(username = "admin@test.com")
  @DisplayName("GET /api/user/{id} - Success for admin user")
  public void testFindById_Success_AdminUser() throws Exception {
    mockMvc.perform(get("/api/user/{id}", adminUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(adminUser.getId()))
      .andExpect(jsonPath("$.email").value("admin@test.com"))
      .andExpect(jsonPath("$.admin").value(true));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/user/{id} - Not Found")
  public void testFindById_NotFound() throws Exception {
    mockMvc.perform(get("/api/user/{id}", 9999L))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/user/{id} - Bad Request (Invalid ID)")
  public void testFindById_BadRequest_InvalidId() throws Exception {
    mockMvc.perform(get("/api/user/{id}", "invalid"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/user/{id} - Not Found  (Negative ID)")
  public void testFindById_BadRequest_NegativeId() throws Exception {
    mockMvc.perform(get("/api/user/{id}", "-1"))
      .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/user/{id} - Unauthorized (No Authentication)")
  public void testFindById_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("GET /api/user/{id} - Password field should not be present")
  public void testFindById_PasswordNotPresent() throws Exception {
    String response = mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    assertThat(response).doesNotContain("password");
    assertThat(response).doesNotContain("password123");
  }

  // ==================== Tests pour DELETE /api/user/{id} ====================

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("DELETE /api/user/{id} - Success (Own account)")
  public void testDelete_Success_OwnAccount() throws Exception {
    mockMvc.perform(delete("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk());

    // Vérifier que l'utilisateur a été supprimé
    assertThat(userRepository.findById(testUser.getId())).isEmpty();
  }

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("DELETE /api/user/{id} - Unauthorized (Different user)")
  public void testDelete_Unauthorized_DifferentUser() throws Exception {
    // Tenter de supprimer le compte admin alors qu'on est connecté en tant que user
    mockMvc.perform(delete("/api/user/{id}", adminUser.getId()))
      .andExpect(status().isUnauthorized());

    // Vérifier que l'admin n'a PAS été supprimé
    assertThat(userRepository.findById(adminUser.getId())).isPresent();
  }

  @Test
  @WithMockUser(username = "admin@test.com")
  @DisplayName("DELETE /api/user/{id} - Unauthorized (Admin cannot delete other users)")
  public void testDelete_Unauthorized_AdminCannotDeleteOthers() throws Exception {
    // Même un admin ne peut supprimer que son propre compte
    mockMvc.perform(delete("/api/user/{id}", testUser.getId()))
      .andExpect(status().isUnauthorized());

    // Vérifier que l'utilisateur n'a PAS été supprimé
    assertThat(userRepository.findById(testUser.getId())).isPresent();
  }

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("DELETE /api/user/{id} - Not Found")
  public void testDelete_NotFound() throws Exception {
    mockMvc.perform(delete("/api/user/{id}", 9999L))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("DELETE /api/user/{id} - Bad Request (Invalid ID)")
  public void testDelete_BadRequest_InvalidId() throws Exception {
    mockMvc.perform(delete("/api/user/{id}", "invalid"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("DELETE /api/user/{id} - Unauthorized (No Authentication)")
  public void testDelete_Unauthorized_NoAuth() throws Exception {
    mockMvc.perform(delete("/api/user/{id}", testUser.getId()))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests de vérification des champs ====================

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("GET /api/user/{id} - Verify all fields are returned")
  public void testFindById_VerifyAllFields() throws Exception {
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.email").exists())
      .andExpect(jsonPath("$.firstName").exists())
      .andExpect(jsonPath("$.lastName").exists())
      .andExpect(jsonPath("$.admin").exists())
      .andExpect(jsonPath("$.createdAt").exists())
      .andExpect(jsonPath("$.updatedAt").exists());
  }

  // ==================== Tests de cas limites ====================

  @Test
  @WithMockUser(username = "special@test.com")
  @DisplayName("GET /api/user/{id} - User with special characters in name")
  public void testFindById_SpecialCharactersInName() throws Exception {
    User specialUser = User.builder()
      .email("special@test.com")
      .firstName("François")
      .lastName("O'Brien")
      .password(passwordEncoder.encode("password"))
      .admin(false)
      .build();
    specialUser = userRepository.save(specialUser);

    mockMvc.perform(get("/api/user/{id}", specialUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.firstName").value("François"))
      .andExpect(jsonPath("$.lastName").value("O'Brien"));
  }

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("GET /api/user/{id} - Very large ID number")
  public void testFindById_VeryLargeId() throws Exception {
    mockMvc.perform(get("/api/user/{id}", Long.MAX_VALUE))
      .andExpect(status().isNotFound());
  }

  // ==================== Tests de scénarios complets ====================

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("Scenario: Get user info then delete account")
  public void testScenario_GetThenDelete() throws Exception {
    // 1. Récupérer les infos de l'utilisateur
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("user@test.com"));

    // 2. Supprimer le compte
    mockMvc.perform(delete("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk());

    // 3. Vérifier que le compte n'existe plus
    assertThat(userRepository.findById(testUser.getId())).isEmpty();
  }

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("Scenario: Verify user cannot access after deletion")
  public void testScenario_CannotAccessAfterDeletion() throws Exception {
    // 1. Vérifier que l'utilisateur existe
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk());

    // 2. Supprimer l'utilisateur
    mockMvc.perform(delete("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk());

    // 3. Tenter d'accéder à nouveau (devrait échouer car user n'existe plus)
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "hacker@test.com")
  @DisplayName("Security: User cannot delete another user's account")
  public void testSecurity_CannotDeleteOtherAccount() throws Exception {
    // Créer un "hacker" qui essaie de supprimer le compte de testUser
    User hacker = User.builder()
      .email("hacker@test.com")
      .firstName("Hacker")
      .lastName("Bad")
      .password(passwordEncoder.encode("password"))
      .admin(false)
      .build();
    hacker = userRepository.save(hacker);

    // Tenter de supprimer le compte de testUser
    mockMvc.perform(delete("/api/user/{id}", testUser.getId()))
      .andExpect(status().isUnauthorized());

    // Vérifier que testUser existe toujours
    assertThat(userRepository.findById(testUser.getId())).isPresent();
  }

  @Test
  @WithMockUser(username = "admin@test.com")
  @DisplayName("Security: Admin can only delete own account")
  public void testSecurity_AdminCanOnlyDeleteOwnAccount() throws Exception {
    // L'admin ne peut pas supprimer le compte d'un autre utilisateur
    mockMvc.perform(delete("/api/user/{id}", testUser.getId()))
      .andExpect(status().isUnauthorized());

    // Vérifier que testUser existe toujours
    assertThat(userRepository.findById(testUser.getId())).isPresent();

    // Mais l'admin peut supprimer son propre compte
    mockMvc.perform(delete("/api/user/{id}", adminUser.getId()))
      .andExpect(status().isOk());

    // Vérifier que l'admin a été supprimé
    assertThat(userRepository.findById(adminUser.getId())).isEmpty();
  }

  // ==================== Tests de validation de données ====================

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("GET /api/user/{id} - Verify email format")
  public void testFindById_VerifyEmailFormat() throws Exception {
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value(containsString("@")));
  }

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("GET /api/user/{id} - Verify boolean admin field")
  public void testFindById_VerifyAdminField() throws Exception {
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.admin").isBoolean())
      .andExpect(jsonPath("$.admin").value(false));
  }

  @Test
  @WithMockUser(username = "admin@test.com")
  @DisplayName("GET /api/user/{id} - Admin field is true for admin")
  public void testFindById_AdminFieldTrue() throws Exception {
    mockMvc.perform(get("/api/user/{id}", adminUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.admin").value(true));
  }

  // ==================== Tests de timestamps ====================

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("GET /api/user/{id} - Verify timestamps exist")
  public void testFindById_VerifyTimestamps() throws Exception {
    mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.createdAt").isNotEmpty())
      .andExpect(jsonPath("$.updatedAt").isNotEmpty());
  }

  // ==================== Tests de concurrence ====================

  @Test
  @WithMockUser(username = "concurrent@test.com")
  @DisplayName("Concurrent delete attempts - Only first should succeed")
  public void testConcurrentDelete() throws Exception {
    User concurrentUser = User.builder()
      .email("concurrent@test.com")
      .firstName("Concurrent")
      .lastName("Test")
      .password(passwordEncoder.encode("password"))
      .admin(false)
      .build();
    concurrentUser = userRepository.save(concurrentUser);

    Long userId = concurrentUser.getId();

    // Première suppression - devrait réussir
    mockMvc.perform(delete("/api/user/{id}", userId))
      .andExpect(status().isOk());

    // Deuxième suppression du même utilisateur - devrait échouer (Not Found)
    mockMvc.perform(delete("/api/user/{id}", userId))
      .andExpect(status().isNotFound());
  }

  // ==================== Tests d'intégrité ====================

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("DELETE - Verify user is completely removed from database")
  public void testDelete_VerifyCompleteRemoval() throws Exception {
    Long userId = testUser.getId();

    // Supprimer l'utilisateur
    mockMvc.perform(delete("/api/user/{id}", userId))
      .andExpect(status().isOk());

    // Vérifier avec findById
    assertThat(userRepository.findById(userId)).isEmpty();

    // Vérifier avec findByEmail
    assertThat(userRepository.findByEmail("user@test.com")).isEmpty();

    // Vérifier le count total
    long count = userRepository.count();
    assertThat(count).isEqualTo(1); // Seulement adminUser reste
  }

  @Test
  @WithMockUser(username = "user@test.com")
  @DisplayName("GET - Multiple consecutive calls return same data")
  public void testFindById_ConsistentData() throws Exception {
    // Appel 1
    String response1 = mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    // Appel 2
    String response2 = mockMvc.perform(get("/api/user/{id}", testUser.getId()))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    // Les réponses doivent être identiques
    assertThat(response1).isEqualTo(response2);
  }
}
