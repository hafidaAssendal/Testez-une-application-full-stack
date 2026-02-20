import { jest,expect } from '@jest/globals';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SessionApiService } from './session-api.service';
import { Session } from '../interfaces/session.interface';

describe('SessionApiService', () => {
  let service: SessionApiService;
  let httpMock: HttpTestingController;

  const mockSession: Session = {
    id: 1,
    name: 'Yoga Session',
    description: 'Relaxing yoga session',
    date: new Date('2024-01-15'),
    teacher_id: 1,
    users: [1, 2, 3],
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date('2024-01-10')
  };

  const mockSessions: Session[] = [
    mockSession,
    {
      id: 2,
      name: 'Advanced Yoga',
      description: 'Advanced techniques',
      date: new Date('2024-01-20'),
      teacher_id: 2,
      users: [4, 5],
      createdAt: new Date('2024-01-02'),
      updatedAt: new Date('2024-01-11')
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SessionApiService]
    });
    service = TestBed.inject(SessionApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('all', () => {
    it('should call GET api/session', () => {
      service.all().subscribe();

      const req = httpMock.expectOne('api/session');
      expect(req.request.method).toBe('GET');
      
      req.flush(mockSessions);
    });

    it('should return array of sessions', (done) => {
      service.all().subscribe({
        next: (sessions) => {
          expect(sessions).toEqual(mockSessions);
          expect(sessions.length).toBe(2);
          expect(sessions[0].name).toBe('Yoga Session');
          done();
        }
      });

      const req = httpMock.expectOne('api/session');
      req.flush(mockSessions);
    });

    it('should handle error when fetching all sessions', (done) => {
      service.all().subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
          done();
        }
      });

      const req = httpMock.expectOne('api/session');
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('detail', () => {
    it('should call GET api/session/:id with correct id', () => {
      service.detail('1').subscribe();

      const req = httpMock.expectOne('api/session/1');
      expect(req.request.method).toBe('GET');
      
      req.flush(mockSession);
    });

    it('should return session details', (done) => {
      service.detail('1').subscribe({
        next: (session) => {
          expect(session).toEqual(mockSession);
          expect(session.id).toBe(1);
          expect(session.name).toBe('Yoga Session');
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1');
      req.flush(mockSession);
    });

    it('should handle 404 error when session not found', (done) => {
      service.detail('999').subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
          done();
        }
      });

      const req = httpMock.expectOne('api/session/999');
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('delete', () => {
    it('should call DELETE api/session/:id with correct id', () => {
      service.delete('1').subscribe();

      const req = httpMock.expectOne('api/session/1');
      expect(req.request.method).toBe('DELETE');
      
      req.flush(null);
    });

    it('should return success on deletion', (done) => {
      service.delete('1').subscribe({
        next: (response) => {
          expect(response).toBeNull();
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1');
      req.flush(null);
    });

    it('should handle error when deleting session', (done) => {
      service.delete('1').subscribe({
        error: (error) => {
          expect(error.status).toBe(403);
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1');
      req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('create', () => {
    it('should call POST api/session with session data', () => {
      service.create(mockSession).subscribe();

      const req = httpMock.expectOne('api/session');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockSession);
      
      req.flush(mockSession);
    });

    it('should return created session', (done) => {
      service.create(mockSession).subscribe({
        next: (session) => {
          expect(session).toEqual(mockSession);
          expect(session.id).toBe(1);
          done();
        }
      });

      const req = httpMock.expectOne('api/session');
      req.flush(mockSession);
    });

    it('should handle validation error when creating session', (done) => {
      service.create(mockSession).subscribe({
        error: (error) => {
          expect(error.status).toBe(400);
          done();
        }
      });

      const req = httpMock.expectOne('api/session');
      req.flush('Invalid data', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('update', () => {
    it('should call PUT api/session/:id with session data', () => {
      service.update('1', mockSession).subscribe();

      const req = httpMock.expectOne('api/session/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockSession);
      
      req.flush(mockSession);
    });

    it('should return updated session', (done) => {
      const updatedSession = { ...mockSession, name: 'Updated Yoga Session' };

      service.update('1', updatedSession).subscribe({
        next: (session) => {
          expect(session).toEqual(updatedSession);
          expect(session.name).toBe('Updated Yoga Session');
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1');
      req.flush(updatedSession);
    });

    it('should handle error when updating session', (done) => {
      service.update('1', mockSession).subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1');
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('participate', () => {
    it('should call POST api/session/:id/participate/:userId', () => {
      service.participate('1', '10').subscribe();

      const req = httpMock.expectOne('api/session/1/participate/10');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeNull();
      
      req.flush(null);
    });

    it('should return void on successful participation', (done) => {
      service.participate('1', '10').subscribe({
        next: (response) => {
          expect(response).toBeNull();
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1/participate/10');
      req.flush(null);
    });

    it('should handle error when participating', (done) => {
      service.participate('1', '10').subscribe({
        error: (error) => {
          expect(error.status).toBe(400);
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1/participate/10');
      req.flush('Already participating', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('unParticipate', () => {
    it('should call DELETE api/session/:id/participate/:userId', () => {
      service.unParticipate('1', '10').subscribe();

      const req = httpMock.expectOne('api/session/1/participate/10');
      expect(req.request.method).toBe('DELETE');
      
      req.flush(null);
    });

    it('should return void on successful unparticipation', (done) => {
      service.unParticipate('1', '10').subscribe({
        next: (response) => {
         expect(response).toBeNull();
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1/participate/10');
      req.flush(null);
    });

    it('should handle error when unparticipating', (done) => {
      service.unParticipate('1', '10').subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
          done();
        }
      });

      const req = httpMock.expectOne('api/session/1/participate/10');
      req.flush('Not participating', { status: 404, statusText: 'Not Found' });
    });
  });
});