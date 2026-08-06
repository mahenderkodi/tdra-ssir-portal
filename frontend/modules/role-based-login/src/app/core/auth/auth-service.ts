import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest } from './models/login-request-model';
import { LoginResponse } from './models/login-response-model';
import { SetupPasswordRequest } from './models/setup-password-model';
import { AuthenticatedUser } from './models/authenticated-user-model';
import { TokenStorageService } from './token-storage';
import {MessageResponse} from './models/message-response-model';

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
    // this.autoInitializeSession();
      window.addEventListener(
    'storage',
    event => {

      /*
       * This event runs in other tabs when
       * the refresh token is removed.
       */
      if (
        event.key === 'ssir_refresh_token' &&
        event.newValue === null
      ) {
        console.log(
          '[AuthService] Logout detected from another tab'
        );

        /*
         * Clear this tab's in-memory access token
         * and logged-in user.
         */
        this.tokenStorage.clearSession();
        this.currentUser.set(null);

        /*
         * Immediately open the sign-in page.
         */
        window.location.replace(
          '/auth/login'
        );
      }
    }
  );
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.AUTH_API}/login`, credentials).pipe(
      tap(response => this.initializeUserSession(response.accessToken, response.refreshToken))
    );
  }

   forgotPassword(email: string): Observable<any> {
    return this.http.post<any>(`${this.AUTH_API}/forgot-password`, { email: email });
  }

  resetPassword(payload: any): Observable<any> {
    return this.http.post<any>(`${this.AUTH_API}/reset-password`, payload);
  }

  
   isAuthenticated(): boolean {
    return Boolean(
      this.tokenStorage.getAccessToken()
    );
  }
  // Executes token rotation handshake with the backend
  refreshToken(): Observable<any> {
    const refreshTokenValue = this.tokenStorage.getRefreshToken();
    return this.http.post<any>(`${this.AUTH_API}/refresh`, { refreshToken: refreshTokenValue }).pipe(
      tap(response => this.initializeUserSession(response.accessToken, response.refreshToken))
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

  hasAnyRole(expectedRoles: string[]): boolean {
    const user = this.currentUser();
    if (!user) return false;
    return user.roles.some(role => expectedRoles.includes(role));
  }

  private initializeUserSession(accessToken: string, refreshToken: string): void {

     console.log(
    '[AuthService] Access token received:',
    Boolean(accessToken)
  );

  console.log(
    '[AuthService] Refresh token received:',
    Boolean(refreshToken)
  );
    this.tokenStorage.saveAccessToken(accessToken);
    this.tokenStorage.saveRefreshToken(refreshToken);
    const decodedUser = this.tokenStorage.decodeUserFromToken(accessToken);
    this.currentUser.set(decodedUser);
  }

 private autoInitializeSession(): void {

  const refreshToken =
    this.tokenStorage.getRefreshToken();

  console.log(
    '[AuthService] Auto initialization started'
  );

  console.log(
    '[AuthService] Refresh token exists:',
    Boolean(refreshToken)
  );

  if (refreshToken) {

    console.log(
      '[AuthService] Calling refresh API'
    );

    this.refreshToken().subscribe({
      next: response => {
        console.log(
          '[AuthService] Refresh successful:',
          response
        );
      },

      error: error => {
        console.error(
          '[AuthService] Refresh failed:',
          error
        );

        /*
         * Temporarily do not call logout,
         * so we can inspect the error.
         */
      }
    });
  }
}

  getCurrentRoles(): string[] {

  /*
   * First, check whether the current user
   * is already available in memory.
   */
  const existingUser =
    this.currentUser();

  if (existingUser) {
    return existingUser.roles;
  }


  /*
   * This situation can happen after
   * refreshing the browser.
   *
   * The token may still exist, but the
   * currentUser signal may be empty.
   */
  const accessToken =
    this.tokenStorage.getAccessToken();

  if (!accessToken) {
    return [];
  }


  /*
   * Read the logged-in user from the
   * stored JWT access token.
   */
  const decodedUser =
    this.tokenStorage.decodeUserFromToken(
      accessToken
    );

  if (!decodedUser) {
    return [];
  }


  /*
   * Restore the current user signal.
   */
  this.currentUser.set(decodedUser);

  return decodedUser.roles;
}
}