import {
  ApplicationConfig
} from '@angular/core';

import {
  provideRouter
} from '@angular/router';

import {
  provideHttpClient
} from '@angular/common/http';

import {
  provideHotToastConfig
} from '@ngxpert/hot-toast';

import {
  routes
} from './app.routes';


export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),

    provideHttpClient(),

    provideHotToastConfig({
      position: 'top-right',
      duration: 3500,
      dismissible: true
    })
  ]
};