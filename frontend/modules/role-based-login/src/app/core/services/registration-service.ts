
/*
|--------------------------------------------------------------------------
| Angular dependency-injection imports
|--------------------------------------------------------------------------
|
| Injectable:
| Marks this class as an Angular service that can be injected into
| components or other services.
|
| inject:
| Used to obtain an instance of HttpClient without constructor injection.
|
*/
import {
  inject,
  Injectable
} from '@angular/core';


/*
|--------------------------------------------------------------------------
| Angular HTTP client
|--------------------------------------------------------------------------
|
| HttpClient:
| Used to send HTTP requests from Angular to the backend.
|
| In this service, it is used for:
|
| POST → Create a registration
| GET  → Retrieve registrations
|
*/
import {
  HttpClient
} from '@angular/common/http';


/*
|--------------------------------------------------------------------------
| RxJS Observable
|--------------------------------------------------------------------------
|
| Angular HttpClient methods return Observables.
|
| An Observable represents a result that will arrive later,
| after the backend responds.
|
*/
import {
  Observable
} from 'rxjs';


import { RegistrationSubmissionResponseModel } from '../models/registration-response';

import { MockApiService }
  from './mock-api-service';


@Injectable({
  /*
  |--------------------------------------------------------------------------
  | Service availability
  |--------------------------------------------------------------------------
  |
  | providedIn: 'root'
  |
  | Makes one shared instance of RegistrationService available
  | throughout the Angular application.
  |
  | There is no need to manually add this service to a component's
  | providers array.
  |
  */
  providedIn: 'root'
})
export class RegistrationService {

  /*
  |--------------------------------------------------------------------------
  | HttpClient dependency
  |--------------------------------------------------------------------------
  |
  | inject(HttpClient) gives this service access to Angular's
  | HTTP request methods.
  |
  | private:
  | The HttpClient instance is used only inside this service.
  |
  | readonly:
  | The reference will not be replaced after creation.
  |
  */
  private readonly http =
    inject(HttpClient);

  
  private readonly mockApi =
  inject(MockApiService);

  /*
  |--------------------------------------------------------------------------
  | Registration API endpoint
  |--------------------------------------------------------------------------
  |
  | This is the backend URL used by both the POST and GET methods.
  |
  | Current local-development URL:
  |
  | http://localhost:8080/api/v1/registrations
  */
  private readonly apiUrl =
    'http://localhost:8080/api/v1/registrations';

  // API for Sender IDs belonging to the logged-in company - submission.
  private readonly senderIdsApiUrl =
  'http://localhost:8080/api/v1/onboarding-single';

// API to fetch all the sender ids

  private readonly senderIdsApi =
  'http://localhost:8080/api/v1/sender-ids';


  /*
  |--------------------------------------------------------------------------
  | Create a registration
  |--------------------------------------------------------------------------
  |
  | This method sends registration information to the backend.
  |
  | HTTP method:
  | POST
  |
  | Parameter:
  | payload
  |
  | Type:
  | RegistrationRequest
  |
  | Current request format:
  | JSON
  |
  | Current limitation:
  | This method sends normal registration data only.
  | It does not currently upload File objects.
  |
  */
  createRegistration(
    formData: FormData
  ): Observable<RegistrationSubmissionResponseModel> {

    /*
    | http.post sends the payload to the backend API.
    |
    | First argument:
    | The API URL.
    |
    | Second argument:
    | The request body sent to the backend.
    |
    | <unknown>:
    | The exact response structure has not yet been defined.
    */
    return this.http.post<RegistrationSubmissionResponseModel>(
      `${this.senderIdsApiUrl}/submit`,
      formData
    );
  }

//   createRegistration(
//   formData: FormData
// ): Observable<RegistrationSubmissionResponseModel> {

//   return this.mockApi.submitRegistration(
//     formData
//   );
// }

  /*
  |--------------------------------------------------------------------------
  | Retrieve registrations
  |--------------------------------------------------------------------------
  |
  | This method requests registration records from the backend.
  |
  | HTTP method:
  | GET
  |
  | It does not require a request body.
  |
  */
  // Retrieves Sender IDs belonging to the logged-in user's company.
getRegistrations(): Observable<any[]> {

  return this.http.get<any[]>(
    this.senderIdsApi
  );
}

//   getRegistrations():
//   Observable<unknown> {

//   return this.mockApi
//     .getRegistrations();
// }


getMyDraft(): Observable<any> {

  return this.http.get<any>(
    `${this.apiUrl}/my-draft`
  );
}

getSenderIdById(
  id: number
): Observable<unknown> {

   return this.http.get<any>(
    `${this.senderIdsApi}/${id}`
  );
}
}
