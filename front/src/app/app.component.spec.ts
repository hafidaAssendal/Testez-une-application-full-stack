import { expect, jest } from '@jest/globals';
import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { AuthService } from './features/auth/services/auth.service';
import { SessionService } from './services/session.service';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let sessionService: SessionService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientModule],
      declarations: [AppComponent],
      providers: [
        SessionService,
        AuthService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    sessionService = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('$isLogged', () => {
    it('should return observable from sessionService', (done) => {
      const isLoggedSpy = jest.spyOn(sessionService, '$isLogged').mockReturnValue(of(true));

      component.$isLogged().subscribe((isLogged) => {
        expect(isLogged).toBeTruthy();
        expect(isLoggedSpy).toHaveBeenCalled();
        done();
      });
    });

    it('should return false when not logged in', (done) => {
      jest.spyOn(sessionService, '$isLogged').mockReturnValue(of(false));

      component.$isLogged().subscribe((isLogged) => {
        expect(isLogged).toBeFalsy();
        done();
      });
    });
  });

  describe('logout', () => {
    it('should call sessionService.logOut', () => {
      const logOutSpy = jest.spyOn(sessionService, 'logOut');
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.logout();

      expect(logOutSpy).toHaveBeenCalled();
    });

    it('should navigate to login page', () => {
      jest.spyOn(sessionService, 'logOut');
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.logout();

      expect(navigateSpy).toHaveBeenCalledWith(['/login']);
    });

    it('should call logOut before navigate', () => {
      const logOutSpy = jest.spyOn(sessionService, 'logOut');
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.logout();

      expect(logOutSpy).toHaveBeenCalled();
      expect(navigateSpy).toHaveBeenCalledWith(['/login']);
    });
  });
});