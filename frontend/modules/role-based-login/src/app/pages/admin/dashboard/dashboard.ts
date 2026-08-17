import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminRegistrationService } from '../../../core/services/admin-registration';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminRegistrationService);

  totalCount = signal(0);
  pendingCount = signal(0);
  approvedCount = signal(0);
   rejectedCount = signal(0);
  isLoading = signal(true);

  ngOnInit(): void {
    this.adminService.getAllRegistrations().subscribe({
      next: (data) => {
        this.totalCount.set(data.length);
        this.pendingCount.set(data.filter(r => r.currentStatus === 'SUBMITTED' || r.currentStatus === 'INFO_REQUESTED').length);
        this.approvedCount.set(data.filter(r => r.currentStatus === 'APPROVED').length);
        this.rejectedCount.set(data.filter(r => r.currentStatus === 'REJECTED').length); 
        this.isLoading.set(false);
        console.log("registrations data", data);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }
}

