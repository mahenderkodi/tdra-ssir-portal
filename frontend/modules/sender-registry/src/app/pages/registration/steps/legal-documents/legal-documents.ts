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
  Input
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

    /*
    | event.target represents the input element that triggered
    | the change event.
    |
    | TypeScript initially sees event.target as EventTarget.
    | We convert it to HTMLInputElement so we can access:
    |
    | input.files
    */
    const input =
      event.target as HTMLInputElement;

    /*
    | input.files contains the files chosen by the user.
    |
    | [0]:
    | Gets the first selected file.
    |
    | ?? null:
    | If no file was selected, store null.
    */
    const file =
      input.files?.[0] ?? null;

    /*
    | Find the correct FormControl using the control name.
    |
    | Example:
    |
    | controlName = 'tradeLicense'
    |
    | This becomes:
    |
    | group.get('tradeLicense')
    |
    | setValue(file):
    | Stores the selected File object in that FormControl.
    */
    this.group
      .get(controlName)
      ?.setValue(file);

    /*
    | Mark the control as touched.
    |
    | This is useful when validation messages are added because Angular
    | can then show errors after the user interacts with the field.
    */
    this.group
      .get(controlName)
      ?.markAsTouched();

    /*
    | Temporary development log.
    |
    | Example console output:
    |
    | tradeLicense: File
    |
    | This can be removed before production.
    */
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

    /*
    | Convert the event target into a file input element.
    */
    const input =
      event.target as HTMLInputElement;

    /*
    | input.files is a FileList object.
    |
    | Array.from converts it into a normal File[] array.
    |
    | If no files were selected, an empty array is created.
    */
    const files =
      Array.from(input.files ?? []);

    /*
    | Store the complete File[] array inside:
    |
    | additionalSupportingDocuments
    */
    this.group
      .get('additionalSupportingDocuments')
      ?.setValue(files);

    /*
    | Mark the control as touched after user interaction.
    */
    this.group
      .get('additionalSupportingDocuments')
      ?.markAsTouched();

    /*
    | Temporary development log.
    |
    | This displays all selected supporting files.
    */
    console.log(
      'Additional documents:',
      files
    );
  }
}