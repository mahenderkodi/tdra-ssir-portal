import {
    Injectable,
    isDevMode
} from '@angular/core';

import {environment} from '../../../environments/environment';

/*Enum effectivey gives the following default values
DEBUG = 0
INFO = 1
WARN = 2
ERROR = 3

In Development mode -- the minLevel is set to DEBUG, so all messages will be logged

In Production mode -- the minLevel is set to ERROR, so only error messages will be logged


isDevMode - isDevMode() is a function provided by Angular that checks whether the application is running in development mode or production mode. It returns true if the application is in development mode and false if it is in production mode. This can be useful for enabling or disabling certain features or behaviors based on the environment.
*/

export enum LogLevel {

    DEBUG,

    INFO,

    WARN,

    ERROR

}

//@Injectable - This is a service Angular can manage and inject.
// providedIn: 'root' - Make this service available application-wide.

@Injectable({
    providedIn: 'root'
})
export class LoggerService {
    
    //What's the minimum severity I'm willing to log?
   private minLevel = environment.logLevel;


    debug(message: string) {
        this.log(
            LogLevel.DEBUG,
            message
        );
    }


    info(message: string) {
        this.log(
            LogLevel.INFO,
            message
        );
    }


    warn(message: string) {
        this.log(
            LogLevel.WARN,
            message
        );
    }


    error(message: string) {
        this.log(
            LogLevel.ERROR,
            message
        );
    }


    private log(
        level: LogLevel,
        message: string
    ) {

        if (level < this.minLevel) {
            return;
        }


        const levelName =
            LogLevel[level];


        console.log(
            `[${new Date().toISOString()}] ` +
            `[${levelName}] ${message}`
        );

    }

}