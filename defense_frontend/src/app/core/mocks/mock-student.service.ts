import { Injectable } from '@angular/core';
import { Observable, of, delay, throwError } from 'rxjs';
import { Student, StudentRequest, Major } from '../models';
import { MOCK_STUDENTS } from './mock-data';
import { StudentFilters } from '../services/student.service';

@Injectable({
  providedIn: 'root'
})
export class MockStudentService {
  private students: Student[] = [...MOCK_STUDENTS];
  private nextId = this.students.length + 1;

  getAll(filters?: StudentFilters): Observable<Student[]> {
    let filtered = [...this.students];

    if (filters?.major) {
      filtered = filtered.filter(student => student.major === filters.major);
    }

    if (filters?.level !== undefined && filters.level !== null) {
      filtered = filtered.filter(student => student.level === filters.level);
    }

    return of(filtered).pipe(delay(300));
  }

  getById(id: number): Observable<Student> {
    const student = this.students.find(s => s.id === id);

    if (student) {
      return of(student).pipe(delay(200));
    }

    return throwError(() => new Error('Étudiant non trouvé')).pipe(delay(200));
  }

  create(request: StudentRequest): Observable<Student> {
    const newStudent: Student = {
      id: this.nextId++,
      userId: request.userId,
      firstName: request.firstName,
      lastName: request.lastName,
      email: request.email,
      major: request.major,
      level: request.level
    };

    this.students.push(newStudent);
    return of(newStudent).pipe(delay(400));
  }

  update(id: number, request: StudentRequest): Observable<Student> {
    const index = this.students.findIndex(s => s.id === id);

    if (index !== -1) {
      this.students[index] = {
        id,
        userId: request.userId,
        firstName: request.firstName,
        lastName: request.lastName,
        email: request.email,
        major: request.major,
        level: request.level
      };

      return of(this.students[index]).pipe(delay(400));
    }

    return throwError(() => new Error('Étudiant non trouvé')).pipe(delay(200));
  }

  delete(id: number): Observable<void> {
    const index = this.students.findIndex(s => s.id === id);

    if (index !== -1) {
      this.students.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }

    return throwError(() => new Error('Étudiant non trouvé')).pipe(delay(200));
  }

  getByMajor(major: Major): Observable<Student[]> {
    return of(this.students.filter(s => s.major === major)).pipe(delay(200));
  }

  getMe(): Observable<Student> {
    // Temporary mock behavior
    return of(this.students[0]).pipe(delay(200));
  }
}