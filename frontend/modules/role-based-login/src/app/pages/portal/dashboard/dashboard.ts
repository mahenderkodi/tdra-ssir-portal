import {
  Component,
  computed,
  signal
} from '@angular/core';

import {
  DatePipe
} from '@angular/common';

import {
  FormControl,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';


type ApplicationStatus =
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'INFO_REQUESTED'
  | 'APPROVED'
  | 'REJECTED';


interface PortalApplication {
  trackingId: string;
  companyName: string;
  applicationType: string;
  status: ApplicationStatus;
  submittedAt: string;
  lastUpdatedAt: string;
  assignedDepartment: string;
  statusMessage: string;
}


@Component({
  selector: 'app-portal-dashboard',
  standalone: true,

  imports: [
    ReactiveFormsModule,
    DatePipe
  ],

  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class PortalDashboard {

  /*
   * Temporary mock data.
   *
   * Later replace this with an API response.
   */
  readonly applications =
    signal<PortalApplication[]>([
      {
        trackingId:
          'REG-2026-E60F0BDD',

        companyName:
          'Elevate Core Technologies',

        applicationType:
          'Company Onboarding',

        status:
          'UNDER_REVIEW',

        submittedAt:
          '2026-07-30T19:27:02.7755373',

        lastUpdatedAt:
          '2026-07-31T10:30:00',

        assignedDepartment:
          'TDRA Registration Team',

        statusMessage:
          'Your application and submitted documents are currently under review.'
      },

      {
        trackingId:
          'REG-2026-B17A92C4',

        companyName:
          'Elevate Core Technologies',

        applicationType:
          'Sender ID Registration',

        status:
          'INFO_REQUESTED',

        submittedAt:
          '2026-07-25T11:15:00',

        lastUpdatedAt:
          '2026-07-29T15:45:00',

        assignedDepartment:
          'Compliance Review Team',

        statusMessage:
          'Additional information is required before the application can proceed.'
      },

      {
        trackingId:
          'REG-2026-A41D802E',

        companyName:
          'Elevate Core Technologies',

        applicationType:
          'Sender ID Registration',

        status:
          'APPROVED',

        submittedAt:
          '2026-07-18T09:40:00',

        lastUpdatedAt:
          '2026-07-23T12:20:00',

        assignedDepartment:
          'TDRA Registration Team',

        statusMessage:
          'Your application has been approved successfully.'
      }
    ]);


  readonly trackingNumberControl =
    new FormControl(
      '',
      {
        nonNullable: true,
        validators: [
          Validators.required
        ]
      }
    );


  readonly selectedApplication =
    signal<PortalApplication | null>(null);


  readonly trackingMessage =
    signal('');


  readonly trackingError =
    signal(false);


  readonly totalApplications =
    computed(
      () => this.applications().length
    );


  readonly underReviewCount =
    computed(
      () =>
        this.applications().filter(
          application =>
            application.status ===
              'UNDER_REVIEW' ||
            application.status ===
              'SUBMITTED'
        ).length
    );


  readonly actionRequiredCount =
    computed(
      () =>
        this.applications().filter(
          application =>
            application.status ===
            'INFO_REQUESTED'
        ).length
    );


  readonly approvedCount =
    computed(
      () =>
        this.applications().filter(
          application =>
            application.status ===
            'APPROVED'
        ).length
    );


  trackApplication(): void {
    this.trackingMessage.set('');
    this.trackingError.set(false);
    this.selectedApplication.set(null);

    if (
      this.trackingNumberControl.invalid
    ) {
      this.trackingNumberControl
        .markAsTouched();

      this.trackingMessage.set(
        'Please enter a tracking number.'
      );

      this.trackingError.set(true);

      return;
    }

    const trackingNumber =
      this.trackingNumberControl.value
        .trim()
        .toUpperCase();


    const application =
      this.applications().find(
        item =>
          item.trackingId.toUpperCase() ===
          trackingNumber
      );


    if (!application) {
      this.trackingMessage.set(
        'No application was found for this tracking number.'
      );

      this.trackingError.set(true);

      return;
    }


    this.selectedApplication.set(
      application
    );

    this.trackingMessage.set(
      'Application found successfully.'
    );
  }


  viewApplication(
    application: PortalApplication
  ): void {
    this.selectedApplication.set(
      application
    );

    this.trackingMessage.set('');
    this.trackingError.set(false);

    /*
     * Scroll to the details section.
     */
    setTimeout(() => {
      document
        .getElementById(
          'application-details'
        )
        ?.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
    });
  }


  clearTracking(): void {
    this.trackingNumberControl.reset();
    this.selectedApplication.set(null);
    this.trackingMessage.set('');
    this.trackingError.set(false);
  }


  getStatusLabel(
    status: ApplicationStatus
  ): string {
    switch (status) {
      case 'SUBMITTED':
        return 'Submitted';

      case 'UNDER_REVIEW':
        return 'Under Review';

      case 'INFO_REQUESTED':
        return 'Information Required';

      case 'APPROVED':
        return 'Approved';

      case 'REJECTED':
        return 'Rejected';

      default:
        return status;
    }
  }


  getStatusBadgeClass(
    status: ApplicationStatus
  ): string {
    switch (status) {
      case 'SUBMITTED':
        return 'text-bg-secondary';

      case 'UNDER_REVIEW':
        return 'text-bg-warning';

      case 'INFO_REQUESTED':
        return 'text-bg-danger';

      case 'APPROVED':
        return 'text-bg-success';

      case 'REJECTED':
        return 'text-bg-dark';

      default:
        return 'text-bg-secondary';
    }
  }
}