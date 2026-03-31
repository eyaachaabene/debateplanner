import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { Professor, ProfessorRequest } from '../models';

export interface ProfessorFilters {
  search?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProfessorService {
  private readonly apiUrl = `${environment.apiUrl}/professors`;
  private readonly defenseApiUrl = `${environment.apiUrl}/defenses`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Professor[]> {
    return this.http.get<Professor[]>(this.apiUrl);
  }

  getById(id: number): Observable<Professor> {
    return this.http.get<Professor>(`${this.apiUrl}/${id}`);
  }

  getByUserId(userId: number): Observable<Professor> {
    return this.http.get<Professor>(`${this.apiUrl}/user/${userId}`);
  }

  create(professor: ProfessorRequest): Observable<Professor> {
    return this.http.post<Professor>(this.apiUrl, professor);
  }

  update(id: number, professor: ProfessorRequest): Observable<Professor> {
    return this.http.put<Professor>(`${this.apiUrl}/${id}`, professor);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getAvailableForRole(
    role: 'SUPERVISOR' | 'PRESIDENT' | 'REVIEWER' | 'EXAMINER',
    date: string,
    startTime: string,
    endTime: string,
    excludeDefenseId?: number
  ): Observable<Professor[]> {
    let params = new HttpParams()
      .set('role', role)
      .set('date', date)
      .set('startTime', startTime)
      .set('endTime', endTime);

    if (excludeDefenseId !== undefined) {
      params = params.set('excludeDefenseId', excludeDefenseId.toString());
    }

    return this.http.get<Professor[]>(`${this.defenseApiUrl}/available-jury-members`, { params });
  }

  getMe(): Observable<Professor> {
    return this.http.get<Professor>(`${this.apiUrl}/me`);
  }
}
