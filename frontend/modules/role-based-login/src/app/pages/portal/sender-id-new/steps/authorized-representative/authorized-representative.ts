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
  selector: 'app-authorized-representative',

  standalone: true,

  imports: [
    ReactiveFormsModule,
    TranslatePipe
  ],

  templateUrl: './authorized-representative.html',
  styleUrl: './authorized-representative.css',
})
export class AuthorizedRepresentative {

  @Input()
  group!: FormGroup;
}
