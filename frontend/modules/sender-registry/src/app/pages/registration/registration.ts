/*
|--------------------------------------------------------------------------
| Angular core imports
|--------------------------------------------------------------------------
|
| Component:
| Used to define this TypeScript class as an Angular component.
|
| signal:
| Used for values that can change and automatically update the HTML.
|
| inject:
| Used to obtain Angular services such as FormBuilder and
| RegistrationService without constructor injection.
|
*/
import {
  Component,
  inject,
  signal,
  ViewChild
} from '@angular/core';


import {
  HotToastService
} from '@ngxpert/hot-toast';
/*
|--------------------------------------------------------------------------
| Step components
|--------------------------------------------------------------------------
|
| Registration is the parent component.
|
| Each section of the registration process is maintained as a separate
| child component:
|
| Step 1: CompanyRegistration
| Step 2: LegalDocuments
| Step 3: AuthorizedRepresentative
| Step 4: AccountSetup
|
*/
import {
  CompanyRegistration
} from './steps/company-registration/company-registration';

import {
  LegalDocuments
} from './steps/legal-documents/legal-documents';

import {
  AuthorizedRepresentative
} from './steps/authorized-representative/authorized-representative';

import {
  AccountSetup
} from './steps/account-setup/account-setup';

/*
|--------------------------------------------------------------------------
| Reactive-form imports
|--------------------------------------------------------------------------
|
| FormBuilder:
| Helps us create FormGroup and FormControl objects.
|
| ReactiveFormsModule:
| Makes reactive-form directives such as [formGroup] and formControlName
| available inside the HTML template.
|
| Validators:
| Provides built-in validation rules such as:
| - required
| - email
| - pattern
|
*/
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

/*
|--------------------------------------------------------------------------
| Application imports
|--------------------------------------------------------------------------
|
| RegistrationService:
| Responsible for communicating with the backend registration API.
|
| RegistrationRequest:
| Defines the expected structure of the registration payload.
|
*/
import {
  RegistrationService
} from '../../services/registration-service';

import {
  RegistrationRequest
} from '../../models/registration-request';
import { Router } from '@angular/router';


