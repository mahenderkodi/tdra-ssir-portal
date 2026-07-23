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
  /*
  |--------------------------------------------------------------------------
  | Dependency injection
  |--------------------------------------------------------------------------
  |
  | formBuilder:
  | Used to create the complete registration reactive form.
  |
  | registrationService:
  | Used to send the registration request to the backend.
  |
  | private:
  | These properties are only used inside this TypeScript class.
  |
  | readonly:
  | The service references should not be replaced after creation.
  |
  */
  private readonly formBuilder = inject(FormBuilder);

  private readonly toast =
  inject(HotToastService);
  private readonly router =
  inject(Router);

  private readonly registrationService =
    inject(RegistrationService);


  /*
  |--------------------------------------------------------------------------
  | Submission-state signals
  |--------------------------------------------------------------------------
  |
  | Signals are values that Angular can track.
  |
  | In HTML, signals are read using parentheses:
  |
  | isSubmitting()
  | successMessage()
  | errorMessage()
  |
  */

  /*
  | true  = API submission is in progress
  | false = no API submission is in progress
  */
  readonly isSubmitting = signal(false);

  /*
  | Stores the success message returned after a successful submission.
  */
  readonly successMessage = signal('');

  /*
  | Stores the message shown when the API request fails.
  */
  readonly errorMessage = signal('');


  /*
  |--------------------------------------------------------------------------
  | Registration-step state
  |--------------------------------------------------------------------------
  |
  | currentStep controls which child component is displayed.
  |
  | 1 = Company Registration
  | 2 = Legal Documents
  | 3 = Authorized Representative
  | 4 = Account Setup
  |
  */
  readonly currentStep = signal(1);

  /*
  | Used by the HTML stepper to display the step number and title.
  */
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


  /*
  |--------------------------------------------------------------------------
  | Main registration form
  |--------------------------------------------------------------------------
  |
  | This is the parent FormGroup for the complete four-step form.
  |
  | registrationForm
  | ├── company
  | ├── documents
  | ├── representative
  | └── account
  |
  | Each child component receives only the FormGroup belonging to its step.
  |
  */
  readonly registrationForm = this.formBuilder.group({

    /*
    |--------------------------------------------------------------------------
    | Step 1: Company form group
    |--------------------------------------------------------------------------
    |
    | nonNullable.group is used because company text fields should contain
    | strings instead of string | null.
    |
    | IMPORTANT:
    | The values below are temporary test values.
    |
    | Before pushing production-ready code:
    | 1. Replace test values with empty strings.
    | 2. Uncomment Validators.required.
    |
    */
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
        'Artificial Intelligence',
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
    }),


    /*
    |--------------------------------------------------------------------------
    | Step 2: Legal-documents form group
    |--------------------------------------------------------------------------
    |
    | Each single-document control stores either:
    |
    | File  = a file has been selected
    | null  = no file has been selected
    |
    | Validators.required is temporarily disabled for testing.
    |
    */
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


  /*
  |--------------------------------------------------------------------------
  | Build the backend registration payload
  |--------------------------------------------------------------------------
  |
  | This method converts the Angular reactive-form values into the
  | RegistrationRequest format expected by RegistrationService.
  |
  | private:
  | This method is used only inside this component.
  |
  */
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

    /*
    | Return an object that follows RegistrationRequest.
    */
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
          company.companyPhone ?? ''
      },

      /*
      | IMPORTANT:
      |
      | Documents are currently not added to this payload.
      |
      | Selected files exist in:
      |
      | registrationForm.controls.documents
      |
      | But this method currently sends an empty documents object.
      |
      | File-upload integration will be implemented separately.
      */
      documents: {},

      /*
      | These remain empty until Steps 3 and 4 are developed.
      */
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

    /*
     * First multipart request part:
     *
     * Backend:
     * @RequestPart("registrationData")
     */
    const payload = this.buildRegistrationPayload();

    formData.append(
      'registrationData',
      JSON.stringify(payload)
    );

    /*
     * Read all selected documents from Step 2.
     */
    const documents =
      this.registrationForm.controls.documents.getRawValue();

    /*
     * Second multipart request-part name:
     *
     * Backend:
     * @RequestPart("file")
     *
     * The same key is added once for every selected file.
     */
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

    /*
     * This control is non-nullable, so its value is always File[].
     */
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
  /*
   * Clear all File objects stored in the reactive form.
   */
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

  /*
   * Clear the visible native file-input values.
   *
   * The component may not exist when another registration step is open,
   * so optional chaining is used.
   */
  this.legalDocumentsComponent
    ?.clearFileInputs();
}

  /*
  |--------------------------------------------------------------------------
  | Move to the previous step
  |--------------------------------------------------------------------------
  |
  | The step number cannot go below 1.
  |
  */
  previousStep(): void {

    if (this.currentStep() > 1) {
      this.currentStep.update(
        step => step - 1
      );
    }
  }


  /*
  |--------------------------------------------------------------------------
  | Move to the next step
  |--------------------------------------------------------------------------
  |
  | Before leaving Step 1:
  |
  | 1. Mark every company control as touched.
  | 2. Check whether the company group is invalid.
  | 3. Stop navigation if validation fails.
  |
  */
  nextStep(): void {

    /*
    | Step 1 validation
    */
    if (this.currentStep() === 1) {

      const companyGroup =
        this.registrationForm.controls.company;

      /*
      | Marking controls as touched allows validation messages
      | to appear in the child component.
      */
      companyGroup.markAllAsTouched();

      /*
      | Stop here when any company control is invalid.
      */
      if (companyGroup.invalid) {
        return;
      }
    }

    /*
    | Step 2 validation is not added yet.
    |
    | Later, this section can check whether all mandatory documents
    | have been selected before allowing navigation to Step 3.
    */

     if (this.currentStep() === 3){
      const representativeGroup =
        this.registrationForm.controls.representative;

      /*
      | Marking controls as touched allows validation messages
      | to appear in the child component.
      */
      representativeGroup.markAllAsTouched();

      /*
      | Stop here when any representative control is invalid.
      */
      if (representativeGroup.invalid) {
        return;
      }
     }

     if (this.currentStep() === 4){
      const representativeGroup =
        this.registrationForm.controls.account;

    
      representativeGroup.markAllAsTouched();

      
      if (representativeGroup.invalid) {
        return;
      }
     }

      
    

    /*
    | Increase the step only when it is below Step 4.
    */
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
  | - Validates Step 1
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
              response.submittedAt
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