import { Injectable, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, of, delay, throwError, tap } from 'rxjs';
import { AuthResponse, LoginRequest, Role } from '../models';
import { MOCK_USERS } from './mock-data';

@Injectable({
  providedIn: 'root'
})
export class MockAuthService {
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly AUTH_RESPONSE_KEY = 'auth_response';

  private currentUserSignal = signal<null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.getAccessToken());

  private readonly mockCredentials: Record<string, { password: string; role: Role }> = {
    'admin': { password: 'admin123', role: Role.ADMIN },
    'prof.martin': { password: 'prof123', role: Role.PROFESSOR },
    'prof.bernard': { password: 'prof123', role: Role.PROFESSOR },
    'prof.petit': { password: 'prof123', role: Role.PROFESSOR },
    'etudiant.leroy': { password: 'student123', role: Role.STUDENT },
    'etudiant.moreau': { password: 'student123', role: Role.STUDENT }
  };

  constructor(private router: Router) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    const mockAuth = this.mockCredentials[credentials.username];

    if (mockAuth && mockAuth.password === credentials.password) {
      const accessToken = this.buildMockJwt(credentials.username, mockAuth.role);

      const response: AuthResponse = {
        accessToken,
        refreshToken: 'mock-refresh-token-' + Date.now(),
        tokenType: 'Bearer',
        expiresIn: 3600,
        mustChangePassword: false
      };

      return of(response).pipe(
        delay(500),
        tap(res => {
          this.setAuthResponse(res);
        })
      );
    }

    return throwError(() => new Error('Nom d’utilisateur ou mot de passe incorrect')).pipe(delay(500));
  }

  refreshToken(): Observable<string> {
    const authResponse = this.getStoredAuthResponse();

    if (!authResponse) {
      return throwError(() => new Error('No refresh token available'));
    }

    return of(authResponse.accessToken).pipe(delay(200));
  }

  changePassword(request: { currentPassword: string; newPassword: string }): Observable<void> {
    const authResponse = this.getStoredAuthResponse();
    const username = this.getUsername();

    if (!authResponse || !username) {
      return throwError(() => new Error('Not authenticated'));
    }

    const mockAuth = this.mockCredentials[username];

    if (!mockAuth || mockAuth.password !== request.currentPassword) {
      return throwError(() => new Error('Current password is incorrect'));
    }

    this.mockCredentials[username] = {
      ...mockAuth,
      password: request.newPassword
    };

    this.clearMustChangePasswordFlag();
    return of(void 0).pipe(delay(300));
  }

  logout(): Observable<void> {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.AUTH_RESPONSE_KEY);
    this.currentUserSignal.set(null);
    this.router.navigate(['/login']);
    return of(void 0);
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

  getStoredAuthResponse(): AuthResponse | null {
    const rawResponse = localStorage.getItem(this.AUTH_RESPONSE_KEY);

    if (!rawResponse) {
      return null;
    }

    try {
      return JSON.parse(rawResponse) as AuthResponse;
    } catch {
      return null;
    }
  }

  isPasswordChangeRequired(): boolean {
    return this.getStoredAuthResponse()?.mustChangePassword ?? false;
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
      const tokenRoles: string[] = payload?.roles || payload?.authorities || [];

      if (tokenRoles.includes('ROLE_ADMIN')) return Role.ADMIN;
      if (tokenRoles.includes('ROLE_PROFESSOR')) return Role.PROFESSOR;
      if (tokenRoles.includes('ROLE_STUDENT')) return Role.STUDENT;

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

  getDemoCredentials(): { role: string; username: string; password: string }[] {
    return [
      { role: 'Administrateur', username: 'admin', password: 'admin123' },
      { role: 'Professeur', username: 'prof.martin', password: 'prof123' },
      { role: 'Étudiant', username: 'etudiant.leroy', password: 'student123' }
    ];
  }

  private setAccessToken(token: string): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, token);
  }

  private setRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
  }

  private setAuthResponse(response: AuthResponse): void {
    this.setAccessToken(response.accessToken);
    this.setRefreshToken(response.refreshToken);
    localStorage.setItem(this.AUTH_RESPONSE_KEY, JSON.stringify(response));
  }

  private clearMustChangePasswordFlag(): void {
    const storedResponse = this.getStoredAuthResponse();

    if (!storedResponse) {
      return;
    }

    storedResponse.mustChangePassword = false;
    localStorage.setItem(this.AUTH_RESPONSE_KEY, JSON.stringify(storedResponse));
  }

  private buildMockJwt(username: string, role: Role): string {
    const header = {
      alg: 'HS256',
      typ: 'JWT'
    };

    const payload = {
      sub: username,
      roles: [`ROLE_${role}`],
      authorities: [`ROLE_${role}`]
    };

    const encode = (obj: object) =>
      btoa(JSON.stringify(obj)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

    return `${encode(header)}.${encode(payload)}.mock-signature`;
  }
}