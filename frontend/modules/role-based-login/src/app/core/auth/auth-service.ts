import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { LoginRequest } from './models/login-request-model';
import { LoginResponse } from './models/login-response-model';
import { SetupPasswordRequest } from './models/setup-password-model';
import { AuthenticatedUser } from './models/authenticated-user-model';
import { TokenStorageService } from './token-storage';
import { MessageResponse } from './models/message-response-model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);

  private readonly AUTH_API =
    'http://localhost:8080/api/v1/auth';

  // Holds the currently logged-in user's details in Angular memory.
  readonly currentUser =
    signal<AuthenticatedUser | null>(null);

  readonly isLoggedIn =
    computed(() =>
      this.tokenStorage.accessToken() !== null
    );

  constructor() {

    // Synchronizes logout across multiple browser tabs.
    window.addEventListener('storage', event => {

      if (
        event.key === 'ssir_refresh_token' &&
        event.newValue === null
      ) {
        this.tokenStorage.clearSession();
        this.currentUser.set(null);

        window.location.replace('/auth/login');
      }
    });
  }


  // Authenticates user and creates the frontend session from returned tokens.
  login(
    credentials: LoginRequest
  ): Observable<LoginResponse> {

    return this.http
      .post<LoginResponse>(
        `${this.AUTH_API}/login`,
        credentials
      )
      .pipe(
        tap(response =>
          this.initializeUserSession(
            response.accessToken,
            response.refreshToken,
            response.companyId
          )
        )
      );
  }


  forgotPassword(email: string): Observable<any> {
    return this.http.post(
      `${this.AUTH_API}/forgot-password`,
      { email }
    );
  }


  resetPassword(payload: any): Observable<any> {
    return this.http.post(
      `${this.AUTH_API}/reset-password`,
      payload
    );
  }


  isAuthenticated(): boolean {
    return Boolean(
      this.tokenStorage.getAccessToken()
    );
  }


  // Uses the refresh token to obtain a new access token.
  refreshToken(): Observable<any> {

    const refreshTokenValue =
      this.tokenStorage.getRefreshToken();

    return this.http.post<any>(
      `${this.AUTH_API}/refresh`,
      { refreshToken: refreshTokenValue }
    )
    .pipe(
      tap(response =>
        this.initializeUserSession(
          response.accessToken,
          response.refreshToken
        )
      )
    );
  }


  setupPassword(
    request: SetupPasswordRequest
  ): Observable<MessageResponse> {

    return this.http.post<MessageResponse>(
      `${this.AUTH_API}/setup-password`,
      request
    );
  }


  logout(): void {
    this.tokenStorage.clearSession();
    this.currentUser.set(null);
  }


  // Used by roleGuard to check whether the user has an allowed role.
  hasAnyRole(expectedRoles: string[]): boolean {

    const user = this.currentUser();

    if (!user) return false;

    return user.roles.some(
      role => expectedRoles.includes(role)
    );
  }


  // Common session setup used after login, signup and token refresh.
  private initializeUserSession(
    accessToken: string,
    refreshToken: string,
    companyId?: number | null
  ): void {

    this.tokenStorage.saveAccessToken(accessToken);
    this.tokenStorage.saveRefreshToken(refreshToken);

    const decodedUser =
      this.tokenStorage.decodeUserFromToken(
        accessToken
      );

    if (decodedUser) {
      this.currentUser.set({
        ...decodedUser,
        companyId:
          companyId !== undefined
            ? companyId
            : decodedUser.companyId
      });
    }
  }


  getCurrentRoles(): string[] {

    const existingUser = this.currentUser();

    if (existingUser) {
      return existingUser.roles;
    }

    const accessToken =
      this.tokenStorage.getAccessToken();

    if (!accessToken) {
      return [];
    }

    const decodedUser =
      this.tokenStorage.decodeUserFromToken(
        accessToken
      );

    if (!decodedUser) {
      return [];
    }

    this.currentUser.set(decodedUser);

    return decodedUser.roles;
  }


  // Company signup/initial registration API; also logs the created user in.
  registerInit(request: {
    email: string;
    username: string;
    password: string;
  }): Observable<any> {

    return this.http
      .post<any>(
        `${this.AUTH_API}/register-init`,
        request
      )
      .pipe(
        tap(response =>
          this.initializeUserSession(
            response.accessToken,
            response.refreshToken,
            response.companyId
          )
        )
      );
  }
}