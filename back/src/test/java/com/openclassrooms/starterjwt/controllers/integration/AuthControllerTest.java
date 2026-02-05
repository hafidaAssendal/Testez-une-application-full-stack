package com.openclassrooms.starterjwt.controllers.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

  /**
   * Tests d'intégration pour AuthController
   * Teste les endpoints /api/auth/login et /api/auth/register
   */
  @SpringBootTest
  @AutoConfigureMockMvc
  public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String testPassword = "password123";

    @BeforeEach
    public void setUp() {
      // Nettoyer la base de données avant chaque test
      userRepository.deleteAll();

      // Créer un utilisateur de test
      testUser = User.builder()
        .email("test@test.com")
        .firstName("John")
        .lastName("Doe")
        .password(passwordEncoder.encode(testPassword))
        .admin(false)
        .build();

      userRepository.save(testUser);
    }

    @AfterEach
    public void tearDown() {
      // Nettoyer la base de données après chaque test
      userRepository.deleteAll();
    }

    // ==================== Tests pour /api/auth/login ====================

    @Test
    @DisplayName("Login successful - User authentifié avec succès")
    public void testLogin_Success() throws Exception {
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("test@test.com");
      loginRequest.setPassword(testPassword);

      mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.type").value("Bearer"))
        .andExpect(jsonPath("$.id").value(testUser.getId()))
        .andExpect(jsonPath("$.username").value("test@test.com"))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    @DisplayName("Login successful - Admin authentifié avec succès")
    public void testLogin_AdminUser_Success() throws Exception {
      // Créer un utilisateur admin
      User adminUser = User.builder()
        .email("admin@test.com")
        .firstName("Admin")
        .lastName("User")
        .password(passwordEncoder.encode("adminpass"))
        .admin(true)
        .build();
      userRepository.save(adminUser);

      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("admin@test.com");
      loginRequest.setPassword("adminpass");

      mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.admin").value(true));
    }

    @Test
    @DisplayName("Login failed - Mot de passe incorrect")
    public void testLogin_WrongPassword_Failure() throws Exception {
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("test@test.com");
      loginRequest.setPassword("wrongpassword");

      mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login failed - Email inexistant")
    public void testLogin_NonExistentEmail_Failure() throws Exception {
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("nonexistent@test.com");
      loginRequest.setPassword(testPassword);

      mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login failed - Email vide")
    public void testLogin_EmptyEmail_Failure() throws Exception {
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("");
      loginRequest.setPassword(testPassword);

      mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login failed - Password vide")
    public void testLogin_EmptyPassword_Failure() throws Exception {
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("test@test.com");
      loginRequest.setPassword("");

      mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login failed - Requête invalide (champs manquants)")
    public void testLogin_InvalidRequest_Failure() throws Exception {
      String invalidJson = "{}";

      mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(invalidJson))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login - Vérifier que le JWT token est valide")
    public void testLogin_JwtTokenIsValid() throws Exception {
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("test@test.com");
      loginRequest.setPassword(testPassword);

      MvcResult result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn();

      String responseContent = result.getResponse().getContentAsString();
      assertThat(responseContent).contains("token");
      assertThat(responseContent).contains("Bearer");

      // Vérifier que le token a une longueur raisonnable (JWT typique)
      String token = objectMapper.readTree(responseContent).get("token").asText();
      assertThat(token).isNotEmpty();
      assertThat(token.length()).isGreaterThan(50);
    }

    // ==================== Tests pour /api/auth/register ====================

    @Test
    @DisplayName("Register successful - Nouvel utilisateur créé avec succès")
    public void testRegister_Success() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("newuser@test.com");
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("User registered successfully!"));

      // Vérifier que l'utilisateur a été créé dans la base de données
      User createdUser = userRepository.findByEmail("newuser@test.com").orElse(null);
      assertThat(createdUser).isNotNull();
      assertThat(createdUser.getEmail()).isEqualTo("newuser@test.com");
      assertThat(createdUser.getFirstName()).isEqualTo("Jane");
      assertThat(createdUser.getLastName()).isEqualTo("Smith");
      assertThat(createdUser.isAdmin()).isFalse();

      // Vérifier que le mot de passe est encodé
      assertThat(createdUser.getPassword()).isNotEqualTo("password123");
      assertThat(passwordEncoder.matches("password123", createdUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Register failed - Email déjà utilisé")
    public void testRegister_EmailAlreadyExists_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("test@test.com"); // Email déjà existant
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Error: Email is already taken!"));

      // Vérifier qu'aucun nouvel utilisateur n'a été créé
      long userCount = userRepository.count();
      assertThat(userCount).isEqualTo(1); // Seulement l'utilisateur de test du setUp
    }

    @Test
    @DisplayName("Register failed - Email invalide")
    public void testRegister_InvalidEmail_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("invalidemail"); // Email sans @
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register failed - Email vide")
    public void testRegister_EmptyEmail_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("");
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register failed - FirstName trop court")
    public void testRegister_FirstNameTooShort_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("newuser@test.com");
      signupRequest.setFirstName("Jo"); // Moins de 3 caractères
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register failed - FirstName trop long")
    public void testRegister_FirstNameTooLong_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("newuser@test.com");
      signupRequest.setFirstName("ThisNameIsWayTooLongAndExceedsTheMaximumLimit"); // Plus de 20 caractères
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register failed - LastName vide")
    public void testRegister_EmptyLastName_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("newuser@test.com");
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register failed - Password trop court")
    public void testRegister_PasswordTooShort_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("newuser@test.com");
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("12345"); // Moins de 6 caractères

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register failed - Password trop long")
    public void testRegister_PasswordTooLong_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("newuser@test.com");
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("ThisPasswordIsWayTooLongAndExceedsTheMaximumLimitOf40Characters1234567890"); // Plus de 40 caractères

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register failed - Tous les champs vides")
    public void testRegister_AllFieldsEmpty_Failure() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("");
      signupRequest.setFirstName("");
      signupRequest.setLastName("");
      signupRequest.setPassword("");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Vérifier que l'utilisateur n'est pas admin par défaut")
    public void testRegister_UserNotAdminByDefault() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("newuser@test.com");
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk());

      User createdUser = userRepository.findByEmail("newuser@test.com").orElse(null);
      assertThat(createdUser).isNotNull();
      assertThat(createdUser.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Register puis Login - Scénario complet")
    public void testRegister_ThenLogin_Success() throws Exception {
      // Étape 1 : Créer un nouvel utilisateur
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("newuser@test.com");
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("User registered successfully!"));

      // Étape 2 : Se connecter avec le nouvel utilisateur
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("newuser@test.com");
      loginRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.username").value("newuser@test.com"))
        .andExpect(jsonPath("$.firstName").value("Jane"))
        .andExpect(jsonPath("$.lastName").value("Smith"))
        .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    @DisplayName("Register avec email aux limites de taille maximale")
    public void testRegister_EmailMaxSize_Success() throws Exception {
      // Email de 50 caractères (limite max)
      String maxEmail = "a123456789012345678901234567890123456@test.com"; // 50 caractères

      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail(maxEmail);
      signupRequest.setFirstName("Jane");
      signupRequest.setLastName("Smith");
      signupRequest.setPassword("password123");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    @DisplayName("Register avec noms aux limites de taille")
    public void testRegister_NamesAtBoundaries_Success() throws Exception {
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setEmail("boundary@test.com");
      signupRequest.setFirstName("Jan"); // 3 caractères (minimum)
      signupRequest.setLastName("Smi"); // 3 caractères (minimum)
      signupRequest.setPassword("pass12"); // 6 caractères (minimum)

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    @DisplayName("Login - Vérifier le format du token JWT")
    public void testLogin_VerifyJwtTokenFormat() throws Exception {
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setEmail("test@test.com");
      loginRequest.setPassword(testPassword);

      MvcResult result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn();

      String responseContent = result.getResponse().getContentAsString();
      String token = objectMapper.readTree(responseContent).get("token").asText();

      // Vérifier que le token JWT a 3 parties séparées par des points
      String[] tokenParts = token.split("\\.");
      assertThat(tokenParts).hasSize(3);
    }
  }

