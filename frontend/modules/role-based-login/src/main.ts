/*
 * Application entry point.
 * Bootstraps the standalone Angular application using the root App
 * component and the global providers configured in app.config.ts.
 */


import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig).catch((err) => console.error(err));
