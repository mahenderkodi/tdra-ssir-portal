/*
|--------------------------------------------------------------------------
| Angular core imports
|--------------------------------------------------------------------------
|
| Component:
| Used to define this class as an Angular component.
|
| Input:
| Allows this child component to receive data from its parent component.
|
*/
import {
  Component,
  Input
} from '@angular/core';

/*
|--------------------------------------------------------------------------
| Angular reactive-form imports
|--------------------------------------------------------------------------
|
| FormGroup:
| Represents the group of company-registration form controls received
| from the parent Registration component.
|
| ReactiveFormsModule:
| Enables reactive-form directives such as:
|
| [formGroup]
| formControlName
|
| inside company-registration.html.
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
  | The parent uses this component in HTML as:
  |
  | <app-company-registration></app-company-registration>
  |
  */
  selector: 'app-company-registration',

  /*
  |--------------------------------------------------------------------------
  | Standalone component
  |--------------------------------------------------------------------------
  |
  | This component does not belong to a traditional Angular NgModule.
  | It directly imports the Angular features required by its template.
  |
  */
  standalone: true,

  /*
  |--------------------------------------------------------------------------
  | Template dependencies
  |--------------------------------------------------------------------------
  |
  | ReactiveFormsModule is required because the component HTML uses:
  |
  | <section [formGroup]="group">
  | <input formControlName="companyName">
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
  templateUrl: './company-registration.html',
  styleUrl: './company-registration.css',
})
export class CompanyRegistration {

  /*
  |--------------------------------------------------------------------------
  | Company FormGroup received from the parent
  |--------------------------------------------------------------------------
  |
  | This child component does not create its own form.
  |
  | The complete form is created inside registration.ts.
  |
  | The parent passes only the company section:
  |
  | <app-company-registration
  |   [group]="registrationForm.controls.company"
  | ></app-company-registration>
  |
  | @Input:
  | Makes the `group` property available for parent binding.
  |
  | required: true:
  | Indicates that the parent must provide this input.
  |
  | group!: FormGroup:
  | Tells TypeScript that Angular will assign the FormGroup after
  | creating the component.
  |
  */
  @Input({ required: true })
  group!: FormGroup;


  /*
  |--------------------------------------------------------------------------
  | Company-type dropdown options
  |--------------------------------------------------------------------------
  |
  | This array supplies the available options for the Company Type
  | dropdown inside company-registration.html.
  |
  | readonly:
  | Prevents the property from being replaced after creation.
  |
  */
  readonly companyTypes = [
    'Private Company',
  'Private Joint Stock',
    'Public Company',
    'Government Entity',
    'Free Zone Company',
    'Partnership',
    'Sole Establishment',
    'Non-Profit Organization',
  ];


  /*
  |--------------------------------------------------------------------------
  | Industry dropdown options
  |--------------------------------------------------------------------------
  |
  | These values are displayed inside the Industry dropdown.
  |
  */
  readonly industries = [
    'Telecommunications',
    'Technology',
    'Financial Services',
    'Healthcare',
    'Retail',
    'Manufacturing',
    'Education',
    'Government',
    'Other',
  ];


  /*
  |--------------------------------------------------------------------------
  | UAE emirate dropdown options
  |--------------------------------------------------------------------------
  |
  | These values are displayed inside the Emirate / State dropdown.
  |
  */
  readonly emirates = [
    'Abu Dhabi',
    'Dubai',
    'Sharjah',
    'Ajman',
    'Umm Al Quwain',
    'Ras Al Khaimah',
    'Fujairah',
  ];
}