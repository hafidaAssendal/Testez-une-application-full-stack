package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - TeacherService")
class TeacherServiceTest {

  @Mock
  private TeacherRepository teacherRepository;

  @InjectMocks
  private TeacherService teacherService;
  private Teacher teacher1;
  private Teacher teacher2;


  @BeforeEach
  void setUp() {

    teacher1 = Teacher.builder()
      .id(1L)
      .firstName("Marie")
      .lastName("Dupont")
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    teacher2 = Teacher.builder()
      .id(2L)
      .firstName("Pierre")
      .lastName("Martin")
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();
  }


  @Test
  @DisplayName("findAll() doit retourner tous les teachers")
  void testFindAll_ShouldReturnAllTeachers() {

     List<Teacher> expectedTeachers = Arrays.asList(teacher1, teacher2);
    when(teacherRepository.findAll()).thenReturn(expectedTeachers);

    List<Teacher> actualTeachers = teacherService.findAll();

    assertThat(actualTeachers).isNotNull();
    assertThat(actualTeachers).hasSize(2);
    assertThat(actualTeachers).containsExactly(teacher1, teacher2);

    // Vérifier que le repository a bien été appelé une fois
    verify(teacherRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("findAll() doit retourner une liste vide si aucun teacher n'existe")
  void testFindAll_ShouldReturnEmptyList_WhenNoTeachersExist() {
    // ARRANGE
    when(teacherRepository.findAll()).thenReturn(Arrays.asList());

    // ACT
    List<Teacher> actualTeachers = teacherService.findAll();

    // ASSERT
    assertThat(actualTeachers).isEmpty();
    verify(teacherRepository, times(1)).findAll();
  }


  @Test
  @DisplayName("findById() doit retourner le teacher correspondant à l'ID")
  void testFindById_ShouldReturnTeacher_WhenIdExists() {

    Long teacherId = 1L;
    when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher1));

    Teacher actualTeacher = teacherService.findById(teacherId);

    assertThat(actualTeacher).isNotNull();
    assertThat(actualTeacher.getId()).isEqualTo(teacherId);
    assertThat(actualTeacher.getFirstName()).isEqualTo("Marie");
    assertThat(actualTeacher.getLastName()).isEqualTo("Dupont");

    verify(teacherRepository, times(1)).findById(teacherId);
  }


  @Test
  @DisplayName("findById() doit retourner null si l'ID n'existe pas")
  void testFindById_ShouldReturnNull_WhenIdDoesNotExist() {

    Long nonExistentId = 999L;
    when(teacherRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    Teacher actualTeacher = teacherService.findById(nonExistentId);

    assertThat(actualTeacher).isNull();
    verify(teacherRepository, times(1)).findById(nonExistentId);
  }


  @Test
  @DisplayName("findById() doit gérer correctement un ID null")
  void testFindById_ShouldHandleNullId() {

    when(teacherRepository.findById(null)).thenReturn(Optional.empty());


    Teacher actualTeacher = teacherService.findById(null);

    assertThat(actualTeacher).isNull();
    verify(teacherRepository, times(1)).findById(null);
  }
}
