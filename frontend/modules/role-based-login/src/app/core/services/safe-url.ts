import { Injectable, inject } from '@angular/core';

//Using DOM Sanitizer - Angular protects certain HTML bindings 
// from unsafe values.
// SafeResourceUrl - A resource URL that your application has 
// explicitly marked as trusted.
import {
  DomSanitizer,
  SafeResourceUrl
} from '@angular/platform-browser';
import { LoggerService } from '../../layouts/logging/loggerService';


@Injectable({
  providedIn: 'root'
})
export class SafeResourceUrlService {

  private readonly logger = inject(LoggerService);
  private readonly sanitizer =
    inject(DomSanitizer);

  // URL accepted - safeUrl
  // URL rejected - null
  getTrustedDocumentUrl(
    url: string
  ): SafeResourceUrl | null {

    try {
      //URL()  - built in browser url api
      //throws error when a given url is not valid
      const parsedUrl =
        new URL(url);


      /*
       * Local development
       */
      const isAllowedLocalhost =
        parsedUrl.hostname === 'localhost' &&
        parsedUrl.port === '9100' &&
        (
          parsedUrl.protocol === 'http:' ||
          parsedUrl.protocol === 'https:'
        );


      /*
       * Production:
       * replace this with the real
       * document-storage hostname.
       *
       * Example:
       * documents.ssir.gov.ae
       */
      const allowedProductionHosts = [
        'documents.ssir.gov.ae'
      ];


      const isAllowedProductionHost =
        allowedProductionHosts.includes(
          parsedUrl.hostname
        );


      /*
       * localhost may use HTTP during development.
       *
       * Production document URLs must use HTTPS.
       */
      if (isAllowedLocalhost) {

        return this.sanitizer
          .bypassSecurityTrustResourceUrl(
            url
          );
      }


      if (
        parsedUrl.protocol === 'https:' &&
        isAllowedProductionHost
      ) {

        return this.sanitizer
          .bypassSecurityTrustResourceUrl(
            url
          );
      }


      /*
       * Unknown / untrusted URL
       */
      this.logger.warn(
        'Blocked untrusted document URL'
      );
      return null;

    } catch {

      /*
       * Invalid URL
       */
      this.logger.warn(
        'Invalid document URL rejected'
      );
      return null;
    }
  }
}