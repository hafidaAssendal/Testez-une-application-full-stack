package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

  @Mock
  private SessionRepository sessionRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private SessionService sessionService;

  private Session session;
  private User user;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    user = User.builder()
      .id(1L)
      .email("test@test.com")
      .firstName("John")
      .lastName("Doe")
      .password("password")
      .admin(false)
      .build();

    session = Session.builder()
      .id(1L)
      .name("Yoga Session")
      .description("Relax")
      .date(new Date())
      .users(new ArrayList<>())
      .build();
  }

  @Test
  void shouldCreateSession() {
    when(sessionRepository.save(session)).thenReturn(session);
    Session result = sessionService.create(session);
    assertNotNull(result);
    verify(sessionRepository).save(session);
  }


  @Test
  void shouldReturnAllSessions() {
    when(sessionRepository.findAll()).thenReturn(List.of(session));
    List<Session> result = sessionService.findAll();
    assertEquals(1, result.size());
  }


  @Test
  void shouldReturnSessionById() {
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

    Session result = sessionService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
  }

  @Test
  void shouldReturnNullIfSessionNotFound() {
    when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

    Session result = sessionService.getById(1L);

    assertNull(result);
  }

  // ---------------- UPDATE ----------------
  @Test
  void shouldUpdateSession() {
    when(sessionRepository.save(any(Session.class))).thenReturn(session);

    Session result = sessionService.update(1L, session);

    assertEquals(1L, result.getId());
  }

  // ---------------- DELETE ----------------
  @Test
  void shouldDeleteSession() {
    sessionService.delete(1L);

    verify(sessionRepository).deleteById(1L);
  }

  // ---------------- PARTICIPATE ----------------
  @Test
  void shouldAllowUserToParticipate() {
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    sessionService.participate(1L, 1L);

    assertEquals(1, session.getUsers().size());
    verify(sessionRepository).save(session);
  }

  @Test
  void shouldThrowNotFoundIfSessionMissing() {
    when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> sessionService.participate(1L, 1L));
  }

  @Test
  void shouldThrowNotFoundIfUserMissing() {
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> sessionService.participate(1L, 1L));
  }

  @Test
  void shouldThrowBadRequestIfAlreadyParticipating() {
    session.getUsers().add(user);

    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    assertThrows(BadRequestException.class, () -> sessionService.participate(1L, 1L));
  }

  // ---------------- NO LONGER PARTICIPATE ----------------
  @Test
  void shouldRemoveUserFromSession() {
    session.getUsers().add(user);

    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

    sessionService.noLongerParticipate(1L, 1L);

    assertEquals(0, session.getUsers().size());
    verify(sessionRepository).save(session);
  }

  @Test
  void shouldThrowNotFoundIfSessionMissingWhenRemoving() {
    when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> sessionService.noLongerParticipate(1L, 1L));
  }

  @Test
  void shouldThrowBadRequestIfUserNotParticipating() {
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

    assertThrows(BadRequestException.class, () -> sessionService.noLongerParticipate(1L, 1L));
  }
}
