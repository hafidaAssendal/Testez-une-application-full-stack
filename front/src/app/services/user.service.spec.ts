import { expect } from '@jest/globals';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UserService } from './user.service';
import { User } from '../interfaces/user.interface';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const mockUser: User = {
    id: 1,
    email: 'test@test.com',
    lastName: 'Doe',
    firstName: 'John',
    admin: false,
    password: 'password123',
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date('2024-01-10')
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getById', () => {
    it('should call GET api/user/:id with correct id', () => {
      service.getById('1').subscribe();

      const req = httpMock.expectOne('api/user/1');
      expect(req.request.method).toBe('GET');
      
      req.flush(mockUser);
    });

    it('should return user details', (done) => {
      service.getById('1').subscribe({
        next: (user) => {
          expect(user).toEqual(mockUser);
          expect(user.id).toBe(1);
          expect(user.email).toBe('test@test.com');
          done();
        }
      });

      const req = httpMock.expectOne('api/user/1');
      req.flush(mockUser);
    });

    it('should handle 404 error when user not found', (done) => {
      service.getById('999').subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
          done();
        }
      });

      const req = httpMock.expectOne('api/user/999');
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('delete', () => {
    it('should call DELETE api/user/:id with correct id', () => {
      service.delete('1').subscribe();

      const req = httpMock.expectOne('api/user/1');
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

      const req = httpMock.expectOne('api/user/1');
      req.flush(null);
    });

    it('should handle error when deleting user', (done) => {
      service.delete('1').subscribe({
        error: (error) => {
          expect(error.status).toBe(403);
          done();
        }
      });

      const req = httpMock.expectOne('api/user/1');
      req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });
  });
});