import {
  Component,
  OnInit,
  inject,
  signal,
  computed
} from '@angular/core';

import {
  ActivatedRoute,
  RouterLink
} from '@angular/router';

import {
  CommonModule
} from '@angular/common';

import {
  SafeResourceUrl
} from '@angular/platform-browser';

import { SafeResourceUrlService } from '../../../core/services/safe-url';

import {
  RegistrationService
} from '../../../core/services/registration-service';

import {
  LoggerService
} from '../../../layouts/logging/loggerService';

import { TranslatePipe } from '@ngx-translate/core';


@Component({
  selector: 'app-sender-id-details',

  standalone: true,

 imports: [
  CommonModule,
  RouterLink,
  TranslatePipe
],

  templateUrl:
    './sender-id-details.html',

  styleUrl:
    './sender-id-details.css'
})
export class SenderIdDetails
  implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly registrationService =
    inject(RegistrationService);

  private readonly safeUrlService =
    inject(SafeResourceUrlService);

  private readonly logger =
    inject(LoggerService);

  readonly senderId =
    signal<any | null>(null);

  readonly registration =
    signal<any | null>(null);

  readonly activeDocument =
    signal<any | null>(null);

  readonly activePreviewUrl =
    signal<SafeResourceUrl | null>(null);

  readonly loading =
    signal(true);

  readonly errorMessage =
    signal('');


  /*
   * TDRA comment.
   *
   * First try Sender ID remarks.
   * If backend currently stores the comment
   * on Registration, use infoRequestComments.
   */
  readonly tdraRemark =
    computed(() => {

      return (
        this.senderId()?.remarks ||
        this.senderId()?.infoRequestComments ||
        // this.registration()?.infoRequestComments ||
        ''
      );

    });


  ngOnInit(): void {

    const id =
      Number(
        this.route.snapshot
          .paramMap
          .get('id')
      );

    if (!id) {
      this.logger.warn(
        'Sender ID details opened with an invalid identifier'
      );
      this.loading.set(false);

      this.errorMessage.set(
         'errors.SENDER001'
      );

      return;
    }

    this.loadDetails(id);
  }


  private loadDetails(
    id: number
  ): void {

    this.loading.set(true);
    this.errorMessage.set('');


    // 1. Load the selected Sender ID
    this.registrationService
      .getSenderIdById(id)
      .subscribe({

        next: senderResponse => {



          this.senderId.set(
            senderResponse
          );


          // 2. Load company / documents separately
          this.loadRegistrationData();

        },

        error: () => {

          this.logger.error(
            'Unable to load Sender ID details'
          );

          this.loading.set(false);

          this.errorMessage.set(
            'errors.SENDER002'
          );
        }

      });
  }


  private loadRegistrationData(): void {

    this.registrationService
      .getMyDraft()
      .subscribe({

        next: registrationResponse => {


          this.registration.set(
            registrationResponse
          );


          const documents =
            registrationResponse
              ?.documents ?? [];


          if (documents.length > 0) {

            this.previewDocument(
              documents[0]
            );
          }


          this.loading.set(false);
        },

        error: () => {

          this.logger.error(
            'Unable to load Sender ID registration details'
          );

          /*
           * Sender ID has already loaded,
           * so don't destroy the entire page.
           */
          this.loading.set(false);

          this.errorMessage.set(
             'errors.SENDER003'
          );
        }

      });
  }

  previewDocument(
    doc: any
  ): void {

    this.activeDocument.set(doc);

    if (doc?.presignedUrl) {

      this.activePreviewUrl.set(
        this.safeUrlService
          .getTrustedDocumentUrl(
            doc.presignedUrl
          )
      );

    } else {

      this.activePreviewUrl.set(null);
    }
  }


  isImage(
    doc: any
  ): boolean {

    return (
      doc?.contentType
        ?.startsWith('image/')
    );
  }


  isPdf(
    doc: any
  ): boolean {

    return (
      doc?.contentType ===
      'application/pdf'
    );
  }
}