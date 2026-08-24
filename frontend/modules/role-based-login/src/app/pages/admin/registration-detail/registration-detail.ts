import {
  Component,
  OnInit,
  signal,
  inject,
  Input,
  
} from '@angular/core';

import { CommonModule } from '@angular/common';
import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  SafeResourceUrl
} from '@angular/platform-browser';

import {SafeResourceUrlService} from '../../../core/services/safe-url';

import {
  AdminRegistrationService
} from '../../../core/services/admin-registration';

import { HotToastService } from '@ngxpert/hot-toast';


@Component({
  selector: 'app-admin-registration-detail',
  standalone: true,
  imports: [CommonModule],
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

  @Input() id!: string;


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

      this.errorMessage.set(
        'Missing registration identifier.'
      );

      this.isLoading.set(false);

      return;
    }

    this.loadRegistrationDetails(
      Number(resolvedId)
    );
  }


  previewDocument(doc: any): void {

    this.activeDocument.set(doc);

    /*
     * iframe requires a trusted resource URL.
     * We mainly use this for PDFs.
     */
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


  

isImage(doc: any): boolean {

  const fileName = doc?.fileName;

  if (!fileName) {
    return false;
  }

  return /\.(png|jpg|jpeg|gif|webp)$/i
    .test(fileName);
}


isPdf(doc: any): boolean {

  const fileName = doc?.fileName;

  if (!fileName) {
    return false;
  }

  return /\.pdf$/i.test(fileName);
}

  executeAction(status: string): void {

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


    /*
     * Reject and Request Info
     * require explanation.
     */
    if (
  (
    status === 'REJECTED' ||
    status === 'INFO_REQUESTED'
  ) &&
  !comments
) {

  if (status === 'REJECTED') {
    this.toast.error(
      'Please enter the reason for rejection.'
    );
  }

  if (status === 'INFO_REQUESTED') {
    this.toast.error(
      'Please enter what additional information is required.'
    );
  }

  return;
}


    /*
     * Approval comment is optional.
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

    this.toast.success(
      'Registration approved successfully.'
    );

  } else if (status === 'REJECTED') {

    this.toast.success(
      'Registration rejected successfully.'
    );

  } else if (status === 'INFO_REQUESTED') {

    this.toast.success(
      'Information request sent successfully.'
    );

  }

  void this.router.navigate(
    ['/admin/registrations']
  );
},

        error: err => {

          this.isProcessing.set(false);
          this.activeAction.set(null);

          this.errorMessage.set(
            err.error?.message ||
            'Failed to process request.'
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

          this.isLoading.set(false);

          this.errorMessage.set(
            'Failed to load application details.'
          );
        }

      });
  }
}