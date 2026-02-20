
import { expect, jest } from '@jest/globals';
import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { SessionService } from 'src/app/services/session.service';
import { TeacherService } from 'src/app/services/teacher.service';
import { SessionApiService } from '../../services/session-api.service';
import { FormComponent } from './form.component';

describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
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

  const mockTeachers = [
    { id: 1, firstName: 'John', lastName: 'Doe', createdAt: new Date(), updatedAt: new Date() },
    { id: 2, firstName: 'Jane', lastName: 'Smith', createdAt: new Date(), updatedAt: new Date() }
  ];

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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FormComponent],
      imports: [
        HttpClientModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        MatSnackBarModule,
        MatSelectModule,
        BrowserAnimationsModule
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
        TeacherService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
    sessionApiService = TestBed.inject(SessionApiService);
    teacherService = TestBed.inject(TeacherService);
    matSnackBar = TestBed.inject(MatSnackBar);
    router = TestBed.inject(Router);

    jest.spyOn(teacherService, 'all').mockReturnValue(of(mockTeachers));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
  
    it('should initialize form in create mode when URL does not include update', () => {
      jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/create');

      component.ngOnInit();

      expect(component.onUpdate).toBeFalsy();
      expect(component.sessionForm).toBeDefined();
      expect(component.sessionForm?.get('name')?.value).toBe('');
    });

    it('should initialize form in update mode when URL includes update', () => {
      jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/update/1');
      const detailSpy = jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));

      component.ngOnInit();

      expect(component.onUpdate).toBeTruthy();
      expect(detailSpy).toHaveBeenCalledWith('1');
    });

    it('should populate form with session data in update mode', () => {
      jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/update/1');
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));

      component.ngOnInit();

      expect(component.sessionForm?.get('name')?.value).toBe('Yoga Session');
      expect(component.sessionForm?.get('teacher_id')?.value).toBe(1);
      expect(component.sessionForm?.get('description')?.value).toBe('Relaxing yoga session');
    });
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/create');
      component.ngOnInit();
    });

    it('should have invalid form when empty', () => {
      expect(component.sessionForm?.valid).toBeFalsy();
    });

    it('should validate name field as required', () => {
      const nameControl = component.sessionForm?.get('name');
      
      nameControl?.setValue('');
      expect(nameControl?.hasError('required')).toBeTruthy();
      
      nameControl?.setValue('Yoga Session');
      expect(nameControl?.hasError('required')).toBeFalsy();
    });

    it('should validate date field as required', () => {
      const dateControl = component.sessionForm?.get('date');
      
      dateControl?.setValue('');
      expect(dateControl?.hasError('required')).toBeTruthy();
      
      dateControl?.setValue('2024-01-15');
      expect(dateControl?.hasError('required')).toBeFalsy();
    });

    it('should validate teacher_id field as required', () => {
      const teacherControl = component.sessionForm?.get('teacher_id');
      
      teacherControl?.setValue('');
      expect(teacherControl?.hasError('required')).toBeTruthy();
      
      teacherControl?.setValue(1);
      expect(teacherControl?.hasError('required')).toBeFalsy();
    });

    it('should validate description field as required', () => {
      const descriptionControl = component.sessionForm?.get('description');
      
      descriptionControl?.setValue('');
      expect(descriptionControl?.hasError('required')).toBeTruthy();
      
      descriptionControl?.setValue('Test description');
      expect(descriptionControl?.hasError('required')).toBeFalsy();
    });

    it('should have valid form with all fields filled correctly', () => {
      component.sessionForm?.patchValue({
        name: 'Yoga Session',
        date: '2024-01-15',
        teacher_id: 1,
        description: 'Test description'
      });
      
      expect(component.sessionForm?.valid).toBeTruthy();
    });
  });

  describe('submit - Create Mode', () => {
    beforeEach(() => {
      jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/create');
      component.ngOnInit();
    });

    it('should call create when not in update mode', () => {
      const createSpy = jest.spyOn(sessionApiService, 'create').mockReturnValue(of(mockSession));
      jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      jest.spyOn(router, 'navigate');

      component.sessionForm?.patchValue({
        name: 'New Session',
        date: '2024-01-15',
        teacher_id: 1,
        description: 'Test description'
      });

      component.submit();

      expect(createSpy).toHaveBeenCalled();
    });

    it('should display success message after creation', () => {
      jest.spyOn(sessionApiService, 'create').mockReturnValue(of(mockSession));
      const snackBarSpy = jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      jest.spyOn(router, 'navigate');

      component.sessionForm?.patchValue({
        name: 'New Session',
        date: '2024-01-15',
        teacher_id: 1,
        description: 'Test description'
      });

      component.submit();

      expect(snackBarSpy).toHaveBeenCalledWith('Session created !', 'Close', { duration: 3000 });
    });

    it('should navigate to sessions after creation', () => {
      jest.spyOn(sessionApiService, 'create').mockReturnValue(of(mockSession));
      jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.sessionForm?.patchValue({
        name: 'New Session',
        date: '2024-01-15',
        teacher_id: 1,
        description: 'Test description'
      });

      component.submit();

      expect(navigateSpy).toHaveBeenCalledWith(['sessions']);
    });
  });

  describe('submit - Update Mode', () => {
    beforeEach(() => {
      jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/update/1');
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      component.ngOnInit();
    });

    it('should call update when in update mode', () => {
      const updateSpy = jest.spyOn(sessionApiService, 'update').mockReturnValue(of(mockSession));
      jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      jest.spyOn(router, 'navigate');

      component.sessionForm?.patchValue({
        name: 'Updated Session',
        date: '2024-01-20',
        teacher_id: 2,
        description: 'Updated description'
      });

      component.submit();

      expect(updateSpy).toHaveBeenCalledWith('1', expect.any(Object));
    });

    it('should display success message after update', () => {
      jest.spyOn(sessionApiService, 'update').mockReturnValue(of(mockSession));
      const snackBarSpy = jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      jest.spyOn(router, 'navigate');

      component.sessionForm?.patchValue({
        name: 'Updated Session',
        date: '2024-01-20',
        teacher_id: 2,
        description: 'Updated description'
      });

      component.submit();

      expect(snackBarSpy).toHaveBeenCalledWith('Session updated !', 'Close', { duration: 3000 });
    });

    it('should navigate to sessions after update', () => {
      jest.spyOn(sessionApiService, 'update').mockReturnValue(of(mockSession));
      jest.spyOn(matSnackBar, 'open').mockReturnValue({} as any);
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.sessionForm?.patchValue({
        name: 'Updated Session',
        date: '2024-01-20',
        teacher_id: 2,
        description: 'Updated description'
      });

      component.submit();

      expect(navigateSpy).toHaveBeenCalledWith(['sessions']);
    });
  });
});