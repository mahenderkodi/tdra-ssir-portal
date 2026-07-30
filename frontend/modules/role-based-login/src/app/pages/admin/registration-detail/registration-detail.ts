import { Component, OnInit, signal, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AdminRegistrationService } from '../../../core/services/admin-registration';

@Component({
  selector: 'app-admin-registration-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './registration-detail.html'
})
export class AdminRegistrationDetailComponent implements OnInit {
  private router = inject(Router);
  private sanitizer = inject(DomSanitizer);
  private adminService = inject(AdminRegistrationService);

  // Maps route parameter ':id' directly to this variable via withComponentInputBinding()
  @Input() id!: string;

  registration = signal<any | null>(null);
  activePreviewUrl = signal<SafeResourceUrl | null>(null);
  activeDocName = signal<string>('');
  isLoading = signal(true);
  isProcessing = signal(false);
  errorMessage = signal('');

  ngOnInit(): void {
    if (this.id) {
      this.loadRegistrationDetails(Number(this.id));
    } else {
      this.errorMessage.set('Missing registration identifier.');
      this.isLoading.set(false);
    }
  }

  // Prepares the secure pre-signed MinIO URL safely for iframe integration [1.1.2]
  previewDocument(doc: any): void {
    this.activeDocName.set(doc.documentType);
    this.activePreviewUrl.set(
      this.sanitizer.bypassSecurityTrustResourceUrl(doc.presignedUrl)
    );
  }

  // Executes state-machine triggers (APPROVE / REJECT / INFO_REQUESTED)
  executeAction(status: string, comments: string): void {
    const regId = Number(this.id);
    if (!regId) return;

    this.isProcessing.set(true);
    this.errorMessage.set('');

    this.adminService.updateRegistrationStatus(regId, status, comments).subscribe({
      next: () => {
        this.isProcessing.set(false);
        this.router.navigate(['/admin/registrations']); // Redirect back to queue
      },
      error: (err) => {
        this.isProcessing.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to process request.');
      }
    });
  }

  private loadRegistrationDetails(id: number): void {
    this.adminService.getRegistrationById(id).subscribe({
      next: (data) => {
        this.registration.set(data);
        this.isLoading.set(false);
        // Automatically load the first document preview in the iframe
        if (data.documents && data.documents.length > 0) {
          this.previewDocument(data.documents[0]);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to load application details.');
      }
    });
  }
}