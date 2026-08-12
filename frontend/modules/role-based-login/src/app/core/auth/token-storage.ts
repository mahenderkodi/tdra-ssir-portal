import { Injectable, signal } from '@angular/core';
import { AuthenticatedUser } from './models/authenticated-user-model';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {

  // Refresh token persists across page refreshes and browser tabs.
  private readonly REFRESH_TOKEN_KEY = 'ssir_refresh_token';

  // Access token stays only in Angular memory.
  readonly accessToken = signal<string | null>(null);

  saveAccessToken(token: string): void {
    this.accessToken.set(token);
  }

  getAccessToken(): string | null {
    return this.accessToken();
  }

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

  // Decodes JWT payload to restore logged-in user and roles.
  decodeUserFromToken(token: string): AuthenticatedUser | null {
    try {
      const tokenParts = token.split('.');

      if (tokenParts.length !== 3) {
        return null;
      }

      const normalizedPayload =
        tokenParts[1]
          .replace(/-/g, '+')
          .replace(/_/g, '/');

      const paddedPayload =
        normalizedPayload.padEnd(
          Math.ceil(normalizedPayload.length / 4) * 4,
          '='
        );

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
      console.error(
        '[TokenStorage] Unable to decode JWT:',
        error
      );

      return null;
    }
  }
}