import {
  Component,
  Input
} from '@angular/core';

import {
  FormGroup,
  ReactiveFormsModule
} from '@angular/forms';

import {
  TranslatePipe
} from '@ngx-translate/core';

@Component({
  selector: 'app-account-setup',
  standalone: true,

  imports: [
    ReactiveFormsModule,
    TranslatePipe
  ],

  templateUrl: './account-setup.html',
  styleUrl: './account-setup.css'
})
export class AccountSetup {

  @Input({ required: true })
  group!: FormGroup;


  /*
  |--------------------------------------------------------------------------
  | Preferred-language options
  |--------------------------------------------------------------------------
  |
  | value:
  | Sent to the backend.
  |
  | labelKey:
  | Localized only for display.
  |
  */
  readonly languages = [
    {
      value: 'English',
      labelKey:
        'portal.senderIdNew.accountSetup.options.language.english'
    },
    {
      value: 'Arabic',
      labelKey:
        'portal.senderIdNew.accountSetup.options.language.arabic'
    }
  ];


  /*
  |--------------------------------------------------------------------------
  | Time-zone options
  |--------------------------------------------------------------------------
  */
  readonly timeZones = [
    {
      value: 'Asia/Dubai',
      labelKey:
        'portal.senderIdNew.accountSetup.options.timeZone.uae'
    },
    {
      value: 'Asia/Kolkata',
      labelKey:
        'portal.senderIdNew.accountSetup.options.timeZone.india'
    },
    {
      value: 'Europe/London',
      labelKey:
        'portal.senderIdNew.accountSetup.options.timeZone.uk'
    }
  ];


  /*
  |--------------------------------------------------------------------------
  | MFA options
  |--------------------------------------------------------------------------
  */
  readonly mfaPreferences = [
    {
      value: 'Authenticator App',
      labelKey:
        'portal.senderIdNew.accountSetup.options.mfa.authenticatorApp'
    },
    {
      value: 'SMS',
      labelKey:
        'portal.senderIdNew.accountSetup.options.mfa.sms'
    },
    {
      value: 'Email',
      labelKey:
        'portal.senderIdNew.accountSetup.options.mfa.email'
    }
  ];


  /*
  |--------------------------------------------------------------------------
  | Notification options
  |--------------------------------------------------------------------------
  */
  readonly notificationPreferences = [
    {
      value: 'Email',
      labelKey:
        'portal.senderIdNew.accountSetup.options.notification.email'
    },
    {
      value: 'SMS',
      labelKey:
        'portal.senderIdNew.accountSetup.options.notification.sms'
    },
    {
      value: 'Email and SMS',
      labelKey:
        'portal.senderIdNew.accountSetup.options.notification.emailAndSms'
    }
  ];


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