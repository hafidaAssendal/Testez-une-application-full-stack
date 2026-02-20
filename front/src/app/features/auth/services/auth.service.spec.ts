import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { LoginRequest } from '../interfaces/loginRequest.interface';
import { RegisterRequest } from '../interfaces/registerRequest.interface';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';
import{expect,jest} from '@jest/globals'
import { from } from 'rxjs';
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockLoginRequest: LoginRequest = {
    email: 'test@test.com',
    password: 'password123'
  };

  const mockRegisterRequest: RegisterRequest = {
    email: 'test@test.com',
    firstName: 'John',
    lastName: 'Doe',
    password: 'password123'
  };

  const mockSessionInformation: SessionInformation = {
    token: 'jwt-token',
    type: 'Bearer',
    id: 1,
    username: 'test@test.com',
    firstName: 'John',
    lastName: 'Doe',
    admin: false
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('register', () => {
    it('should call POST api/auth/register with register data', () => {
      service.register(mockRegisterRequest).subscribe();

      const req = httpMock.expectOne('api/auth/register');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRegisterRequest);
      
      req.flush(null);
    });

    it('should return void on successful registration', (done) => {
      service.register(mockRegisterRequest).subscribe({
        next: (response) => {
          expect(response).toBeUndefined();
          done();
        }
      });

      const req = httpMock.expectOne('api/auth/register');
      req.flush(null);
    });

    it('should handle registration error', (done) => {
      const errorMessage = 'Registration failed';

      service.register(mockRegisterRequest).subscribe({
        error: (error) => {
          expect(error.status).toBe(400);
          expect(error.error).toBe(errorMessage);
          done();
        }
      });

      const req = httpMock.expectOne('api/auth/register');
      req.flush(errorMessage, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('login', () => {
    it('should call POST api/auth/login with login credentials', () => {
      service.login(mockLoginRequest).subscribe();

      const req = httpMock.expectOne('api/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockLoginRequest);
      
      req.flush(mockSessionInformation);
    });

    it('should return session information on successful login', (done) => {
      service.login(mockLoginRequest).subscribe({
        next: (response) => {
          expect(response).toEqual(mockSessionInformation);
          expect(response.token).toBe('jwt-token');
          expect(response.id).toBe(1);
          expect(response.username).toBe('test@test.com');
          done();
        }
      });

      const req = httpMock.expectOne('api/auth/login');
      req.flush(mockSessionInformation);
    });

    it('should handle login error', (done) => {
      const errorMessage = 'Invalid credentials';

      service.login(mockLoginRequest).subscribe({
        error: (error) => {
          expect(error.status).toBe(401);
          expect(error.error).toBe(errorMessage);
          done();
        }
      });

      const req = httpMock.expectOne('api/auth/login');
      req.flush(errorMessage, { status: 401, statusText: 'Unauthorized' });
    });
  });
});