import {expect, jest} from '@jest/globals';
import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: AuthService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [
        BrowserAnimationsModule,
        HttpClientModule,
        ReactiveFormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Validation', () => {
    it('should have invalid form when empty', () => {
      expect(component.form.valid).toBeFalsy();
    });

    it('should validate email field as required', () => {
      const emailControl = component.form.get('email');
      
      emailControl?.setValue('');
      expect(emailControl?.hasError('required')).toBeTruthy();
    });

    it('should validate email format', () => {
      const emailControl = component.form.get('email');
      
      emailControl?.setValue('invalid-email');
      expect(emailControl?.hasError('email')).toBeTruthy();
      
      emailControl?.setValue('valid@email.com');
      expect(emailControl?.hasError('email')).toBeFalsy();
    });

    it('should validate firstName field as required', () => {
      const firstNameControl = component.form.get('firstName');
      
      firstNameControl?.setValue('');
      expect(firstNameControl?.hasError('required')).toBeTruthy();
    });

    it('should validate lastName field as required', () => {
      const lastNameControl = component.form.get('lastName');
      
      lastNameControl?.setValue('');
      expect(lastNameControl?.hasError('required')).toBeTruthy();
    });

    it('should validate password field as required', () => {
      const passwordControl = component.form.get('password');
      
      passwordControl?.setValue('');
      expect(passwordControl?.hasError('required')).toBeTruthy();
    });

    it('should have valid form with correct inputs', () => {
      component.form.setValue({
        email: 'test@test.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });
      
      expect(component.form.valid).toBeTruthy();
    });
  });

  describe('submit', () => {
    it('should register successfully and navigate to login', () => {
      const registerSpy = jest.spyOn(authService, 'register').mockReturnValue(of(void 0));
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.form.setValue({
        email: 'test@test.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });

      component.submit();

      expect(registerSpy).toHaveBeenCalledWith({
        email: 'test@test.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });
      expect(navigateSpy).toHaveBeenCalledWith(['/login']);
      expect(component.onError).toBeFalsy();
    });

    it('should set onError to true on registration failure', () => {
      const registerSpy = jest.spyOn(authService, 'register').mockReturnValue(
        throwError(() => new Error('Registration failed'))
      );

      component.form.setValue({
        email: 'test@test.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });

      component.submit();

      expect(registerSpy).toHaveBeenCalled();
      expect(component.onError).toBeTruthy();
    });

    it('should not navigate on registration error', () => {
      jest.spyOn(authService, 'register').mockReturnValue(
        throwError(() => new Error('Registration failed'))
      );
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.form.setValue({
        email: 'test@test.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });

      component.submit();

      expect(navigateSpy).not.toHaveBeenCalled();
    });
  });

  describe('Template Integration', () => {
    it('should display error message when onError is true', () => {
      component.onError = true;
      fixture.detectChanges();

      const errorElement = fixture.nativeElement.querySelector('.error');
      expect(errorElement).toBeTruthy();
      expect(errorElement.textContent).toContain('An error occurred');
    });

    it('should not display error message when onError is false', () => {
      component.onError = false;
      fixture.detectChanges();

      const errorElement = fixture.nativeElement.querySelector('.error');
      expect(errorElement).toBeFalsy();
    });

    it('should disable submit button when form is invalid', () => {
      component.form.setValue({
        email: '',
        firstName: '',
        lastName: '',
        password: ''
      });
      fixture.detectChanges();

      const submitButton = fixture.nativeElement.querySelector('button[type="submit"]');
      expect(submitButton.disabled).toBeTruthy();
    });

    it('should enable submit button when form is valid', () => {
      component.form.setValue({
        email: 'test@test.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });
      fixture.detectChanges();

      const submitButton = fixture.nativeElement.querySelector('button[type="submit"]');
      expect(submitButton.disabled).toBeFalsy();
    });

    it('should call submit method when form is submitted', () => {
      const submitSpy = jest.spyOn(component, 'submit');
      jest.spyOn(authService, 'register').mockReturnValue(of(void 0));

      component.form.setValue({
        email: 'test@test.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });
      fixture.detectChanges();

      const form = fixture.nativeElement.querySelector('form');
      form.dispatchEvent(new Event('submit'));

      expect(submitSpy).toHaveBeenCalled();
    });
  });
});