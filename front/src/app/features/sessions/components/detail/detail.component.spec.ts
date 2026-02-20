
import { expect, jest } from '@jest/globals'; 
import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { SessionService } from 'src/app/services/session.service';
import { TeacherService } from 'src/app/services/teacher.service';
import { SessionApiService } from '../../services/session-api.service';
import { DetailComponent } from './detail.component';

describe('DetailComponent', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;
  let sessionApiService: SessionApiService;
  let teacherService: TeacherService;
  let matSnackBar: MatSnackBar;
  let router: Router;

  const mockSessionService = {
    sessionInformation: {
      admin: true,
      id: 1
    }
  };

  const mockSession = {
    id: 1,
    name: 'Yoga Session',
    description: 'Relaxing yoga session',
    date: new Date('2024-01-15'),
    teacher_id: 1,
    users: [1, 2, 3],
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date('2024-01-10')
  };

  const mockTeacher = {
    id: 1,
    lastName: 'Doe',
    firstName: 'John',
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date('2024-01-10')
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DetailComponent],
      imports: [
        HttpClientModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatCardModule,
        MatIconModule
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => '1' } }
          }
        },
        SessionApiService,
        TeacherService,
        FormBuilder
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    sessionApiService = TestBed.inject(SessionApiService);
    teacherService = TestBed.inject(TeacherService);
    matSnackBar = TestBed.inject(MatSnackBar);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should initialize with correct sessionId from route', () => {
      expect(component.sessionId).toBe('1');
    });

    it('should initialize isAdmin from sessionService', () => {
      expect(component.isAdmin).toBe(true);
    });

    it('should initialize userId from sessionService', () => {
      expect(component.userId).toBe('1');
    });
  });

  describe('ngOnInit', () => {
    it('should fetch session on init', () => {
      const detailSpy = jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      const teacherSpy = jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      component.ngOnInit();

      expect(detailSpy).toHaveBeenCalledWith('1');
      expect(teacherSpy).toHaveBeenCalledWith('1');
    });

    it('should set session and teacher on successful fetch', () => {
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      component.ngOnInit();

      expect(component.session).toEqual(mockSession);
      expect(component.teacher).toEqual(mockTeacher);
    });

    it('should set isParticipate to true when user is in session users', () => {
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      component.ngOnInit();

      expect(component.isParticipate).toBeTruthy();
    });

    it('should set isParticipate to false when user is not in session users', () => {
      const sessionWithoutUser = { ...mockSession, users: [2, 3] };
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(sessionWithoutUser));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      component.ngOnInit();

      expect(component.isParticipate).toBeFalsy();
    });
  });

  describe('back', () => {
    it('should call window.history.back', () => {
      const backSpy = jest.spyOn(window.history, 'back').mockImplementation(() => {});

      component.back();

      expect(backSpy).toHaveBeenCalled();
    });
  });

  describe('delete', () => {
    it('should call sessionApiService.delete with correct id', () => {
      const deleteSpy = jest.spyOn(sessionApiService, 'delete').mockReturnValue(of(null));
      const snackBarSpy = jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.delete();

      expect(deleteSpy).toHaveBeenCalledWith('1');
    });

    it('should display success message after deletion', () => {
      jest.spyOn(sessionApiService, 'delete').mockReturnValue(of(null));
      const snackBarSpy = jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.delete();

      expect(snackBarSpy).toHaveBeenCalledWith('Session deleted !', 'Close', { duration: 3000 });
    });

    it('should navigate to sessions after deletion', () => {
      jest.spyOn(sessionApiService, 'delete').mockReturnValue(of(null));
      jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.delete();

      expect(navigateSpy).toHaveBeenCalledWith(['sessions']);
    });
  });

  describe('participate', () => {
    it('should call sessionApiService.participate with correct ids', () => {
      const participateSpy = jest.spyOn(sessionApiService, 'participate').mockReturnValue(of(void 0));
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      component.participate();

      expect(participateSpy).toHaveBeenCalledWith('1', '1');
    });

    it('should fetch session after participation', () => {
      jest.spyOn(sessionApiService, 'participate').mockReturnValue(of(void 0));
      const detailSpy = jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      component.participate();

      expect(detailSpy).toHaveBeenCalledWith('1');
    });
  });

  describe('unParticipate', () => {
    it('should call sessionApiService.unParticipate with correct ids', () => {
      const unParticipateSpy = jest.spyOn(sessionApiService, 'unParticipate').mockReturnValue(of(void 0));
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      component.unParticipate();

      expect(unParticipateSpy).toHaveBeenCalledWith('1', '1');
    });

    it('should fetch session after unparticipation', () => {
      jest.spyOn(sessionApiService, 'unParticipate').mockReturnValue(of(void 0));
      const detailSpy = jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      component.unParticipate();

      expect(detailSpy).toHaveBeenCalledWith('1');
    });
  });
});