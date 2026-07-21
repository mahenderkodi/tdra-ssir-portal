import { Component,Input  } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-company-registration',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './company-registration.html',
  styleUrl: './company-registration.css',
})
export class CompanyRegistration {

  //child expects property named group to be passed from parent component, value must be a form group object
  @Input({ required: true })
  group!: FormGroup;

  readonly companyTypes = [
  'Private Company',
  'Public Company',
  'Government Entity',
  'Free Zone Company',
  'Partnership',
  'Sole Establishment',
  'Non-Profit Organization',
];

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