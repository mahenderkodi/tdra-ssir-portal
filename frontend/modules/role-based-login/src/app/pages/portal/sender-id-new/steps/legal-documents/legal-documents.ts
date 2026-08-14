/*
|--------------------------------------------------------------------------
| Angular core imports
|--------------------------------------------------------------------------
|
| Component:
| Used to define this class as an Angular component.
|
| Input:
| Allows this child component to receive the Documents FormGroup
| from its parent Registration component.
|
*/
import {
  Component,
  ElementRef,
  Input,
  QueryList,
  ViewChildren
} from '@angular/core';

/*
|--------------------------------------------------------------------------
| Reactive-form imports
|--------------------------------------------------------------------------
|
| FormGroup:
| Represents the documents section of the registration form.
|
| ReactiveFormsModule:
| Allows legal-documents.html to use reactive-form directives such as:
|
| [formGroup]
|
*/
import {
  FormGroup,
  ReactiveFormsModule
} from '@angular/forms';


@Component({
  /*
  |--------------------------------------------------------------------------
  | Component selector
  |--------------------------------------------------------------------------
  |
  | The parent uses this component as:
  |
  | <app-legal-documents></app-legal-documents>
  |
  */
  selector: 'app-legal-documents',

  /*
  |--------------------------------------------------------------------------
  | Standalone component
  |--------------------------------------------------------------------------
  |
  | This component imports its own required Angular modules instead of
  | depending on an NgModule.
  |
  */
  standalone: true,

  /*
  |--------------------------------------------------------------------------
  | Template dependencies
  |--------------------------------------------------------------------------
  |
  | ReactiveFormsModule is required because the HTML uses:
  |
  | <section [formGroup]="group">
  |
  */
  imports: [
    ReactiveFormsModule
  ],

  /*
  |--------------------------------------------------------------------------
  | Component files
  |--------------------------------------------------------------------------
  */
  templateUrl: './legal-documents.html',
  styleUrl: './legal-documents.css',
})
export class LegalDocuments {

  /*
  |--------------------------------------------------------------------------
  | Documents FormGroup received from the parent
  |--------------------------------------------------------------------------
  |
  | The complete registration form is created inside registration.ts.
  |
  | The parent passes only the documents section:
  |
  | <app-legal-documents
  |   [group]="registrationForm.controls.documents"
  | ></app-legal-documents>
  |
  | This group contains controls such as:
  |
  | tradeLicense
  | certificateOfIncorporation
  | taxRegistrationCertificate
  | authorizedSignatoryLetter
  | signatoryIdentityDocument
  | businessRegistrationCertificate
  | companyLogo
  | additionalSupportingDocuments
  |
  */
  @Input({ required: true })
  group!: FormGroup;

  @ViewChildren('fileInput')
  private readonly fileInputs!: QueryList<
  ElementRef<HTMLInputElement>
>;

/*
|--------------------------------------------------------------------------
| Clear native file-input elements
|--------------------------------------------------------------------------
|
| Resetting the Angular FormControls removes the stored File objects,
| but browsers do not automatically clear the visible native file inputs.
|
*/
clearFileInputs(): void {
  this.fileInputs.forEach(inputReference => {
    inputReference.nativeElement.value = '';
  });
}

@Input()
existingDocuments: any[] = [];

@Input()
isAdditionalSenderId = false;
  /*
  |--------------------------------------------------------------------------
  | Handle a single selected file
  |--------------------------------------------------------------------------
  |
  | This method is used for upload fields that allow only one file.
  |
  | Examples:
  |
  | Trade License
  | Certificate of Incorporation
  | Tax Registration Certificate
  | Company Logo
  |
  | The HTML passes:
  |
  | 1. The file-input change event
  | 2. The name of the matching FormControl
  |
  | Example:
  |
  | (change)="onFileSelected($event, 'tradeLicense')"
  |
  */
 onFileSelected(
  event: Event,
  controlName: string
): void {

  const input =
    event.target as HTMLInputElement;

  const file =
    input.files?.[0] ?? null;

  const control =
    this.group.get(controlName);

  control?.setValue(file);
  control?.markAsTouched();
  control?.markAsDirty();
  control?.updateValueAndValidity();

  console.log(
    `${controlName}:`,
    file
  );
}


  /*
  |--------------------------------------------------------------------------
  | Handle multiple selected files
  |--------------------------------------------------------------------------
  |
  | This method is used only for:
  |
  | Additional Supporting Documents
  |
  | That input uses the HTML `multiple` attribute, so the user can
  | select more than one file.
  |
  */
  onMultipleFilesSelected(
  event: Event
): void {

  const input =
    event.target as HTMLInputElement;

  const files =
    Array.from(input.files ?? []);

  const control =
    this.group.get(
      'additionalSupportingDocuments'
    );

  control?.setValue(files);
  control?.markAsTouched();
  control?.markAsDirty();
  control?.updateValueAndValidity();
}
}