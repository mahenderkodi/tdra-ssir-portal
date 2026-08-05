import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import {
  DatePipe
} from '@angular/common';

import {
  RegistrationStatusService
} from '../../../core/services/registration-status';

import {
  TrackingStatusResponse
} from '../../../core/services/tracking-status-response';


@Component({
  selector: 'app-track-status',
  standalone: true,

  imports: [
    DatePipe
  ],

  templateUrl: './track-status.html',
  styleUrl: './track-status.css'
})
export class TrackStatus
  implements OnInit {

  private readonly registrationStatusService =
    inject(RegistrationStatusService);


  readonly statusData =
    signal<TrackingStatusResponse | null>(
      null
    );

  readonly loading =
    signal(true);

  readonly errorMessage =
    signal('');


  ngOnInit(): void {
  console.log(
    '[TrackStatus] ngOnInit called'
  );

  this.loadStatus();
}


 private loadStatus(): void {
  console.log(
    '[TrackStatus] Calling my-status API'
  );

  this.loading.set(true);
  this.errorMessage.set('');

  this.registrationStatusService
    .getMyStatus()
    .subscribe({
      next: response => {
        console.log(
          '[TrackStatus] Response:',
          response
        );

        this.statusData.set(response);
        this.loading.set(false);
      },

      error: error => {
        console.error(
          '[TrackStatus] API error:',
          error
        );

        this.loading.set(false);

        this.errorMessage.set(
          error.error?.message ??
          'Unable to load application status.'
        );
      }
    });
}


  getStatusLabel(
    status: string
  ): string {
    switch (status) {
      case 'SUBMITTED':
        return 'Submitted';

      case 'UNDER_REVIEW':
        return 'Under Review';

      case 'INFO_REQUESTED':
        return 'Information Requested';

      case 'APPROVED':
        return 'Approved';

      case 'REJECTED':
        return 'Rejected';

      default:
        return status;
    }
  }


  getStatusClass(
    status: string
  ): string {
    switch (status) {
      case 'APPROVED':
        return 'text-bg-success';

      case 'REJECTED':
        return 'text-bg-danger';

      case 'INFO_REQUESTED':
        return 'text-bg-warning';

      case 'UNDER_REVIEW':
        return 'text-bg-primary';

      default:
        return 'text-bg-secondary';
    }
  }
}