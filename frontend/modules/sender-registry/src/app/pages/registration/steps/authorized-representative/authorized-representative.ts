import { Component, Input } from '@angular/core';
import {
  FormGroup,
  ReactiveFormsModule
} from '@angular/forms';

@Component({
  selector: 'app-authorized-representative',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './authorized-representative.html',
  styleUrl: './authorized-representative.css',
})
export class AuthorizedRepresentative {
  @Input() group!: FormGroup;

}
