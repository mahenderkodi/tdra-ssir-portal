import {
  inject,
  Injectable
} from '@angular/core';
import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import { RegistrationSubmissionResponseModel } from '../models/registration-response';

import { MockApiService }
  from './mock-api-service';


@Injectable({
  providedIn: 'root'
})


export class RegistrationService {
  private readonly http =
    inject(HttpClient);
  
  private readonly mockApi =
  inject(MockApiService);

  
  private readonly REGISTRATION_API =
    'http://localhost:8080/api/v1/registrations';

  // API for Sender IDs belonging to the logged-in company - submission.
  private readonly ONBOARDING_API =
  'http://localhost:8080/api/v1/onboarding-single';

// API to fetch all the sender ids

  private readonly SENDER_IDS_API =
  'http://localhost:8080/api/v1/sender-ids';

 
  createRegistration(
    formData: FormData
  ): Observable<RegistrationSubmissionResponseModel> {

    return this.http.post<RegistrationSubmissionResponseModel>(
      `${this.ONBOARDING_API}/submit`,
      formData
    );
  }

  // Retrieves Sender IDs belonging to the logged-in user's company.
getRegistrations(): Observable<any[]> {

  return this.http.get<any[]>(
    this.SENDER_IDS_API
  );
}



getMyDraft(): Observable<any> {

  return this.http.get<any>(
    `${this.REGISTRATION_API}/my-draft`
  );
}

getSenderIdById(
  id: number
): Observable<unknown> {

   return this.http.get<any>(
    `${this.SENDER_IDS_API}/${id}`
  );
}

resubmitRegistration(
  id: number,
  formData: FormData
) {
  return this.http.put<any>(
    `${this.ONBOARDING_API}/${id}/resubmit`,
    formData
  );
}
}
