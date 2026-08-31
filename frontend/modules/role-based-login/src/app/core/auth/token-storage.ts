// ------- AUTHENTICATION/SESSION UTILITY SERVICE -------

import { Injectable, signal, inject } from '@angular/core';
import { AuthenticatedUser } from './models/authenticated-user-model';
import { LoggerService } from '../../layouts/logging/loggerService';

//To make the service injectable application-wide (DI)
@Injectable({
  providedIn: 'root'
})


export class TokenStorageService {

  //internal constant for browser storage "key".
  private readonly REFRESH_TOKEN_KEY = 'ssir_refresh_token';

 //internal signal to hold the access token in Angular memory. (We cant change signal but we can set it)
 //Browser reload --> signal destroyed--> Access token lost(intentionally)
  readonly accessToken = signal<string | null>(null);

  private readonly logger = inject(LoggerService);

  saveAccessToken(token: string): void {
    this.accessToken.set(token);
  }

  getAccessToken(): string | null {
    return this.accessToken();
  }

  //local storage stores key,value pairs->ssir-refresh_token: token
  // signal() - angular, localStorag - browser web api
  saveRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  hasRefreshToken(): boolean {
    return Boolean(this.getRefreshToken());
  }

  clearSession(): void {
    this.accessToken.set(null);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }


  decodeUserFromToken(token: string): AuthenticatedUser | null {
    try {
      //token--> Header.Payload.Signature
      // JWT payload uses Base64URL encoding
      const tokenParts = token.split('.');

      if (tokenParts.length !== 3) {
        return null;
      }

      //Base64URL - '-','_'
      //Base 64 - '+', '/'
      // '-' --> '+'
      // '_' --> '/'
      //g -> global

      const normalizedPayload =
        tokenParts[1]
          .replace(/-/g, '+')
          .replace(/_/g, '/');

      
      //Base64 --> expected length - multiple of 4, less than that padded with '='
      const paddedPayload =
        normalizedPayload.padEnd(
          Math.ceil(normalizedPayload.length / 4) * 4,
          '='
        );

      
      //atob() --> browser function - decodes base64 string
      const decodedJson =
        JSON.parse(atob(paddedPayload));

      return {
        id: Number(decodedJson.sub),
        userIdString: decodedJson.userIdString ?? '',
        username: decodedJson.username ?? '',
        email: decodedJson.email ?? '',
        roles: decodedJson.roles ?? [],
        companyId: decodedJson.companyId ?? null
      };

    } catch (error) {
      const errorMessage =
  error instanceof Error
    ? error.message
    : 'Unknown error';

this.logger.error(
  `[TokenStorage] Unable to decode JWT: ${errorMessage}`
);

      return null;
    }
  }
}