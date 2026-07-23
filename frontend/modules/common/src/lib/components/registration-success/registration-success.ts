import {
  Component
} from '@angular/core';

import {
  DatePipe
} from '@angular/common';

import {
  RouterLink
} from '@angular/router';


interface RegistrationSuccessDetails {
  trackingId: string;
  status: string;
  message: string;
  submittedAt: string;
}


@Component({
  selector: 'lib-registration-success',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe
  ],
  templateUrl: './registration-success.html',
  styleUrl: './registration-success.css'
})
export class RegistrationSuccessComponent {

  /*
   * Read the values passed through router navigation state.
   *
   * history.state may also contain Angular's navigationId,
   * but we only use the four properties we need.
   */
  readonly submissionDetails:
    RegistrationSuccessDetails = {

      trackingId:
        history.state.trackingId ?? '',

      status:
        history.state.status ?? 'SUBMITTED',

      message:
        history.state.message ??
        'Your onboarding application has been successfully submitted.',

      submittedAt:
        history.state.submittedAt ?? ''
    };
}