import { Injectable, inject, signal, computed } from '@angular/core'; //Computed Signal is a derived value
import { HttpClient } from '@angular/common/http';//Angular API for makinh HTTP calls, return obeservables
import { Observable, tap, throwError } from 'rxjs';//asynchronously produce response or error, tap() for side effects and pass value without transforming like map()
import { LoginRequest } from './models/login-request-model';
import { LoginResponse } from './models/login-response-model';
import { RefreshTokenResponse } from './models/refresh-token-response';
import { SetupPasswordRequest } from './models/setup-password-model';
import { AuthenticatedUser } from './models/authenticated-user-model';
import { TokenStorageService } from './token-storage';
import { MessageResponse } from './models/message-response-model';
import { LoggerService } from '../../layouts/logging/loggerService';
import { environment } from '../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  //Authservice uses these properties directly and their references are not re-assigned later
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly logger = inject(LoggerService);
  private readonly AUTH_API =
    `${environment.apiBaseUrl}/auth`;

  // Holds the currently logged-in user's details in Angular memory.
  readonly currentUser =
    signal<AuthenticatedUser | null>(null);

  // isLoggedIn should always be derived from whether an access token exists.
  // recalculates automatically when that accessToken() Signal changes.It is derived value We cant set().
  readonly isLoggedIn =
    computed(() =>
      this.currentUser() !== null
    );
  
  
  // Runs when angular creates AuthService instance- since we are using providerIn: 'root'
  // its a root level instance(application level service instance), Inside this we register browser event-listener
  constructor() {
    
    //listening for browser event assocaited with change in local storage
    window.addEventListener('storage', event => {

      if (
        event.key === 'ssir_refresh_token' &&
        event.newValue === null
      ) {

        this.logger.info('Authentication session cleared in another browser tab');
        // removes access token and refresh token
        this.tokenStorage.clearSession();
        this.currentUser.set(null);

        window.location.replace('/auth/login'); //removes current entry from browser history, so back button wont take to authenticated route
      }
    });
  }


  // Authenticates user and creates the frontend session from returned tokens.
  // Input--> LoginRequest Output--> async LoginResponse
  login(
    credentials: LoginRequest
  ): Observable<LoginResponse> {
    //post(url,requestBody)
    //pipe() - is how RxJS operators are chained to an Observable
    //tap() - is side effect, although we initialized user session, we still get entire response as output of login() function
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
    return this.currentUser()!==null;

  }


  // Uses the refresh token to obtain a new access token.
  refreshToken(): Observable<RefreshTokenResponse> {
 
  const refreshTokenValue =
    this.tokenStorage.getRefreshToken();

  if (!refreshTokenValue) {
    return throwError(
      () => new Error(
        'No refresh token available'
      )
    );
  }

   const existingCompanyId =
    this.currentUser()?.companyId;

  return this.http
    .post<RefreshTokenResponse>(
      `${this.AUTH_API}/refresh`,
      {
        refreshToken: refreshTokenValue
      }
    )
    .pipe(
      tap(response =>
        this.initializeUserSession(
          response.accessToken,
          response.refreshToken,
          existingCompanyId
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

    this.logger.info('Authentication session cleared');
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

    const decodedUser =
      this.tokenStorage.decodeUserFromToken(
        accessToken
      );

    if (!decodedUser) {

      this.tokenStorage.clearSession();
      this.currentUser.set(null);

      return;
    }
    this.tokenStorage.saveAccessToken(accessToken);
    this.tokenStorage.saveRefreshToken(refreshToken);



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