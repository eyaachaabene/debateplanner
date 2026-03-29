import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  Defense,
  DefenseRequest,
  CheckConflictsRequest,
  JuryAssignmentRequest,
  DefenseGrades
} from '../models';
import { DefenseStatus } from '../models/enums';

export interface DefenseFilters {
  status?: DefenseStatus;
  date?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DefenseService {
  private readonly apiUrl = `${environment.apiUrl}/defenses`;
  private readonly juryApiUrl = `${environment.apiUrl}/jury/defenses`;

  constructor(private http: HttpClient) {}

  getAll(filters?: DefenseFilters): Observable<Defense[]> {
    let params = new HttpParams();

    if (filters?.status) {
      params = params.set('status', filters.status);
    }

    if (filters?.date) {
      params = params.set('date', filters.date);
    }

    return this.http.get<Defense[]>(this.apiUrl, { params });
  }

  getById(id: number): Observable<Defense> {
    return this.http.get<Defense>(`${this.apiUrl}/${id}`);
  }

  create(defense: DefenseRequest): Observable<Defense> {
    return this.http.post<Defense>(this.apiUrl, defense);
  }

  update(id: number, defense: DefenseRequest): Observable<Defense> {
    return this.http.put<Defense>(`${this.apiUrl}/${id}`, defense);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  checkConflicts(request: CheckConflictsRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/check-conflicts`, request);
  }

  updateJury(id: number, request: JuryAssignmentRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}/jury`, request);
  }

  getJury(id: number): Observable<JuryAssignmentRequest> {
    return this.http.get<JuryAssignmentRequest>(`${this.apiUrl}/${id}/jury`);
  }

  submitPresidentGrade(id: number, grade: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/grades/president`, { grade });
  }

  submitReviewerGrade(id: number, grade: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/grades/reviewer`, { grade });
  }

  submitExaminerGrade(id: number, grade: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/grades/examiner`, { grade });
  }

  submitSupervisorGrade(id: number, grade: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/grades/supervisor`, { grade });
  }

  getGrades(id: number): Observable<DefenseGrades> {
    return this.http.get<DefenseGrades>(`${this.apiUrl}/${id}/grades`);
  }

  publish(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/publish`, {});
  }

  getResult(id: number): Observable<Defense> {
    return this.http.get<Defense>(`${this.apiUrl}/${id}/result`);
  }

  getByStudent(studentId: number): Observable<Defense> {
    return this.http.get<Defense>(`${environment.apiUrl}/students/${studentId}/result`);
  }

  getMyResult(): Observable<Defense> {
    return this.http.get<Defense>(`${environment.apiUrl}/students/me/result`);
  }

  getJuryDefenses(status?: 'UPCOMING' | 'TODO' | 'DONE'): Observable<Defense[]> {
    let params = new HttpParams();

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<Defense[]>(this.juryApiUrl, { params });
  }
}