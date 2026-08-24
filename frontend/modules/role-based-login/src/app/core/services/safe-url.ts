import { Injectable, inject } from '@angular/core';

import {
  DomSanitizer,
  SafeResourceUrl
} from '@angular/platform-browser';


@Injectable({
  providedIn: 'root'
})
export class SafeResourceUrlService {

  private readonly sanitizer =
    inject(DomSanitizer);


  getTrustedDocumentUrl(
    url: string
  ): SafeResourceUrl | null {

    try {

      const parsedUrl =
        new URL(url);


      /*
       * Local development
       */
      const isLocalhost =
        parsedUrl.hostname === 'localhost' &&
        parsedUrl.port === '9100';


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
      if (isLocalhost) {

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
      return null;

    } catch {

      /*
       * Invalid URL
       */
      return null;
    }
  }
}