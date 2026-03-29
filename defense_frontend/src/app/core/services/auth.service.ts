import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';
import { environment } from '@environments/environment';
import { AuthResponse, LoginRequest, Role } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';

  private currentUserSignal = signal<null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.getAccessToken());

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials).pipe(
      tap(response => {
        this.setAccessToken(response.accessToken);
        this.setRefreshToken(response.refreshToken);
      })
    );
  }

  logout(): Observable<void> {
    const token = this.getAccessToken();

    if (!token) {
      this.clearSession();
      this.router.navigate(['/login']);
      return of(void 0);
    }

    return this.http.post<void>(`${environment.apiUrl}/auth/logout`, {}).pipe(
      tap(() => {
        this.clearSession();
        this.router.navigate(['/login']);
      }),
      catchError((error) => {
        this.clearSession();
        this.router.navigate(['/login']);
        return throwError(() => error);
      })
    );
  }

  refreshToken(): Observable<string> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/refresh-token`, { refreshToken })
      .pipe(
        tap(response => {
          this.setAccessToken(response.accessToken);
          this.setRefreshToken(response.refreshToken);
        }),
        map(response => response.accessToken)
      );
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getToken(): string | null {
    return this.getAccessToken();
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  hasRole(roles: Role[]): boolean {
    const currentRole = this.getCurrentRole();
    return currentRole ? roles.includes(currentRole) : false;
  }

  getCurrentRole(): Role | null {
    const token = this.getAccessToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles: string[] = payload?.roles || payload?.authorities || [];

      if (roles.includes('ROLE_ADMIN')) return Role.ADMIN;
      if (roles.includes('ROLE_PROFESSOR')) return Role.PROFESSOR;
      if (roles.includes('ROLE_STUDENT')) return Role.STUDENT;

      return null;
    } catch {
      return null;
    }
  }

  getUsername(): string | null {
    const token = this.getAccessToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.sub || payload?.username || null;
    } catch {
      return null;
    }
  }

  private setAccessToken(token: string): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, token);
  }

  private setRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
  }

  private clearSession(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    this.currentUserSignal.set(null);
  }
}