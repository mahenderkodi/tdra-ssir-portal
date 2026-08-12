import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MockApiService {

 submitRegistration(
  formData: FormData
): Observable<any> {

  console.log(
    'Mock API received FormData'
  );

  const registrationData =
    formData.get('registrationData');

  if (
    !registrationData ||
    typeof registrationData !== 'string'
  ) {
    throw new Error(
      'registrationData not found'
    );
  }

  const payload =
    JSON.parse(registrationData);

  /*
  |--------------------------------------------------------------------------
  | Read existing registrations
  |--------------------------------------------------------------------------
  */
  const existingData =
    localStorage.getItem(
      'mockRegistrations'
    );

  const registrations =
    existingData
      ? JSON.parse(existingData)
      : [];


  /*
  |--------------------------------------------------------------------------
  | Create fake registration record
  |--------------------------------------------------------------------------
  */
  const newRegistration = {

    id:
      registrations.length + 1,

    trackingId:
      `REG-2026-${String(
        registrations.length + 1
      ).padStart(5, '0')}`,

    currentStatus:
      'SUBMITTED',

    submittedAt:
      new Date().toISOString(),

    feedbackComments:
      null,

    company:
      payload.company,

    representative:
      payload.representative,

    account:
      payload.account
  };


  /*
  |--------------------------------------------------------------------------
  | Store it
  |--------------------------------------------------------------------------
  */
  registrations.push(
    newRegistration
  );

  localStorage.setItem(
    'mockRegistrations',
    JSON.stringify(
      registrations
    )
  );


  console.log(
    'Saved mock registrations:',
    registrations
  );


  /*
  |--------------------------------------------------------------------------
  | Fake backend response
  |--------------------------------------------------------------------------
  */
  const response = {

    trackingId:
      newRegistration.trackingId,

    status:
      newRegistration.currentStatus,

    message:
      'Registration submitted successfully.',

    submittedAt:
      newRegistration.submittedAt
  };


  return of(response).pipe(
    delay(500)
  );
}

getRegistrations():
  Observable<any[]> {

  const existingData =
    localStorage.getItem(
      'mockRegistrations'
    );

  const registrations =
    existingData
      ? JSON.parse(existingData)
      : [];

  console.log(
    'Mock GET registrations:',
    registrations
  );

  return of(
    registrations
  ).pipe(
    delay(300)
  );
}

getRegistrationById(
  id: number
): Observable<any> {

  const existingData =
    localStorage.getItem(
      'mockRegistrations'
    );

  const registrations =
    existingData
      ? JSON.parse(existingData)
      : [];

  const registration =
    registrations.find(
      (item: any) =>
        item.id === id
    );

  if (!registration) {

    throw new Error(
      `Registration with ID ${id} not found`
    );
  }

  console.log(
    'Mock registration details:',
    registration
  );

  return of(
    registration
  ).pipe(
    delay(300)
  );
}
}