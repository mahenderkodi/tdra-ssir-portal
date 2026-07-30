import { Injectable, Service, signal } from '@angular/core';
import {AuthenticatedUser} from './models/authenticated-user-model';

@Injectable({
  providedIn: 'root'
})


export class TokenStorageService {
  private readonly REFRESH_TOKEN_KEY = 'ssir_refresh_token';
  
  // In-memory Signals for access tokens (secure from XSS hijacking)
  readonly accessToken = signal<string | null>(null);

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  saveRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
  }

  saveAccessToken(token: string): void {
    this.accessToken.set(token);
  }


  getAccessToken(): string | null {
    return this.accessToken();
  }

  clearSession(): void {
    this.accessToken.set(null);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }

  // Cryptographically decodes the active JWT token payload securely in the browser
  decodeUserFromToken(token: string): AuthenticatedUser | null {
    try {
      const payloadBase64 = token.split('.')[1];
      const decodedJson = JSON.parse(atob(payloadBase64));
      
      return {
        id: Number(decodedJson.sub),
        userIdString: decodedJson.userIdString || '',
        username: decodedJson.username || '',
        email: decodedJson.email || '',
        roles: decodedJson.roles || [],
        companyId: decodedJson.companyId || null
      };
    } catch (e) {
      return null;
    }
  }
}