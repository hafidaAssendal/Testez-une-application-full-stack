import { expect } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { SessionService } from './session.service';
import { SessionInformation } from '../interfaces/sessionInformation.interface';

describe('SessionService', () => {
  let service: SessionService;

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
      providers: [SessionService]
    });
    service = TestBed.inject(SessionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have initial state as not logged', () => {
    expect(service.isLogged).toBeFalsy();
    expect(service.sessionInformation).toBeUndefined();
  });

  describe('$isLogged', () => {
    it('should return an observable of isLogged state', (done) => {
      service.$isLogged().subscribe((isLogged) => {
        expect(isLogged).toBeFalsy();
        done();
      });
    });
  });

  describe('logIn', () => {
    it('should set user session information', () => {
      service.logIn(mockSessionInformation);

      expect(service.sessionInformation).toEqual(mockSessionInformation);
      expect(service.isLogged).toBeTruthy();
    });

    it('should emit true through $isLogged observable', (done) => {
      service.logIn(mockSessionInformation);

      service.$isLogged().subscribe((isLogged) => {
        expect(isLogged).toBeTruthy();
        done();
      });
    });

    it('should store user data correctly', () => {
      service.logIn(mockSessionInformation);

      expect(service.sessionInformation?.id).toBe(1);
      expect(service.sessionInformation?.username).toBe('test@test.com');
      expect(service.sessionInformation?.admin).toBeFalsy();
    });
  });

  describe('logOut', () => {
    beforeEach(() => {
      service.logIn(mockSessionInformation);
    });

    it('should clear session information', () => {
      service.logOut();

      expect(service.sessionInformation).toBeUndefined();
      expect(service.isLogged).toBeFalsy();
    });

    it('should emit false through $isLogged observable', (done) => {
      service.logOut();

      service.$isLogged().subscribe((isLogged) => {
        expect(isLogged).toBeFalsy();
        done();
      });
    });
  });

  describe('Multiple login/logout cycles', () => {
    it('should handle multiple login and logout correctly', (done) => {
      const states: boolean[] = [];

      service.$isLogged().subscribe((isLogged) => {
        states.push(isLogged);
      });

      service.logIn(mockSessionInformation);
      service.logOut();
      service.logIn(mockSessionInformation);

      setTimeout(() => {
        expect(states).toEqual([false, true, false, true]);
        done();
      }, 100);
    });
  });
});