@Component({
  selector: 'app-registration',

  /*
  |--------------------------------------------------------------------------
  | Standalone component
  |--------------------------------------------------------------------------
  |
  | Because this is a standalone component, it directly imports the
  | components and Angular features required by its HTML template.
  |
  */
  standalone: true,

  /*
  |--------------------------------------------------------------------------
  | Template dependencies
  |--------------------------------------------------------------------------
  |
  | These components and modules are used inside registration.html.
  |
  */
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

  @ViewChild(LegalDocuments)
  private legalDocumentsComponent?: LegalDocuments;
  private readonly formBuilder = inject(FormBuilder);
  private readonly toast =
    inject(HotToastService);
  private readonly router =
    inject(Router);
  private readonly registrationService =
    inject(RegistrationService);



  readonly isSubmitting = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');
  readonly currentStep = signal(1);

  readonly steps = [
    {
      number: 1,
      title: 'Company Registration'
    },
    {
      number: 2,
      title: 'Legal Documents'
    },
    {
      number: 3,
      title: 'Authorized Representative'
    },
    {
      number: 4,
      title: 'Account Setup'
    },
  ];

  readonly registrationForm = this.formBuilder.group({
    company: this.formBuilder.nonNullable.group({
      companyName: [
        '',
        Validators.required
      ],

      legalEntityName: [
        '',
        Validators.required
      ],

      tradeLicenseNumber: [
        '',
        Validators.required
      ],

      registrationNumber: [
        '',
        Validators.required

      ],

      taxId: [
        '',
        Validators.required
      ],

      companyType: [
        '',
        Validators.required
      ],
      industry: [
        '',
        Validators.required
      ],


      dateOfIncorporation: [
        '',
        Validators.required
      ],

      registeredAddress: [
        '',
        Validators.required
      ],

      country: [
        'United Arab Emirates',
        Validators.required
      ],

      emirateState: [
        '',
        Validators.required
      ],

      city: [
        '',
        Validators.required
      ],

      postalCode: [
        '',
        Validators.required
      ],

      website: [
        ''
      ],


      companyEmail: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],


      companyPhone: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^[0-9+\-\s()]{7,20}$/
          )
        ]
      ],

      proposedSenderId : [
        '',
        Validators.required
      ]
    }),

    documents: this.formBuilder.group({

      tradeLicense:
        this.formBuilder.control<File | null>(
          null,
          Validators.required
        ),

      certificateOfIncorporation:
        this.formBuilder.control<File | null>(
          null,
          Validators.required
        ),

      taxRegistrationCertificate:
        this.formBuilder.control<File | null>(
          null,
          Validators.required
        ),

      authorizedSignatoryLetter:
        this.formBuilder.control<File | null>(
          null,
          Validators.required
        ),

      /*
      | Stores either the signatory's Emirates ID or passport.
      */
      signatoryIdentityDocument:
        this.formBuilder.control<File | null>(
          null,
          Validators.required
        ),

      businessRegistrationCertificate:
        this.formBuilder.control<File | null>(
          null,
          Validators.required
        ),

      /*
      | Company logo is optional.
      */
      companyLogo:
        this.formBuilder.control<File | null>(
          null
        ),

      additionalSupportingDocuments:
        this.formBuilder.nonNullable.control<File[]>([]),
    }),



    representative:
      this.formBuilder.nonNullable.group({

        firstName: [
          '',
          Validators.required
        ],

        lastName: [
          '',
          Validators.required
        ],

        designation: [
          '',
          Validators.required
        ],

        department: [
          '',
          Validators.required
        ],

        officialEmail: [
          '',
          [
            Validators.required,
            Validators.email
          ]
        ],

        mobileNumber: [
          '',
          [
            Validators.required,
            Validators.pattern(
              /^[0-9+\-\s()]{7,20}$/
            )
          ]
        ],

        officeNumber: [
          '',
          [
            Validators.required,
            Validators.pattern(
              /^[0-9+\-\s()]{7,20}$/
            )
          ]
        ],

        address: [
          '',
          Validators.required
        ],

        uaePassId: [
          '',
          Validators.required
        ],

        passportOrEmiratesId: [
          '',
          Validators.required
        ]
      }),

    account:
      this.formBuilder.nonNullable.group({

        Username: [
          '',
          [

            Validators.required,
            Validators.minLength(4),
            Validators.maxLength(30),
            Validators.pattern(
              /^[a-zA-Z0-9._-]+$/
            )
          ]
        ],

        PreferredLanguage: [
          '',
          Validators.required
        ],

        TimeZone: [
          '',
          Validators.required
        ],

        MfaPreference: [
          '',
          Validators.required
        ],

        NotificationPreference: [
          '',
          Validators.required
        ]
      }),
  });

  private buildRegistrationPayload():
    RegistrationRequest {

    /*
    | getRawValue returns all current values from the company FormGroup.
    */
    const company =
      this.registrationForm.controls.company.getRawValue();

    const representative =
      this.registrationForm.controls.representative.getRawValue();

    const account =
      this.registrationForm.controls.account.getRawValue();

    return {
      company: {
        companyName:
          company.companyName ?? '',

        legalEntityName:
          company.legalEntityName ?? '',

        tradeLicenseNumber:
          company.tradeLicenseNumber ?? '',

        registrationNumber:
          company.registrationNumber ?? '',

        taxId:
          company.taxId ?? '',

        companyType:
          company.companyType ?? '',

        industry:
          company.industry ?? '',

        dateOfIncorporation:
          company.dateOfIncorporation ?? '',

        registeredAddress:
          company.registeredAddress ?? '',

        country:
          company.country ??
          'United Arab Emirates',

        emirateState:
          company.emirateState ?? '',

        city:
          company.city ?? '',

        postalCode:
          company.postalCode ?? '',

        website:
          company.website ?? '',

        companyEmail:
          company.companyEmail ?? '',

        companyPhone:
          company.companyPhone ?? '',
        proposedSenderId:
          company.proposedSenderId ?? ''
      },

      documents: {},

      representative: {
        firstName: representative.firstName ?? '',
        lastName: representative.lastName ?? '',
        designation: representative.designation ?? '',
        department: representative.department ?? '',
        officialEmail: representative.officialEmail ?? '',
        mobileNumber: representative.mobileNumber ?? '',
        officeNumber: representative.officeNumber ?? '',
        address: representative.address ?? '',
        uaePassId: representative.uaePassId ?? '',
        passportOrEmiratesId: representative.passportOrEmiratesId ?? ''
      },
      account: {
        username: account.Username ?? '',
        preferredLanguage: account.PreferredLanguage ?? '',
        timeZone: account.TimeZone ?? '',
        mfaPreference: account.MfaPreference ?? '',
        notificationPreference: account.NotificationPreference ?? ''

      }
    };
  }

  private buildRegistrationFormData(): FormData {
    const formData = new FormData();

    const payload = this.buildRegistrationPayload();
    //.append(...) add new key-value pair
    formData.append(
      'registrationData',
      JSON.stringify(payload)
    );


    const documents =
      this.registrationForm.controls.documents.getRawValue();


    if (documents.tradeLicense) {
      formData.append(
        'file',
        documents.tradeLicense,
        documents.tradeLicense.name
      );
    }

    if (documents.certificateOfIncorporation) {
      formData.append(
        'file',
        documents.certificateOfIncorporation,
        documents.certificateOfIncorporation.name
      );
    }

    if (documents.taxRegistrationCertificate) {
      formData.append(
        'file',
        documents.taxRegistrationCertificate,
        documents.taxRegistrationCertificate.name
      );
    }

    if (documents.authorizedSignatoryLetter) {
      formData.append(
        'file',
        documents.authorizedSignatoryLetter,
        documents.authorizedSignatoryLetter.name
      );
    }

    if (documents.signatoryIdentityDocument) {
      formData.append(
        'file',
        documents.signatoryIdentityDocument,
        documents.signatoryIdentityDocument.name
      );
    }

    if (documents.businessRegistrationCertificate) {
      formData.append(
        'file',
        documents.businessRegistrationCertificate,
        documents.businessRegistrationCertificate.name
      );
    }

    if (documents.companyLogo) {
      formData.append(
        'file',
        documents.companyLogo,
        documents.companyLogo.name
      );
    }


    documents.additionalSupportingDocuments.forEach(file => {
      formData.append(
        'file',
        file,
        file.name
      );
    });

    return formData;
  }

  private resetDocuments(): void {

    this.registrationForm.controls.documents.reset({
      tradeLicense: null,
      certificateOfIncorporation: null,
      taxRegistrationCertificate: null,
      authorizedSignatoryLetter: null,
      signatoryIdentityDocument: null,
      businessRegistrationCertificate: null,
      companyLogo: null,
      additionalSupportingDocuments: []
    });


    this.legalDocumentsComponent
      ?.clearFileInputs();
  }


  previousStep(): void {

    if (this.currentStep() > 1) {
      this.currentStep.update(
        step => step - 1
      );
    }
  }



  nextStep(): void {


    if (this.currentStep() === 1) {

      const companyGroup =
        this.registrationForm.controls.company;


      companyGroup.markAllAsTouched();

      if (companyGroup.invalid) {
        return;
      }
    }




    if (this.currentStep() === 2) {

      const documentsGroup =
        this.registrationForm.controls.documents;

      documentsGroup.markAllAsTouched();

      if (documentsGroup.invalid) {
        return;
      }
    }

    if (this.currentStep() === 3) {
      const representativeGroup =
        this.registrationForm.controls.representative;

      representativeGroup.markAllAsTouched();


      if (representativeGroup.invalid) {
        return;
      }
    }

    if (this.currentStep() === 4) {
      const representativeGroup =
        this.registrationForm.controls.account;


      representativeGroup.markAllAsTouched();


      if (representativeGroup.invalid) {
        return;
      }
    }



    if (this.currentStep() < 4) {
      this.currentStep.update(
        step => step + 1
      );
    }
  }


  /*
  |--------------------------------------------------------------------------
  | Submit the registration
  |--------------------------------------------------------------------------
  |
  | This method is called by the form's ngSubmit event.
  |
  | Current submission:
  | - Validates Steps
  | - Builds the company JSON payload
  | - Sends the payload to the registration API
  |
  |
  */
  submitRegistration(): void {

    const companyGroup =
      this.registrationForm.controls.company;

    companyGroup.markAllAsTouched();

    if (companyGroup.invalid) {
      this.currentStep.set(1);
      return;
    }

    const documentsGroup =
      this.registrationForm.controls.documents;

    documentsGroup.markAllAsTouched();

    if (documentsGroup.invalid) {
      this.currentStep.set(2);
      return;
    }

    const representativeGroup =
      this.registrationForm.controls.representative;

    representativeGroup.markAllAsTouched();

    if (representativeGroup.invalid) {
      this.currentStep.set(3);
      return;
    }



    const accountGroup =
      this.registrationForm.controls.account;

    accountGroup.markAllAsTouched();

    if (accountGroup.invalid) {
      this.currentStep.set(4);
      return;
    }

    const formData =
      this.buildRegistrationFormData();

    this.isSubmitting.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    formData.forEach((value, key) => {
      console.log(key, value);
    });

    this.registrationService
      .createRegistration(formData)
      .subscribe({

        next: (response) => {
          console.log(
            'Backend response:',
            response
          );

          this.isSubmitting.set(false);

          /*
           * Clear documents only after successful submission.
           */
          this.resetDocuments();

          this.successMessage.set(
            'Registration and documents submitted successfully.'
          );

          this.toast.success(
            'Registration submitted successfully.'
          );


          void this.router.navigate(
            ['/registration-success'],
            {
              state: {
                trackingId:
                  response.trackingId,

                status:
                  response.status,

                message:
                  response.message,

                submittedAt:
                  response.submittedAt,

                username: response.username,
                tempPassword: response.tempPassword
              }
            }
          );

        },

        error: (error) => {
          console.error(
            'Registration API error:',
            error
          );

          this.isSubmitting.set(false);



          /*
           * Do not reset documents here.
           * The user may retry the submission.
           */
          this.errorMessage.set(
            'Registration could not be submitted. Please try again.'
          );

          this.toast.error(
            'Registration could not be submitted. Please try again.'
          );
        }
      });
  }
}