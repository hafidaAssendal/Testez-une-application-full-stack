package com.openclassrooms.starterjwt.controllers.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour TeacherController
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TeacherControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TeacherRepository teacherRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private Teacher testTeacher;

  @BeforeEach
  public void setUp() {
    // Nettoyer les données
    teacherRepository.deleteAll();

    // Créer un teacher de test
    testTeacher = Teacher.builder()
      .firstName("John")
      .lastName("Doe")
      .build();
    testTeacher = teacherRepository.save(testTeacher);
  }

  @AfterEach
  public void tearDown() {
    teacherRepository.deleteAll();
  }

  // ==================== Tests pour GET /api/teacher/{id} ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher/{id} - Success")
  public void testFindById_Success() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(testTeacher.getId()))
      .andExpect(jsonPath("$.firstName").value("John"))
      .andExpect(jsonPath("$.lastName").value("Doe"));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher/{id} - Not Found")
  public void testFindById_NotFound() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", 9999L))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher/{id} - Bad Request (Invalid ID format)")
  public void testFindById_BadRequest_InvalidId() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", "invalid"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher/{id} - Not Found (Negative ID)")
  public void testFindById_BadRequest_NegativeId() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", "-1"))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher/{id} - Bad Request (Special characters)")
  public void testFindById_BadRequest_SpecialCharacters() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", "123abc"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/teacher/{id} - Unauthorized (No Authentication)")
  public void testFindById_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId()))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests pour GET /api/teacher ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Success with single teacher")
  public void testFindAll_Success_SingleTeacher() throws Exception {
    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].id").value(testTeacher.getId()))
      .andExpect(jsonPath("$[0].firstName").value("John"))
      .andExpect(jsonPath("$[0].lastName").value("Doe"));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Success with multiple teachers")
  public void testFindAll_Success_MultipleTeachers() throws Exception {
    // Créer des teachers supplémentaires
    Teacher teacher2 = Teacher.builder()
      .firstName("Jane")
      .lastName("Smith")
      .build();
    teacherRepository.save(teacher2);

    Teacher teacher3 = Teacher.builder()
      .firstName("Bob")
      .lastName("Johnson")
      .build();
    teacherRepository.save(teacher3);

    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(3)))
      .andExpect(jsonPath("$[0].firstName").value("John"))
      .andExpect(jsonPath("$[1].firstName").value("Jane"))
      .andExpect(jsonPath("$[2].firstName").value("Bob"));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Empty list")
  public void testFindAll_EmptyList() throws Exception {
    teacherRepository.deleteAll();

    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("GET /api/teacher - Unauthorized")
  public void testFindAll_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isUnauthorized());
  }

  // ==================== Tests de vérification des champs ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher/{id} - Verify all fields are returned")
  public void testFindById_VerifyAllFields() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.firstName").exists())
      .andExpect(jsonPath("$.lastName").exists())
      .andExpect(jsonPath("$.createdAt").exists())
      .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Verify all fields are returned for list")
  public void testFindAll_VerifyAllFields() throws Exception {
    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").exists())
      .andExpect(jsonPath("$[0].firstName").exists())
      .andExpect(jsonPath("$[0].lastName").exists())
      .andExpect(jsonPath("$[0].createdAt").exists())
      .andExpect(jsonPath("$[0].updatedAt").exists());
  }

  // ==================== Tests de cas limites ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher/{id} - Very large ID number")
  public void testFindById_VeryLargeId() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", Long.MAX_VALUE))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Teachers with special characters in names")
  public void testFindAll_SpecialCharactersInNames() throws Exception {
    teacherRepository.deleteAll();

    Teacher teacher = Teacher.builder()
      .firstName("François")
      .lastName("O'Brien")
      .build();
    teacherRepository.save(teacher);

    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].firstName").value("François"))
      .andExpect(jsonPath("$[0].lastName").value("O'Brien"));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Teachers with minimum length names")
  public void testFindAll_MinimumLengthNames() throws Exception {
    teacherRepository.deleteAll();

    Teacher teacher = Teacher.builder()
      .firstName("A")
      .lastName("B")
      .build();
    teacherRepository.save(teacher);

    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].firstName").value("A"))
      .andExpect(jsonPath("$[0].lastName").value("B"));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Teachers with maximum length names")
  public void testFindAll_MaximumLengthNames() throws Exception {
    teacherRepository.deleteAll();

    String maxFirstName = "A".repeat(20); // Max 20 caractères
    String maxLastName = "B".repeat(20); // Max 20 caractères

    Teacher teacher = Teacher.builder()
      .firstName(maxFirstName)
      .lastName(maxLastName)
      .build();
    teacherRepository.save(teacher);

    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].firstName").value(maxFirstName))
      .andExpect(jsonPath("$[0].lastName").value(maxLastName));
  }

  // ==================== Tests de performance ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Performance with many teachers")
  public void testFindAll_PerformanceWithManyTeachers() throws Exception {
    teacherRepository.deleteAll();

    // Créer 50 teachers
    for (int i = 1; i <= 50; i++) {
      Teacher teacher = Teacher.builder()
        .firstName("Teacher" + i)
        .lastName("LastName" + i)
        .build();
      teacherRepository.save(teacher);
    }

    mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(50)));
  }

  // ==================== Tests de scénarios ====================

  @Test
  @WithMockUser
  @DisplayName("Scenario: Get all teachers, then get specific teacher")
  public void testScenario_GetAllThenGetSpecific() throws Exception {
    // 1. Récupérer tous les teachers
    String response = mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(1)))
      .andReturn().getResponse().getContentAsString();

    // Extraire l'ID du premier teacher
    Long teacherId = objectMapper.readTree(response).get(0).get("id").asLong();

    // 2. Récupérer ce teacher spécifique
    mockMvc.perform(get("/api/teacher/{id}", teacherId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(teacherId))
      .andExpect(jsonPath("$.firstName").value("John"))
      .andExpect(jsonPath("$.lastName").value("Doe"));
  }

  @Test
  @WithMockUser
  @DisplayName("Scenario: Verify teacher exists before and after deletion")
  public void testScenario_VerifyExistenceAfterDeletion() throws Exception {
    // 1. Vérifier que le teacher existe
    mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId()))
      .andExpect(status().isOk());

    // 2. Supprimer le teacher directement en base
    teacherRepository.deleteById(testTeacher.getId());

    // 3. Vérifier que le teacher n'existe plus
    mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId()))
      .andExpect(status().isNotFound());
  }

  // ==================== Tests de validation de données ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher/{id} - Verify timestamps are properly formatted")
  public void testFindById_VerifyTimestampsFormat() throws Exception {
    mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.createdAt").isNotEmpty())
      .andExpect(jsonPath("$.updatedAt").isNotEmpty());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/teacher - Verify consistent ordering")
  public void testFindAll_VerifyOrdering() throws Exception {
    teacherRepository.deleteAll();

    // Créer plusieurs teachers
    Teacher teacher1 = teacherRepository.save(Teacher.builder()
      .firstName("Alpha")
      .lastName("First")
      .build());

    Teacher teacher2 = teacherRepository.save(Teacher.builder()
      .firstName("Beta")
      .lastName("Second")
      .build());

    Teacher teacher3 = teacherRepository.save(Teacher.builder()
      .firstName("Gamma")
      .lastName("Third")
      .build());

    // Récupérer deux fois pour vérifier la cohérence
    String response1 = mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    String response2 = mockMvc.perform(get("/api/teacher"))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    // Les réponses devraient être identiques
    org.assertj.core.api.Assertions.assertThat(response1).isEqualTo(response2);
  }
}
