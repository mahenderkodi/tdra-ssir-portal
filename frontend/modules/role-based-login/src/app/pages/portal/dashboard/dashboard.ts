import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import { SenderId } from '../../../core/services/sender-id';
import {SenderIdResponse} from '../../../core/auth/models/sender-id-response-model';

import { DashboardStatsResponse } from '../../../core/services/dashboard-stats-response';
import { RegistrationService }
  from '../../../core/services/registration-service';

import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-portal-dashboard',
  standalone: true,

  imports: [RouterLink],

  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class PortalDashboard
  implements OnInit {

  /*
   * Service responsible for Sender ID APIs.
   */
  private readonly senderIdService =
    inject(SenderId);


  /*
   * Stores the statistics returned by:
   *
   * GET /api/v1/sender-ids/stats
   */
  readonly stats =
    signal<DashboardStatsResponse | null>(
      null
    );


  /*
   * Controls the loading spinner.
   */
  readonly loading =
    signal(true);

  readonly registrations =
  signal<SenderIdResponse[]>([]);

  /*
   * Stores a user-friendly API error.
   */
  readonly errorMessage =
    signal('');


    private readonly registrationService =
  inject(RegistrationService);


 

ngOnInit(): void {
 
  this.loadRegistrations();
}


private loadRegistrations(): void {

  this.loading.set(true);
  this.errorMessage.set('');

  this.registrationService
    .getRegistrations()
    .subscribe({

      next: response => {

        console.log(
          'REGISTRATIONS RESPONSE:',
          response
        );

        this.registrations.set(
          response as any[]
        );

        this.loading.set(false);
      },

      error: error => {

        console.error(
          'REGISTRATIONS ERROR:',
          error
        );

        this.loading.set(false);

        this.errorMessage.set(
          'Unable to load Sender ID registrations.'
        );
      }

    });
}

  /*
   * Fetches the approved company's
   * dashboard statistics.
   */
  private loadDashboardStats(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.senderIdService
      .getDashboardStats()
      .subscribe({
        next: response => {
          this.stats.set(response);
          this.loading.set(false);
        },

        error: error => {
          console.error(
            'Failed to load dashboard statistics:',
            error
          );

          this.loading.set(false);

          this.errorMessage.set(
            error.error?.message ??
            'Unable to load dashboard information.'
          );
        }
      });
  }


  /*
   * Allows the user to retry when the API fails.
   */
  // retry(): void {
  //   this.loadDashboardStats();
  // }

  retry(): void {

  this.loadRegistrations();
}
}