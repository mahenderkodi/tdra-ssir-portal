import { Component, signal, inject } from '@angular/core';
import { CompanyRegistration } from './steps/company-registration/company-registration';
import { LegalDocuments } from './steps/legal-documents/legal-documents';
import { AuthorizedRepresentative } from './steps/authorized-representative/authorized-representative';
import { AccountSetup } from './steps/account-setup/account-setup';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { RegistrationService } from '../../services/registration-service';
import { RegistrationRequest } from '../../models/registration-request'; // Imports the separated interface

@Component({
  selector: 'app-registration',
  standalone: true,
  imports: [
    CompanyRegistration,
    LegalDocuments,
    AuthorizedRepresentative,
    AccountSetup,
    ReactiveFormsModule
  ],
  templateUrl: './registration.html',
  styleUrl: './registration.css',
})
export class Registration {
  private readonly formBuilder = inject(FormBuilder);
  private readonly registrationService = inject(RegistrationService);

  readonly isSubmitting = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  currentStep = signal(1);
  readonly steps = [
    { number: 1, title: 'Company Registration' },
    { number: 2, title: 'Legal Documents' },
    { number: 3, title: 'Authorized Representative' },
    { number: 4, title: 'Account Setup' },
  ];

  readonly registrationForm = this.formBuilder.group({
    company: this.formBuilder.group({
      companyName: ['', Validators.required],
      legalEntityName: ['', Validators.required],
      tradeLicenseNumber: ['', Validators.required],
      registrationNumber: ['', Validators.required],
      taxId: ['', Validators.required],
      companyType: ['', Validators.required],
      industry: ['', Validators.required],
      dateOfIncorporation: ['', Validators.required],
      registeredAddress: ['', Validators.required],
      country: ['United Arab Emirates', Validators.required],
      emirateState: ['', Validators.required],
      city: ['', Validators.required],
      postalCode: ['', Validators.required],
      website: [''],
      companyEmail: ['', [Validators.required, Validators.email]],
      companyPhone: [
        '',
        [
          Validators.required,
          Validators.pattern(/^[0-9+\-\s()]{7,20}$/)
        ]
      ],
    }),
    documents: this.formBuilder.group({}),
    representative: this.formBuilder.group({}),
    account: this.formBuilder.group({}),
  });

  private buildRegistrationPayload(): RegistrationRequest {
    const company = this.registrationForm.controls.company.getRawValue();

    return {
      company: {
        companyName: company.companyName ?? '',
        legalEntityName: company.legalEntityName ?? '',
        tradeLicenseNumber: company.tradeLicenseNumber ?? '',
        registrationNumber: company.registrationNumber ?? '',
        taxId: company.taxId ?? '',
        companyType: company.companyType ?? '',
        industry: company.industry ?? '',
        dateOfIncorporation: company.dateOfIncorporation ?? '',
        registeredAddress: company.registeredAddress ?? '',
        country: company.country ?? 'United Arab Emirates',
        emirateState: company.emirateState ?? '',
        city: company.city ?? '',
        postalCode: company.postalCode ?? '',
        website: company.website ?? '',
        companyEmail: company.companyEmail ?? '',
        companyPhone: company.companyPhone ?? ''
      },
      documents: {},
      representative: {},
      account: {}
    };
  }

  previousStep(): void {
    if (this.currentStep() > 1) {
      this.currentStep.update(step => step - 1);
    }
  }

  nextStep(): void {
    if (this.currentStep() === 1) {
      const companyGroup = this.registrationForm.controls.company;
      companyGroup.markAllAsTouched();

      if (companyGroup.invalid) {
        return;
      }
    }

    if (this.currentStep() < 4) {
      this.currentStep.update(step => step + 1);
    }
  }

  submitRegistration(): void {
    const companyGroup = this.registrationForm.controls.company;
    companyGroup.markAllAsTouched();

    if (companyGroup.invalid) {
      this.currentStep.set(1);
      return;
    }

    const payload = this.buildRegistrationPayload();

    this.isSubmitting.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    console.log('Payload sent to backend:', payload);

    this.registrationService
      .createRegistration(payload)
      .subscribe({
        next: (response) => {
          console.log('Backend response:', response);
          this.isSubmitting.set(false);
          this.successMessage.set(
            'Company registration was submitted successfully.'
          );
        },
        error: (error) => {
          console.error('Registration API error:', error);
          this.isSubmitting.set(false);
          this.errorMessage.set(
            'Registration could not be submitted. Please try again.'
          );
        }
      });
  }
}