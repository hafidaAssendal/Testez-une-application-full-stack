import {HttpClientModule} from '@angular/common/http';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RouterTestingModule} from '@angular/router/testing';
import {expect, jest} from '@jest/globals';
import {SessionService} from 'src/app/services/session.service';

import {LoginComponent} from './login.component';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";
import {of, throwError} from "rxjs";
import {SessionInformation} from "../../../../interfaces/sessionInformation.interface";

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: AuthService;
  let sessionService: SessionService;
  let router: Router;

  const sessionInformation: SessionInformation =
    {token: '', type: '', id: 1, username: '', firstName: '', lastName: '', admin: true};

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [
        {provide: AuthService, useValue: {login: jest.fn()}},
        {provide: Router, useValue: {navigate: jest.fn()}},
        {provide: SessionService, useValue: {logIn: jest.fn()}},
      ],
      imports: [
        RouterTestingModule,
        BrowserAnimationsModule,
        HttpClientModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule]
    })
      .compileComponents();
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    sessionService = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should submit form, handle success, and navigate to sessions', () => {
    const authServiceSpy = jest
      .spyOn(authService, 'login')
      .mockReturnValue(of(sessionInformation));
    const routerSpy = jest.spyOn(router, 'navigate');
    const sessionServiceSpy = jest.spyOn(sessionService, 'logIn');

    component.form.setValue({email: 'test@example.com', password: 'password123'});
    component.submit();

    expect(authServiceSpy).toHaveBeenCalledWith({email: 'test@example.com', password: 'password123'});
    expect(sessionServiceSpy).toHaveBeenCalledWith(sessionInformation);
    expect(routerSpy).toHaveBeenCalledWith(['/sessions']);
  });

  it('should set onError to true on login error', () => {
    jest.spyOn(authService, 'login').mockReturnValue(throwError(() => new Error('Login error')));

    component.submit();

    expect(component.onError).toBeTruthy();
  });

  it('should display error message on login error', () => {
    const authServiceSpy = jest.spyOn(authService, 'login')
                               .mockReturnValue(throwError(() => new Error('Login error')));

    component.submit();

    fixture.detectChanges();

    const errorMessageElement: HTMLElement = fixture.nativeElement.querySelector('.error');
    expect(authServiceSpy).toHaveBeenCalled();
    expect(errorMessageElement.textContent).toContain('An error occurred');
  });
});