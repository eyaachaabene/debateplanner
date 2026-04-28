import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map, shareReplay, tap } from 'rxjs/operators';
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
  
  // Cache for student names
  private studentNamesCache = new Map<number, Observable<string>>();
  private allStudentsCache: Observable<Student[]> | null = null;
  private studentsMapCache = new Map<number, string>();

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

  // Load all students and cache them
  loadAllStudents(): Observable<Student[]> {
    if (!this.allStudentsCache) {
      this.allStudentsCache = this.http.get<Student[]>(this.apiUrl).pipe(
        tap(students => {
          // Build the map for quick name lookup
          this.studentsMapCache.clear();
          students.forEach(student => {
            this.studentsMapCache.set(student.id, `${student.firstName} ${student.lastName}`);
          });
        }),
        shareReplay(1)
      );
    }
    return this.allStudentsCache;
  }

  // Get student name from cached map (synchronous)
  getStudentNameFromCache(id: number): string {
    return this.studentsMapCache.get(id) || `Étudiant #${id}`;
  }

  // Check if students are loaded
  isStudentsLoaded(): boolean {
    return this.studentsMapCache.size > 0;
  }

  getById(id: number): Observable<Student> {
    return this.http.get<Student>(`${this.apiUrl}/${id}`);
  }

  getByUserId(userId: number): Observable<Student> {
    return this.http.get<Student>(`${this.apiUrl}/user/${userId}`);
  }

  create(student: StudentRequest): Observable<Student> {
    console.log('service0', student);    
    console.log('service1', this.http.post<Student>(this.apiUrl, student));
    // Clear cache when data changes
    this.clearCache();
    return this.http.post<Student>(this.apiUrl, student);
  }

  update(id: number, student: StudentRequest): Observable<Student> {
    // Clear cache when data changes
    this.clearCache();
    return this.http.put<Student>(`${this.apiUrl}/${id}`, student);
  }

  delete(id: number): Observable<void> {
    // Clear cache when data changes
    this.clearCache();
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getMe(): Observable<Student> {
    return this.http.get<Student>(`${this.apiUrl}/me`);
  }

  // Get student name by ID with caching
  getNameById(id: number): Observable<string> {
    if (!this.studentNamesCache.has(id)) {
      const name$ = this.http.get<Student>(`${this.apiUrl}/${id}`).pipe(
        map((student: Student) => `${student.firstName} ${student.lastName}`),
        shareReplay(1)
      );
      this.studentNamesCache.set(id, name$);
    }
    return this.studentNamesCache.get(id)!;
  }

  // Clear all caches
  clearCache(): void {
    this.studentNamesCache.clear();
    this.allStudentsCache = null;
    this.studentsMapCache.clear();
  }

  // Get multiple student names at once
  getNamesByIds(ids: number[]): Observable<Map<number, string>> {
    const uniqueIds = [...new Set(ids)];
    const requests = uniqueIds.map(id => this.getNameById(id));
    
    return forkJoin(requests).pipe(
      map((names: string[]) => {
        const nameMap = new Map<number, string>();
        uniqueIds.forEach((id, index) => {
          nameMap.set(id, names[index]);
        });
        return nameMap;
      })
    );
  }
}