import { ApplicationConfig } from '@angular/core';

import { provideRouter } from '@angular/router';

import {
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';

import { routes } from './app.routes';

import { authInterceptor } from './core/auth/auth-interceptor';
import { errorInterceptor } from './core/auth/error-interceptor';

import { provideHotToastConfig } from '@ngxpert/hot-toast';

export const appConfig: ApplicationConfig = {
  providers: [

    // Enables Angular routing using routes defined in app.routes.ts
    provideRouter(routes),

    // Enables HttpClient and applies our authentication/error interceptors to API calls
    provideHttpClient(
      withInterceptors([
        authInterceptor,
        errorInterceptor
      ])
    ),

    // Global toast notification configuration
    provideHotToastConfig()
  ]
};