import { ApplicationConfig } from '@angular/core'; //Type or interface to describe configuration

import { provideRouter } from '@angular/router'; // To enable angular routing

import {
  provideHttpClient,
  withInterceptors
} from '@angular/common/http'; // enable http client to make http requests i.e., api calls, also allows us to add interceptors to modify requests and responses globally

import { routes } from './app.routes';

import { authInterceptor } from './core/auth/auth-interceptor';
import { errorInterceptor } from './core/auth/error-interceptor';

import { provideHotToastConfig } from '@ngxpert/hot-toast';//This is a third-party library that provides a simple way to show toast notifications in Angular applications.
import {
  provideTranslateService
} from '@ngx-translate/core';

import {
  provideTranslateHttpLoader
} from '@ngx-translate/http-loader';



export const appConfig: ApplicationConfig = {
  //providers - This is an array of providers. Tells Angular's Dependency Injection system - something/capability that should be available to the application.

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
    provideHotToastConfig(),
    provideTranslateService({

      loader: provideTranslateHttpLoader({
        prefix: '/i18n/',
        suffix: '.json'
      }),

      fallbackLang: 'en-US',

      lang: 'en-US'
    })
  ]
};