import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import {
  ActivatedRoute,
  RouterLink
} from '@angular/router';

import {
  RegistrationService
} from '../../../core/services/registration-service';

@Component({
  selector: 'app-sender-id-details',

  standalone: true,

  imports: [
    RouterLink
  ],

  templateUrl:
    './sender-id-details.html',

  styleUrl:
    './sender-id-details.css'
})
export class SenderIdDetails
implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly registrationService =
    inject(RegistrationService);


  readonly registration =
    signal<any | null>(null);

  readonly loading =
    signal(true);

  readonly errorMessage =
    signal('');


  ngOnInit(): void {

    const id =
      Number(
        this.route.snapshot.paramMap.get('id')
      );

    console.log(
      'Registration ID from URL:',
      id
    );

    if (!id) {

      this.loading.set(false);

      this.errorMessage.set(
        'Invalid registration ID.'
      );

      return;
    }

    this.loadRegistration(id);
  }


  private loadRegistration(
    id: number
  ): void {

    this.loading.set(true);
    this.errorMessage.set('');

    this.registrationService
      .getRegistrationById(id)
      .subscribe({

        next: response => {

          console.log(
            'REGISTRATION DETAILS:',
            response
          );

          this.registration.set(
            response
          );

          this.loading.set(false);
        },

        error: error => {

          console.error(
            'REGISTRATION DETAILS ERROR:',
            error
          );

          this.loading.set(false);

          this.errorMessage.set(
            'Unable to load registration details.'
          );
        }

      });
  }
}