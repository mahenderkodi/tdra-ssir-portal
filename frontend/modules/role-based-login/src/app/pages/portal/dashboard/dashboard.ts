import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import { SenderId } from '../../../core/services/sender-id';

import { DashboardStatsResponse } from '../../../core/services/dashboard-stats-response';


@Component({
  selector: 'app-portal-dashboard',
  standalone: true,

  imports: [],

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


  /*
   * Stores a user-friendly API error.
   */
  readonly errorMessage =
    signal('');


  /*
   * Runs automatically when the dashboard
   * component is initialized.
   */
  ngOnInit(): void {
  console.log(
    '[PortalDashboard] ngOnInit called'
  );

  this.loadDashboardStats();
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
  retry(): void {
    this.loadDashboardStats();
  }
}