package com.openclassrooms.starterjwt.controllers.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour SessionController
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SessionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private TeacherRepository teacherRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private Teacher testTeacher;
  private User testUser;
  private Session testSession;

  @BeforeEach
  public void setUp() {
    // Nettoyer les données
    sessionRepository.deleteAll();
    userRepository.deleteAll();
    teacherRepository.deleteAll();

    // Créer un teacher de test
    testTeacher = Teacher.builder()
      .firstName("John")
      .lastName("Doe")
      .build();
    testTeacher = teacherRepository.save(testTeacher);

    // Créer un utilisateur de test
    testUser = User.builder()
      .email("user@test.com")
      .firstName("User")
      .lastName("Test")
      .password("password")
      .admin(false)
      .build();
    testUser = userRepository.save(testUser);

    // Créer une session de test
    testSession = Session.builder()
      .name("Yoga Session")
      .date(new Date())
      .description("A great yoga session")
      .teacher(testTeacher)
      .users(new ArrayList<>())
      .build();
    testSession = sessionRepository.save(testSession);
  }

  @AfterEach
  public void tearDown() {
    sessionRepository.deleteAll();
    userRepository.deleteAll();
    teacherRepository.deleteAll();
  }

  // ==================== Tests pour GET /api/session/{id} ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/session/{id} - Success")
  public void testFindById_Success() throws Exception {
    mockMvc.perform(get("/api/session/{id}", testSession.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(testSession.getId()))
      .andExpect(jsonPath("$.name").value("Yoga Session"))
      .andExpect(jsonPath("$.description").value("A great yoga session"))
      .andExpect(jsonPath("$.teacher_id").value(testTeacher.getId()));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/session/{id} - Not Found")
  public void testFindById_NotFound() throws Exception {
    mockMvc.perform(get("/api/session/{id}", 9999L))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/session/{id} - Bad Request (Invalid ID)")
  public void testFindById_BadRequest_InvalidId() throws Exception {
    mockMvc.perform(get("/api/session/{id}", "invalid"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/session/{id} - Unauthorized (No Authentication)")
  public void testFindById_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/session/{id}", testSession.getId()))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests pour GET /api/session ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/session - Success with multiple sessions")
  public void testFindAll_Success() throws Exception {
    // Créer une deuxième session
    Session session2 = Session.builder()
      .name("Pilates Session")
      .date(new Date())
      .description("Pilates class")
      .teacher(testTeacher)
      .users(new ArrayList<>())
      .build();
    sessionRepository.save(session2);

    mockMvc.perform(get("/api/session"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(2)))
      .andExpect(jsonPath("$[0].name").value("Yoga Session"))
      .andExpect(jsonPath("$[1].name").value("Pilates Session"));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/session - Empty list")
  public void testFindAll_EmptyList() throws Exception {
    sessionRepository.deleteAll();

    mockMvc.perform(get("/api/session"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("GET /api/session - Unauthorized")
  public void testFindAll_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/session"))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests pour POST /api/session ====================

  @Test
  @WithMockUser
  @DisplayName("POST /api/session - Success")
  public void testCreate_Success() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("New Yoga Session");
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Brand new session");
    sessionDto.setUsers(new ArrayList<>());

    mockMvc.perform(post("/api/session")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("New Yoga Session"))
      .andExpect(jsonPath("$.description").value("Brand new session"))
      .andExpect(jsonPath("$.teacher_id").value(testTeacher.getId()));

    // Vérifier que la session a été créée
    List<Session> sessions = sessionRepository.findAll();
    assertThat(sessions).hasSize(2); // testSession + nouvelle session
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session - Validation error (name blank)")
  public void testCreate_ValidationError_NameBlank() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName(""); // Nom vide
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Description");

    mockMvc.perform(post("/api/session")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session - Validation error (name too long)")
  public void testCreate_ValidationError_NameTooLong() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("A".repeat(51)); // Plus de 50 caractères
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Description");

    mockMvc.perform(post("/api/session")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session - Validation error (date null)")
  public void testCreate_ValidationError_DateNull() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("Session");
    sessionDto.setDate(null); // Date null
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Description");

    mockMvc.perform(post("/api/session")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session - Validation error (description too long)")
  public void testCreate_ValidationError_DescriptionTooLong() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("Session");
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("A".repeat(2501)); // Plus de 2500 caractères

    mockMvc.perform(post("/api/session")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/session - Unauthorized")
  public void testCreate_Unauthorized() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("Session");
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Description");

    mockMvc.perform(post("/api/session")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests pour PUT /api/session/{id} ====================

  @Test
  @WithMockUser
  @DisplayName("PUT /api/session/{id} - Success")
  public void testUpdate_Success() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("Updated Session");
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Updated description");

    mockMvc.perform(put("/api/session/{id}", testSession.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Updated Session"))
      .andExpect(jsonPath("$.description").value("Updated description"));

    // Vérifier en base de données
    Session updatedSession = sessionRepository.findById(testSession.getId()).orElse(null);
    assertThat(updatedSession).isNotNull();
    assertThat(updatedSession.getName()).isEqualTo("Updated Session");
  }

  @Test
  @WithMockUser
  @DisplayName("PUT /api/session/{id} - Bad Request (Invalid ID)")
  public void testUpdate_BadRequest_InvalidId() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("Updated Session");
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Updated description");

    mockMvc.perform(put("/api/session/{id}", "invalid")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("PUT /api/session/{id} - Validation error")
  public void testUpdate_ValidationError() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName(""); // Nom vide
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Description");

    mockMvc.perform(put("/api/session/{id}", testSession.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PUT /api/session/{id} - Unauthorized")
  public void testUpdate_Unauthorized() throws Exception {
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("Updated Session");
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Updated description");

    mockMvc.perform(put("/api/session/{id}", testSession.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests pour DELETE /api/session/{id} ====================

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/session/{id} - Success")
  public void testDelete_Success() throws Exception {
    mockMvc.perform(delete("/api/session/{id}", testSession.getId()))
      .andExpect(status().isOk());

    // Vérifier que la session a été supprimée
    assertThat(sessionRepository.findById(testSession.getId())).isEmpty();
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/session/{id} - Not Found")
  public void testDelete_NotFound() throws Exception {
    mockMvc.perform(delete("/api/session/{id}", 9999L))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/session/{id} - Bad Request (Invalid ID)")
  public void testDelete_BadRequest_InvalidId() throws Exception {
    mockMvc.perform(delete("/api/session/{id}", "invalid"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("DELETE /api/session/{id} - Unauthorized")
  public void testDelete_Unauthorized() throws Exception {
    mockMvc.perform(delete("/api/session/{id}", testSession.getId()))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests pour POST /api/session/{id}/participate/{userId} ====================

  @Test
  @WithMockUser
  @DisplayName("POST /api/session/{id}/participate/{userId} - Success")
  public void testParticipate_Success() throws Exception {
    mockMvc.perform(post("/api/session/{id}/participate/{userId}",
        testSession.getId(), testUser.getId()))
      .andExpect(status().isOk());

    // Vérifier que l'utilisateur a été ajouté
    Session session = sessionRepository.findById(testSession.getId()).orElse(null);
    assertThat(session).isNotNull();
    assertThat(session.getUsers()).hasSize(1);
    assertThat(session.getUsers().get(0).getId()).isEqualTo(testUser.getId());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session/{id}/participate/{userId} - Bad Request (Already participating)")
  public void testParticipate_BadRequest_AlreadyParticipating() throws Exception {
    // Ajouter l'utilisateur une première fois
    testSession.getUsers().add(testUser);
    sessionRepository.save(testSession);

    // Tenter de l'ajouter à nouveau
    mockMvc.perform(post("/api/session/{id}/participate/{userId}",
        testSession.getId(), testUser.getId()))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session/{id}/participate/{userId} - Not Found (Session)")
  public void testParticipate_NotFound_Session() throws Exception {
    mockMvc.perform(post("/api/session/{id}/participate/{userId}",
        9999L, testUser.getId()))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session/{id}/participate/{userId} - Not Found (User)")
  public void testParticipate_NotFound_User() throws Exception {
    mockMvc.perform(post("/api/session/{id}/participate/{userId}",
        testSession.getId(), 9999L))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session/{id}/participate/{userId} - Bad Request (Invalid session ID)")
  public void testParticipate_BadRequest_InvalidSessionId() throws Exception {
    mockMvc.perform(post("/api/session/{id}/participate/{userId}",
        "invalid", testUser.getId()))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/session/{id}/participate/{userId} - Bad Request (Invalid user ID)")
  public void testParticipate_BadRequest_InvalidUserId() throws Exception {
    mockMvc.perform(post("/api/session/{id}/participate/{userId}",
        testSession.getId(), "invalid"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/session/{id}/participate/{userId} - Unauthorized")
  public void testParticipate_Unauthorized() throws Exception {
    mockMvc.perform(post("/api/session/{id}/participate/{userId}",
        testSession.getId(), testUser.getId()))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests pour DELETE /api/session/{id}/participate/{userId} ====================

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/session/{id}/participate/{userId} - Success")
  public void testNoLongerParticipate_Success() throws Exception {
    // Ajouter l'utilisateur à la session
    testSession.getUsers().add(testUser);
    sessionRepository.save(testSession);

    mockMvc.perform(delete("/api/session/{id}/participate/{userId}",
        testSession.getId(), testUser.getId()))
      .andExpect(status().isOk());

    // Vérifier que l'utilisateur a été retiré
    Session session = sessionRepository.findById(testSession.getId()).orElse(null);
    assertThat(session).isNotNull();
    assertThat(session.getUsers()).isEmpty();
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/session/{id}/participate/{userId} - Bad Request (Not participating)")
  public void testNoLongerParticipate_BadRequest_NotParticipating() throws Exception {
    mockMvc.perform(delete("/api/session/{id}/participate/{userId}",
        testSession.getId(), testUser.getId()))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/session/{id}/participate/{userId} - Not Found (Session)")
  public void testNoLongerParticipate_NotFound_Session() throws Exception {
    mockMvc.perform(delete("/api/session/{id}/participate/{userId}",
        9999L, testUser.getId()))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/session/{id}/participate/{userId} - Bad Request (Invalid session ID)")
  public void testNoLongerParticipate_BadRequest_InvalidSessionId() throws Exception {
    mockMvc.perform(delete("/api/session/{id}/participate/{userId}",
        "invalid", testUser.getId()))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/session/{id}/participate/{userId} - Bad Request (Invalid user ID)")
  public void testNoLongerParticipate_BadRequest_InvalidUserId() throws Exception {
    mockMvc.perform(delete("/api/session/{id}/participate/{userId}",
        testSession.getId(), "invalid"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("DELETE /api/session/{id}/participate/{userId} - Unauthorized")
  public void testNoLongerParticipate_Unauthorized() throws Exception {
    mockMvc.perform(delete("/api/session/{id}/participate/{userId}",
        testSession.getId(), testUser.getId()))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests de scénarios complets ====================

  @Test
  @WithMockUser
  @DisplayName("Scenario: Create session, add user, remove user, delete session")
  public void testCompleteScenario() throws Exception {
    // 1. Créer une session
    SessionDto sessionDto = new SessionDto();
    sessionDto.setName("Complete Scenario Session");
    sessionDto.setDate(new Date());
    sessionDto.setTeacher_id(testTeacher.getId());
    sessionDto.setDescription("Test scenario");

    String response = mockMvc.perform(post("/api/session")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sessionDto)))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    Long sessionId = objectMapper.readTree(response).get("id").asLong();

    // 2. Ajouter un utilisateur
    mockMvc.perform(post("/api/session/{id}/participate/{userId}",
        sessionId, testUser.getId()))
      .andExpect(status().isOk());

    // 3. Vérifier la session
    mockMvc.perform(get("/api/session/{id}", sessionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.users", hasSize(1)));

    // 4. Retirer l'utilisateur
    mockMvc.perform(delete("/api/session/{id}/participate/{userId}",
        sessionId, testUser.getId()))
      .andExpect(status().isOk());

    // 5. Vérifier que l'utilisateur a été retiré
    mockMvc.perform(get("/api/session/{id}", sessionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.users", hasSize(0)));

    // 6. Supprimer la session
    mockMvc.perform(delete("/api/session/{id}", sessionId))
      .andExpect(status().isOk());

    // 7. Vérifier que la session n'existe plus
    mockMvc.perform(get("/api/session/{id}", sessionId))
      .andExpect(status().isNotFound());
  }
}
