import { expect } from '@jest/globals';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TeacherService } from './teacher.service';
import { Teacher } from '../interfaces/teacher.interface';

describe('TeacherService', () => {
  let service: TeacherService;
  let httpMock: HttpTestingController;

  const mockTeacher: Teacher = {
    id: 1,
    lastName: 'Doe',
    firstName: 'John',
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date('2024-01-10')
  };

  const mockTeachers: Teacher[] = [
    mockTeacher,
    {
      id: 2,
      lastName: 'Smith',
      firstName: 'Jane',
      createdAt: new Date('2024-01-02'),
      updatedAt: new Date('2024-01-11')
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TeacherService]
    });
    service = TestBed.inject(TeacherService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('all', () => {
    it('should call GET api/teacher', () => {
      service.all().subscribe();

      const req = httpMock.expectOne('api/teacher');
      expect(req.request.method).toBe('GET');
      
      req.flush(mockTeachers);
    });

    it('should return array of teachers', (done) => {
      service.all().subscribe({
        next: (teachers) => {
          expect(teachers).toEqual(mockTeachers);
          expect(teachers.length).toBe(2);
          expect(teachers[0].firstName).toBe('John');
          done();
        }
      });

      const req = httpMock.expectOne('api/teacher');
      req.flush(mockTeachers);
    });

    it('should handle error when fetching teachers', (done) => {
      service.all().subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
          done();
        }
      });

      const req = httpMock.expectOne('api/teacher');
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('detail', () => {
    it('should call GET api/teacher/:id with correct id', () => {
      service.detail('1').subscribe();

      const req = httpMock.expectOne('api/teacher/1');
      expect(req.request.method).toBe('GET');
      
      req.flush(mockTeacher);
    });

    it('should return teacher details', (done) => {
      service.detail('1').subscribe({
        next: (teacher) => {
          expect(teacher).toEqual(mockTeacher);
          expect(teacher.id).toBe(1);
          expect(teacher.firstName).toBe('John');
          done();
        }
      });

      const req = httpMock.expectOne('api/teacher/1');
      req.flush(mockTeacher);
    });

    it('should handle 404 error when teacher not found', (done) => {
      service.detail('999').subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
          done();
        }
      });

      const req = httpMock.expectOne('api/teacher/999');
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });
});