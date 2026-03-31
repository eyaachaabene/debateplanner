import { Injectable } from '@angular/core';
import { Observable, of, delay, throwError } from 'rxjs';
import {
  Defense,
  DefenseRequest,
  CheckConflictsRequest,
  JuryAssignmentRequest,
  DefenseGrades,
  Mention
} from '../models';
import { MOCK_DEFENSES } from './mock-data';
import { DefenseFilters } from '../services/defense.service';
import { DefenseStatus } from '../models/enums';

@Injectable({
  providedIn: 'root'
})
export class MockDefenseService {
  private defenses: Defense[] = [...MOCK_DEFENSES];
  private nextId = this.defenses.length + 1;

  getAll(filters?: DefenseFilters): Observable<Defense[]> {
    let filtered = [...this.defenses];

    if (filters?.status) {
      filtered = filtered.filter(d => d.status === filters.status);
    }

    if (filters?.date) {
      filtered = filtered.filter(d => d.defenseDate === filters.date);
    }

    filtered.sort((a, b) => a.defenseDate.localeCompare(b.defenseDate));

    return of(filtered).pipe(delay(300));
  }

  getById(id: number): Observable<Defense> {
    const defense = this.defenses.find(d => d.id === id);

    if (defense) {
      return of(defense).pipe(delay(200));
    }

    return throwError(() => new Error('Soutenance non trouvée')).pipe(delay(200));
  }

  create(request: DefenseRequest): Observable<Defense> {
    const newDefense: Defense = {
      id: this.nextId++,
      ...request,
      status: DefenseStatus.PLANNED
    };

    this.defenses.push(newDefense);
    return of(newDefense).pipe(delay(400));
  }

  update(id: number, request: DefenseRequest): Observable<Defense> {
    const index = this.defenses.findIndex(d => d.id === id);

    if (index !== -1) {
      this.defenses[index] = {
        ...this.defenses[index],
        ...request
      };

      return of(this.defenses[index]).pipe(delay(400));
    }

    return throwError(() => new Error('Soutenance non trouvée')).pipe(delay(200));
  }

  delete(id: number): Observable<void> {
    const index = this.defenses.findIndex(d => d.id === id);

    if (index !== -1) {
      this.defenses.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }

    return throwError(() => new Error('Soutenance non trouvée')).pipe(delay(200));
  }

  checkConflicts(request: CheckConflictsRequest): Observable<{ hasConflicts: boolean; errors: string[] }> {
    const errors: string[] = [];

    const conflicting = this.defenses.filter(d =>
      d.defenseDate === request.defenseDate &&
      (request.excludeDefenseId === undefined || d.id !== request.excludeDefenseId) &&
      this.timesOverlap(d.startTime, d.endTime, request.startTime, request.endTime)
    );

    if (conflicting.some(d => d.roomId === request.roomId)) {
      errors.push('La salle est déjà occupée sur ce créneau.');
    }

    if (conflicting.some(d => d.studentId === request.studentId)) {
      errors.push('L’étudiant a déjà une soutenance sur ce créneau.');
    }

    if (conflicting.some(d =>
      [d.supervisorId, d.presidentId, d.reviewerId, d.examinerId].includes(request.supervisorId)
    )) {
      errors.push('L’encadrant est déjà pris sur ce créneau.');
    }

    if (conflicting.some(d =>
      [d.supervisorId, d.presidentId, d.reviewerId, d.examinerId].includes(request.presidentId)
    )) {
      errors.push('Le président est déjà pris sur ce créneau.');
    }

    if (conflicting.some(d =>
      [d.supervisorId, d.presidentId, d.reviewerId, d.examinerId].includes(request.reviewerId)
    )) {
      errors.push('Le rapporteur est déjà pris sur ce créneau.');
    }

    if (conflicting.some(d =>
      [d.supervisorId, d.presidentId, d.reviewerId, d.examinerId].includes(request.examinerId)
    )) {
      errors.push('L’examinateur est déjà pris sur ce créneau.');
    }

    return of({
      hasConflicts: errors.length > 0,
      errors
    }).pipe(delay(250));
  }

  updateJury(id: number, request: JuryAssignmentRequest): Observable<any> {
    const index = this.defenses.findIndex(d => d.id === id);

    if (index !== -1) {
      this.defenses[index] = {
        ...this.defenses[index],
        supervisorId: request.supervisorId,
        presidentId: request.presidentId,
        reviewerId: request.reviewerId,
        examinerId: request.examinerId
      };

      return of(request).pipe(delay(300));
    }

    return throwError(() => new Error('Soutenance non trouvée')).pipe(delay(200));
  }

