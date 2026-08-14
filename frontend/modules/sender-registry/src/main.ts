import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// Starts the Angular application using the root App component and global configuration.
bootstrapApplication(App, appConfig).catch((err) => console.error(err));
