import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { Student, StudentRequest, Major } from '../models';

export interface StudentFilters {
  major?: Major;
  level?: number;
}

@Injectable({
  providedIn: 'root'
})
export class StudentService {
  private readonly apiUrl = `${environment.apiUrl}/students`;

  constructor(private http: HttpClient) {}

  getAll(filters?: StudentFilters): Observable<Student[]> {
    let params = new HttpParams();

    if (filters?.major) {
      params = params.set('major', filters.major);
    }

    if (filters?.level !== undefined && filters.level !== null) {
      params = params.set('level', filters.level.toString());
    }

    return this.http.get<Student[]>(this.apiUrl, { params });
  }

  getById(id: number): Observable<Student> {
    return this.http.get<Student>(`${this.apiUrl}/${id}`);
  }

  getByUserId(userId: number): Observable<Student> {
    return this.http.get<Student>(`${this.apiUrl}/user/${userId}`);
  }

  create(student: StudentRequest): Observable<Student> {
    return this.http.post<Student>(this.apiUrl, student);
  }

  update(id: number, student: StudentRequest): Observable<Student> {
    return this.http.put<Student>(`${this.apiUrl}/${id}`, student);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getMe(): Observable<Student> {
    return this.http.get<Student>(`${this.apiUrl}/me`);
  }
  
}