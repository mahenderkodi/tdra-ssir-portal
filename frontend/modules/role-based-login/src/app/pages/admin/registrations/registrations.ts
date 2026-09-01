import { Component, OnInit, signal, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminRegistrationService } from '../../../core/services/admin-registration';
import { HotToastService } from '@ngxpert/hot-toast';
import { timer, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import {
  LoggerService
} from '../../../layouts/logging/loggerService';

@Component({
  selector: 'app-admin-registrations',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './registrations.html',
  styleUrl: './registrations.css'
})
export class AdminRegistrationsComponent implements OnInit, OnDestroy {
  private readonly adminService = inject(AdminRegistrationService);
  private readonly toast = inject(HotToastService);
  // poll subscription may contain subscription or undefined
  // poll subscription is a RxJS subscription that repeatedly asks server
  // for new data at a fixed time interval.
  private pollSubscription?: Subscription;
  private readonly logger =
    inject(LoggerService);

  registrations = signal<any[]>([]);
  isLoading = signal(true);
  actionInProgressId = signal<number | null>(null);

  // Computed signals for top cards
  totalCount = signal(0);
  pendingCount = signal(0);
  approvedCount = signal(0);
  rejectedCount = signal(0);

  ngOnInit(): void {
    // Poll the backend every 15 seconds to keep the admin queue updated in real-time 
    this.pollSubscription = timer(0, 30000).pipe(
      switchMap(() => this.adminService.getAllRegistrations())
    ).subscribe({
      next: (data) => {
        this.registrations.set(data);
        this.calculateMetrics(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.logger.error(
          'Unable to synchronize admin registrations queue'
        );
        this.isLoading.set(false);
        this.toast.error('Failed to synchronize registrations queue.');
      }
    });
  }

  // Calculates metrics dynamically based on active dataset
  calculateMetrics(data: any[]): void {
    this.totalCount.set(data.length);
    this.pendingCount.set(data.filter(r => r.currentStatus === 'SUBMITTED' || r.currentStatus === 'UNDER_REVIEW' || r.currentStatus === 'INFO_REQUESTED').length);
    this.approvedCount.set(data.filter(r => r.currentStatus === 'APPROVED').length);
    this.rejectedCount.set(data.filter(r => r.currentStatus === 'REJECTED').length);
  }

  // Executes inline quick-approvals directly from the table row [3]
  approveCompany(id: number): void {
    this.actionInProgressId.set(id);

    this.adminService.updateRegistrationStatus(id, 'APPROVED', 'Quick approved via Admin Queue Dashboard').subscribe({
      next: (response) => {
        this.logger.info(
          'Admin registration approved successfully'
        );
        this.actionInProgressId.set(null);
        this.toast.success(`Application ${response.trackingId} successfully approved!`);

        // Refresh local memory state instantly without full page reload
        const updatedList = this.registrations().map(r => r.id === id ? response : r);
        this.registrations.set(updatedList);
        this.calculateMetrics(updatedList);
      },
      error: (err) => {

        if (err.status >= 500) {

          this.logger.error(
            'Admin registration approval failed due to server error'
          );

        } else {

          this.logger.warn(
            'Admin registration approval request rejected'
          );
        }

        this.actionInProgressId.set(null);

        this.toast.error(
          err.error?.message ||
          'Failed to approve application.'
        );
      }
    });
  }

  ngOnDestroy(): void {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
    }
  }
}
