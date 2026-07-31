import { Component, OnInit, signal, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router'; // Imported ActivatedRoute
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AdminRegistrationService } from '../../../core/services/admin-registration';


@Component({
  selector: 'app-admin-registration-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './registration-detail.html',
  styleUrl: './registration-detail.css' 
})
export class AdminRegistrationDetailComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute); // Injected ActivatedRoute [3]
  private sanitizer = inject(DomSanitizer);
  private adminService = inject(AdminRegistrationService);

  @Input() id!: string;

  registration = signal<any | null>(null);
  activePreviewUrl = signal<SafeResourceUrl | null>(null);
  activeDocName = signal<string>('');
  isLoading = signal(true);
  isProcessing = signal(false);
  errorMessage = signal('');

  ngOnInit(): void {
    // SAFE FALLBACK: If Router Component Input binding is not enabled in app.config.ts,
    // we retrieve the ':id' parameter directly from the active route snapshot [3]!
    const resolvedId = this.id || this.route.snapshot.paramMap.get('id');

    if (resolvedId) {
      this.loadRegistrationDetails(Number(resolvedId));
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
    const resolvedId = this.id || this.route.snapshot.paramMap.get('id');
    const regId = Number(resolvedId);
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