  getJury(id: number): Observable<JuryAssignmentRequest> {
    const defense = this.defenses.find(d => d.id === id);

    if (!defense) {
      return throwError(() => new Error('Soutenance non trouvée')).pipe(delay(200));
    }

    return of({
      supervisorId: defense.supervisorId,
      presidentId: defense.presidentId,
      reviewerId: defense.reviewerId,
      examinerId: defense.examinerId
    }).pipe(delay(200));
  }

  submitPresidentGrade(id: number, grade: number): Observable<any> {
    return this.updateGrade(id, 'presidentGrade', grade);
  }

  submitReviewerGrade(id: number, grade: number): Observable<any> {
    return this.updateGrade(id, 'reviewerGrade', grade);
  }

  submitExaminerGrade(id: number, grade: number): Observable<any> {
    return this.updateGrade(id, 'examinerGrade', grade);
  }

  submitSupervisorGrade(id: number, grade: number): Observable<any> {
    return this.updateGrade(id, 'supervisorGrade', grade);
  }

  getGrades(id: number): Observable<DefenseGrades> {
    const defense = this.defenses.find(d => d.id === id);

    if (!defense) {
      return throwError(() => new Error('Soutenance non trouvée')).pipe(delay(200));
    }

    return of({
      presidentGrade: defense.presidentGrade,
      reviewerGrade: defense.reviewerGrade,
      examinerGrade: defense.examinerGrade,
      supervisorGrade: defense.supervisorGrade,
      finalAverage: defense.finalAverage,
      mention: defense.mention
    }).pipe(delay(200));
  }

  publish(id: number): Observable<any> {
    const index = this.defenses.findIndex(d => d.id === id);

    if (index === -1) {
      return throwError(() => new Error('Soutenance non trouvée')).pipe(delay(200));
    }

    const defense = this.defenses[index];
    const grades = [
      defense.presidentGrade,
      defense.reviewerGrade,
      defense.examinerGrade,
      defense.supervisorGrade
    ].filter((g): g is number => g !== undefined);

    if (grades.length === 0) {
      return throwError(() => new Error('Aucune note disponible pour publication')).pipe(delay(200));
    }

    const average = Math.round((grades.reduce((a, b) => a + b, 0) / grades.length) * 100) / 100;

    this.defenses[index] = {
      ...defense,
      finalAverage: average,
      mention: this.computeMention(average),
        status: DefenseStatus.PUBLISHED
    };

    return of({ success: true }).pipe(delay(300));
  }

  getResult(id: number): Observable<Defense> {
    const defense = this.defenses.find(d => d.id === id && d.status === DefenseStatus.PUBLISHED);

    if (!defense) {
      return throwError(() => new Error('Résultat non disponible')).pipe(delay(200));
    }

    return of(defense).pipe(delay(200));
  }

  getByStudent(studentId: number): Observable<Defense> {
    const defense = this.defenses.find(d => d.studentId === studentId);

    if (!defense) {
      return throwError(() => new Error('Aucune soutenance trouvée')).pipe(delay(200));
    }

    return of(defense).pipe(delay(200));
  }

  getMyResult(): Observable<Defense> {
    const defense = this.defenses.find(d => d.studentId === 1 && d.status === DefenseStatus.PUBLISHED);

    if (!defense) {
      return throwError(() => new Error('Résultat non disponible')).pipe(delay(200));
    }

    return of(defense).pipe(delay(200));
  }

  getJuryDefenses(status?: 'UPCOMING' | 'TODO' | 'DONE'): Observable<Defense[]> {
    let filtered = [...this.defenses];

    if (status === 'UPCOMING') {
      filtered = filtered.filter(d => d.status === DefenseStatus.PLANNED);
    } else if (status === 'DONE') {
        filtered = filtered.filter(d => d.status === DefenseStatus.PUBLISHED);
    }

    return of(filtered).pipe(delay(200));
  }

  private updateGrade(id: number, field: keyof Defense, grade: number): Observable<any> {
    const index = this.defenses.findIndex(d => d.id === id);

    if (index === -1) {
      return throwError(() => new Error('Soutenance non trouvée')).pipe(delay(200));
    }

    this.defenses[index] = {
      ...this.defenses[index],
      [field]: grade
    };

    return of({ success: true }).pipe(delay(300));
  }

  private computeMention(average: number): Mention {
    if (average >= 18) return Mention.EXCELLENT;
    if (average >= 16) return Mention.VERY_GOOD;
    if (average >= 14) return Mention.GOOD;
    if (average >= 12) return Mention.FAIRLY_GOOD;
    if (average >= 10) return Mention.PASSABLE;
    return Mention.FAIL;
  }

  private timesOverlap(start1: string, end1: string, start2: string, end2: string): boolean {
    return start1 < end2 && end1 > start2;
  }
}
