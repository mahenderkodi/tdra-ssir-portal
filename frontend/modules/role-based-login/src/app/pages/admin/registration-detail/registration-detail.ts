import {
  Component,
  OnInit,
  signal,
  inject,
  Input,
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  SafeResourceUrl
} from '@angular/platform-browser';

import {
  TranslatePipe,
  TranslateService
} from '@ngx-translate/core';

import {
  SafeResourceUrlService
} from '../../../core/services/safe-url';

import {
  AdminRegistrationService
} from '../../../core/services/admin-registration';

import {
  HotToastService
} from '@ngxpert/hot-toast';

import {
  LoggerService
} from '../../../layouts/logging/loggerService';

@Component({
  selector: 'app-admin-registration-detail',

  standalone: true,

  imports: [
    CommonModule,
    TranslatePipe
  ],

  templateUrl: './registration-detail.html',
  styleUrl: './registration-detail.css'
})
export class AdminRegistrationDetailComponent
  implements OnInit {

  private readonly router =
    inject(Router);

  private readonly route =
    inject(ActivatedRoute);

  private readonly safeUrlService =
    inject(SafeResourceUrlService);

  private readonly adminService =
    inject(AdminRegistrationService);

  private readonly toast =
    inject(HotToastService);

  private readonly logger =
    inject(LoggerService);

  private readonly translate =
    inject(TranslateService);

  @Input()
  id!: string;


  registration =
    signal<any | null>(null);

  activeDocument =
    signal<any | null>(null);

  activePreviewUrl =
    signal<SafeResourceUrl | null>(null);

  isLoading =
    signal(true);

  isProcessing =
    signal(false);

  errorMessage =
    signal('');

  actionComment =
    signal('');

  activeAction =
    signal<string | null>(null);


  ngOnInit(): void {

    const resolvedId =
      this.id ||
      this.route.snapshot.paramMap.get('id');

    if (!resolvedId) {

      this.logger.warn(
        'Registration detail opened without a valid identifier'
      );

      this.errorMessage.set(
        'errors.ADMINDETAIL001'
      );

      this.isLoading.set(false);

      return;
    }

    this.loadRegistrationDetails(
      Number(resolvedId)
    );
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

    const fileName =
      doc?.fileName;

    if (!fileName) {
      return false;
    }

    return /\.(png|jpg|jpeg|gif|webp)$/i
      .test(fileName);
  }


  isPdf(
    doc: any
  ): boolean {

    const fileName =
      doc?.fileName;

    if (!fileName) {
      return false;
    }

    return /\.pdf$/i.test(fileName);
  }


  executeAction(
    status: string
  ): void {

    const resolvedId =
      this.id ||
      this.route.snapshot.paramMap.get('id');

    const regId =
      Number(resolvedId);

    if (!regId) {
      return;
    }

    let comments =
      this.actionComment().trim();


    if (
      (
        status === 'REJECTED' ||
        status === 'INFO_REQUESTED'
      ) &&
      !comments
    ) {

      if (status === 'REJECTED') {

        this.toast.error(
          this.translate.instant(
            'validation.rejectionReasonRequired'
          )
        );
      }

      if (status === 'INFO_REQUESTED') {

        this.toast.error(
          this.translate.instant(
            'validation.additionalInformationRequired'
          )
        );
      }

      return;
    }


    /*
     * This comment is submitted to the backend,
     * so keep its business value unchanged.
     */
    if (
      status === 'APPROVED' &&
      !comments
    ) {

      comments =
        'Application approved by TDRA.';
    }


    this.isProcessing.set(true);
    this.errorMessage.set('');
    this.activeAction.set(status);

    this.adminService
      .updateRegistrationStatus(
        regId,
        status,
        comments
      )
      .subscribe({

        next: () => {

          this.isProcessing.set(false);
          this.activeAction.set(null);

          if (status === 'APPROVED') {

            this.logger.info(
              'Admin registration approved successfully'
            );

            this.toast.success(
              this.translate.instant(
                'admin.registrationDetail.approvedSuccess'
              )
            );

          } else if (
            status === 'REJECTED'
          ) {

            this.logger.info(
              'Admin registration rejected successfully'
            );

            this.toast.success(
              this.translate.instant(
                'admin.registrationDetail.rejectedSuccess'
              )
            );

          } else if (
            status === 'INFO_REQUESTED'
          ) {

            this.logger.info(
              'Admin registration information request sent successfully'
            );

            this.toast.success(
              this.translate.instant(
                'admin.registrationDetail.infoRequestSuccess'
              )
            );
          }

          void this.router.navigate(
            ['/admin/registrations']
          );
        },

        error: err => {

          if (err.status >= 500) {

            this.logger.error(
              'Admin registration action failed due to server error'
            );

          } else {

            this.logger.warn(
              'Admin registration action request rejected'
            );
          }

          this.isProcessing.set(false);
          this.activeAction.set(null);

          this.errorMessage.set(
            'errors.ADMINDETAIL002'
          );
        }

      });
  }


  goBack(): void {

    void this.router.navigate(
      ['/admin/registrations']
    );
  }


  private loadRegistrationDetails(
    id: number
  ): void {

    this.adminService
      .getRegistrationById(id)
      .subscribe({

        next: data => {

          this.registration.set(data);
          this.isLoading.set(false);

          if (
            data.documents &&
            data.documents.length > 0
          ) {

            this.previewDocument(
              data.documents[0]
            );
          }
        },

        error: () => {

          this.logger.error(
            'Unable to load admin registration details'
          );

          this.isLoading.set(false);

          this.errorMessage.set(
            'errors.ADMINDETAIL003'
          );
        }

      });
  }
}
