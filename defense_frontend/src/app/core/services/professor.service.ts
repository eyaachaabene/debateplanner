import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, forkJoin } from 'rxjs';
import { map, shareReplay, tap } from 'rxjs/operators';
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
  
  // Cache for professor names
  private professorNamesCache = new Map<number, Observable<string>>();
  private allProfessorsCache: Observable<Professor[]> | null = null;
  private professorsMapCache = new Map<number, string>();

  constructor(private http: HttpClient) {}

  getAll(): Observable<Professor[]> {
    return this.http.get<Professor[]>(this.apiUrl);
  }

  // Load all professors and cache them
  loadAllProfessors(): Observable<Professor[]> {
    if (!this.allProfessorsCache) {
      this.allProfessorsCache = this.http.get<Professor[]>(this.apiUrl).pipe(
        tap(professors => {
          // Build the map for quick name lookup
          this.professorsMapCache.clear();
          professors.forEach(professor => {
            this.professorsMapCache.set(professor.id, `${professor.firstName} ${professor.lastName}`);
          });
        }),
        shareReplay(1)
      );
    }
    return this.allProfessorsCache;
  }

  // Get professor name from cached map (synchronous)
  getProfessorNameFromCache(id: number): string {
    return this.professorsMapCache.get(id) || `Professeur #${id}`;
  }

  // Check if professors are loaded
  isProfessorsLoaded(): boolean {
    return this.professorsMapCache.size > 0;
  }

  getById(id: number): Observable<Professor> {
    return this.http.get<Professor>(`${this.apiUrl}/${id}`);
  }

  getByUserId(userId: number): Observable<Professor> {
    return this.http.get<Professor>(`${this.apiUrl}/user/${userId}`);
  }

  create(professor: ProfessorRequest): Observable<Professor> {
    // Clear cache when data changes
    this.clearCache();
    return this.http.post<Professor>(this.apiUrl, professor);
  }

  update(id: number, professor: ProfessorRequest): Observable<Professor> {
    // Clear cache when data changes
    this.clearCache();
    return this.http.put<Professor>(`${this.apiUrl}/${id}`, professor);
  }

  delete(id: number): Observable<void> {
    // Clear cache when data changes
    this.clearCache();
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

  // Get professor name by ID with caching
  getNameById(id: number): Observable<string> {
    if (!this.professorNamesCache.has(id)) {
      const name$ = this.http.get<Professor>(`${this.apiUrl}/${id}`).pipe(
        map((professor: Professor) => `${professor.firstName} ${professor.lastName}`),
        shareReplay(1)
      );
      this.professorNamesCache.set(id, name$);
    }
    return this.professorNamesCache.get(id)!;
  }

  // Clear all caches
  clearCache(): void {
    this.professorNamesCache.clear();
    this.allProfessorsCache = null;
    this.professorsMapCache.clear();
  }

  // Get multiple professor names at once
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