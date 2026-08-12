import { Injectable, inject } from '@angular/core';

import { HttpClient,HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';
 
@Injectable({

  providedIn: 'root'

})

export class AdminRegistrationService {

  private readonly http = inject(HttpClient);

  private readonly API_URL = 'http://localhost:8080/api/v1/registrations';
 
  // 1. Fetch pending registrations list (Admin Queue View) [3]

  getAllRegistrations(): Observable<any[]> {

    return this.http.get<any[]>(this.API_URL);

  }
 
  // 2. Fetch detailed registration by ID (Inspecting pre-signed MinIO document streams) [3]

  getRegistrationById(id: number): Observable<any> {

    return this.http.get<any>(`${this.API_URL}/${id}`);

  }
 
  // 3. Update Status (Execute APPROVE / REJECT / INFO_REQUESTED actions) [3]

updateRegistrationStatus(
  id: number,
  status: string,
  comments: string
): Observable<any> {

  const params = new HttpParams()
    .set('status', status)
    .set('comments', comments);

  return this.http.put<any>(
    `${this.API_URL}/${id}/status`,
    {},
    { params }
  );
}

}
 