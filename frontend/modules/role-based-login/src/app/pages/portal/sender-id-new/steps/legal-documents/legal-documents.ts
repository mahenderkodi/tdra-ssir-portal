/*
|--------------------------------------------------------------------------
| Angular core imports
|--------------------------------------------------------------------------
*/
import {
  Component,
  ElementRef,
  Input,
  QueryList,
  ViewChildren
} from '@angular/core';

import {
  FormGroup,
  ReactiveFormsModule
} from '@angular/forms';

import {
  TranslatePipe
} from '@ngx-translate/core';

@Component({
  selector: 'app-legal-documents',
  standalone: true,

  imports: [
    ReactiveFormsModule,
    TranslatePipe
  ],

  templateUrl: './legal-documents.html',
  styleUrl: './legal-documents.css',
})
export class LegalDocuments {

  @Input({ required: true })
  group!: FormGroup;

  @ViewChildren('fileInput')
  private readonly fileInputs!: QueryList<
    ElementRef<HTMLInputElement>
  >;

  @Input()
  existingDocuments: any[] = [];

  @Input()
  isAdditionalSenderId = false;


  clearFileInputs(): void {

    this.fileInputs.forEach(
      inputReference => {

        inputReference
          .nativeElement
          .value = '';
      }
    );
  }


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
  }


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
