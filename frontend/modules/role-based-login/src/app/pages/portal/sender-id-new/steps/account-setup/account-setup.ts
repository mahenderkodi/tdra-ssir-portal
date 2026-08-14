import {
  Component,
  Input
} from '@angular/core';

import {
  FormGroup,
  ReactiveFormsModule
} from '@angular/forms';

@Component({
  selector: 'app-account-setup',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './account-setup.html',
  styleUrl: './account-setup.css'
})
export class AccountSetup {

  /*
  |--------------------------------------------------------------------------
  | Account FormGroup received from the parent
  |--------------------------------------------------------------------------
  |
  | The parent registration component creates the account FormGroup
  | and passes it to this component.
  |
  */
  @Input({ required: true })
  group!: FormGroup;


  /*
  |--------------------------------------------------------------------------
  | Preferred-language options
  |--------------------------------------------------------------------------
  */
  readonly languages = [
    'English',
    'Arabic'
  ];


  /*
  |--------------------------------------------------------------------------
  | Time-zone options
  |--------------------------------------------------------------------------
  |
  | value:
  | The value submitted to the backend.
  |
  | label:
  | The user-friendly text displayed in the dropdown.
  |
  */
  readonly timeZones = [
    {
      value: 'Asia/Dubai',
      label: 'UAE — Asia/Dubai (UTC+04:00)'
    },
    {
      value: 'Asia/Kolkata',
      label: 'India — Asia/Kolkata (UTC+05:30)'
    },
    {
      value: 'Europe/London',
      label: 'United Kingdom — Europe/London'
    }
  ];


  /*
  |--------------------------------------------------------------------------
  | MFA options
  |--------------------------------------------------------------------------
  */
  readonly mfaPreferences = [
    'Authenticator App',
    'SMS',
    'Email'
  ];


  /*
  |--------------------------------------------------------------------------
  | Notification options
  |--------------------------------------------------------------------------
  */
  readonly notificationPreferences = [
    'Email',
    'SMS',
    'Email and SMS'
  ];


  /*
  |--------------------------------------------------------------------------
  | Validation helper
  |--------------------------------------------------------------------------
  |
  | Returns true when a control is both touched and invalid.
  |
  | This can be used in account-setup.html:
  |
  | [class.is-invalid]="isInvalid('Username')"
  |
  */
  isInvalid(
    controlName: string
  ): boolean {

    const control =
      this.group.get(controlName);

    return Boolean(
      control?.touched &&
      control.invalid
    );
  }
}