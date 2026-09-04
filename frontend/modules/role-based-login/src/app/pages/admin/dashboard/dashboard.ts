import {
  Component,
  OnInit,
  signal,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  TranslatePipe
} from '@ngx-translate/core';

import {
  AdminRegistrationService
} from '../../../core/services/admin-registration';

import {
  LoggerService
} from '../../../layouts/logging/loggerService';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,

  imports: [
    CommonModule,
    TranslatePipe
  ],

  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class AdminDashboardComponent
  implements OnInit {

  private readonly adminService =
    inject(AdminRegistrationService);

  private readonly logger =
    inject(LoggerService);

  totalCount = signal(0);
  pendingCount = signal(0);
  approvedCount = signal(0);
  rejectedCount = signal(0);
  isLoading = signal(true);


  ngOnInit(): void {

    this.adminService
      .getAllRegistrations()
      .subscribe({

        next: data => {

          this.totalCount.set(
            data.length
          );

          this.pendingCount.set(
            data.filter(
              r =>
                r.currentStatus === 'SUBMITTED' ||
                r.currentStatus === 'INFO_REQUESTED'
            ).length
          );

          this.approvedCount.set(
            data.filter(
              r =>
                r.currentStatus === 'APPROVED'
            ).length
          );

          this.rejectedCount.set(
            data.filter(
              r =>
                r.currentStatus === 'REJECTED'
            ).length
          );

          this.isLoading.set(false);
        },

        error: () => {

          this.logger.error(
            'Unable to load admin dashboard registrations'
          );

          this.isLoading.set(false);
        }

      });
  }
}
