import { Injectable, inject } from '@angular/core';

//HttpParams - build URL query parameters
import { HttpClient,HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';
 
@Injectable({

  providedIn: 'root'

})

export class AdminRegistrationService {

  private readonly http = inject(HttpClient);

  private readonly REGISTRATION_API = 'http://localhost:8080/api/v1/registrations';

  private readonly LIST_OF_REGISTRATIONS = 'http://localhost:8080/api/v1/onboarding-single';
 
  // 1. Fetch pending registrations list (Admin Queue View) [3]

  getAllRegistrations(): Observable<any[]> {

    return this.http.get<any[]>(this.LIST_OF_REGISTRATIONS);

  }
 
  // 2. Fetch detailed registration by ID 

  getRegistrationById(id: number): Observable<any> {

    return this.http.get<any>(`${this.LIST_OF_REGISTRATIONS}/${id}`);

  }
 
  // 3. Update Status (Execute APPROVE / REJECT / INFO_REQUESTED actions) [3]

updateRegistrationStatus(
  id: number,
  status: string,
  comments: string
): Observable<any> {

  // We are taking status and comments into the function call, and
  // adding them to params
  // final URL - .../status?status=REJECTED&comments=Document%20unclear
  // Deliberately sending empty request body
  const params = new HttpParams()
    .set('status', status)
    .set('comments', comments);

  return this.http.put<any>(
    `${this.REGISTRATION_API}/${id}/status`,
    {},
    { params }
  );
}

}
 