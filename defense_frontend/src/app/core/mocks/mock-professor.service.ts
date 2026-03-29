import { Injectable } from '@angular/core';
import { Observable, of, delay, throwError } from 'rxjs';
import { Professor, ProfessorRequest } from '../models';
import { MOCK_PROFESSORS, MOCK_DEFENSES } from './mock-data';

@Injectable({
  providedIn: 'root'
})
export class MockProfessorService {
  private professors: Professor[] = [...MOCK_PROFESSORS];
  private nextId = this.professors.length + 1;

  getAll(): Observable<Professor[]> {
    return of([...this.professors]).pipe(delay(300));
  }

  getById(id: number): Observable<Professor> {
    const professor = this.professors.find(p => p.id === id);

    if (professor) {
      return of(professor).pipe(delay(200));
    }

    return throwError(() => new Error('Professeur non trouvé')).pipe(delay(200));
  }

  getByUserId(userId: number): Observable<Professor> {
    const professor = this.professors.find(p => p.userId === userId);

    if (professor) {
      return of(professor).pipe(delay(200));
    }

    return throwError(() => new Error('Professeur non trouvé')).pipe(delay(200));
  }

  create(request: ProfessorRequest): Observable<Professor> {
    const newProfessor: Professor = {
      id: this.nextId++,
      userId: request.userId,
      firstName: request.firstName,
      lastName: request.lastName,
      email: request.email
    };

    this.professors.push(newProfessor);
    return of(newProfessor).pipe(delay(400));
  }

  update(id: number, request: ProfessorRequest): Observable<Professor> {
    const index = this.professors.findIndex(p => p.id === id);

    if (index !== -1) {
      this.professors[index] = {
        id,
        userId: request.userId,
        firstName: request.firstName,
        lastName: request.lastName,
        email: request.email
      };

      return of(this.professors[index]).pipe(delay(400));
    }

    return throwError(() => new Error('Professeur non trouvé')).pipe(delay(200));
  }

  delete(id: number): Observable<void> {
    const index = this.professors.findIndex(p => p.id === id);

    if (index !== -1) {
      this.professors.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }

    return throwError(() => new Error('Professeur non trouvé')).pipe(delay(200));
  }

  getAvailableForRole(
    role: 'SUPERVISOR' | 'PRESIDENT' | 'REVIEWER' | 'EXAMINER',
    date: string,
    startTime: string,
    endTime: string,
    excludeDefenseId?: number
  ): Observable<Professor[]> {
    const busyProfessorIds = new Set(
      MOCK_DEFENSES
        .filter(d =>
          d.defenseDate === date &&
          (excludeDefenseId === undefined || d.id !== excludeDefenseId) &&
          this.timesOverlap(d.startTime, d.endTime, startTime, endTime)
        )
        .flatMap(d => [d.supervisorId, d.presidentId, d.reviewerId, d.examinerId])
    );

    const available = this.professors.filter(p => !busyProfessorIds.has(p.id));
    return of(available).pipe(delay(300));
  }

  getMe(): Observable<Professor> {
    return of(this.professors[0]).pipe(delay(200));
  }

  private timesOverlap(start1: string, end1: string, start2: string, end2: string): boolean {
    return start1 < end2 && end1 > start2;
  }
}