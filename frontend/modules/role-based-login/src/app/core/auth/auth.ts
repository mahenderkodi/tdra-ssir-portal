import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest } from './models/login-request-model';
import { LoginResponse } from './models/login-response-model';
import { SetupPasswordRequest } from './models/setup-password-model';
import { AuthenticatedUser } from './models/authenticated-user-model';
import { TokenStorageService } from './token-storage';

@Injectable({
  providedIn: 'root'
})

export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly AUTH_API = 'http://localhost:8080/api/v1/auth';

  // State Signals
  readonly currentUser = signal<AuthenticatedUser | null>(null);
  readonly isLoggedIn = computed(() => this.tokenStorage.accessToken() !== null);

  constructor() {
    this.autoInitializeSession();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.AUTH_API}/login`, credentials).pipe(
      tap(response => this.initializeUserSession(response.accessToken, response.refreshToken))
    );
  }

  // Executes token rotation handshake with the backend
  refreshToken(): Observable<any> {
    const refreshTokenValue = this.tokenStorage.getRefreshToken();
    return this.http.post<any>(`${this.AUTH_API}/refresh`, { refreshToken: refreshTokenValue }).pipe(
      tap(response => this.initializeUserSession(response.accessToken, response.refreshToken))
    );
  }

  setupPassword(request: SetupPasswordRequest): Observable<any> {
    return this.http.post<any>(`${this.AUTH_API}/setup-password`, request);
  }

  logout(): void {
    this.tokenStorage.clearSession();
    this.currentUser.set(null);
  }

  hasAnyRole(expectedRoles: string[]): boolean {
    const user = this.currentUser();
    if (!user) return false;
    return user.roles.some(role => expectedRoles.includes(role));
  }

  private initializeUserSession(accessToken: string, refreshToken: string): void {
    this.tokenStorage.saveAccessToken(accessToken);
    this.tokenStorage.saveRefreshToken(refreshToken);
    const decodedUser = this.tokenStorage.decodeUserFromToken(accessToken);
    this.currentUser.set(decodedUser);
  }

  private autoInitializeSession(): void {
    const hasRefreshToken = this.tokenStorage.getRefreshToken() !== null;
    if (hasRefreshToken) {
      this.refreshToken().subscribe({
        error: () => this.logout() // Force session invalidation if refresh token has expired
      });
    }
  }
}