import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {RegistrationRequest} from '../models/registration-request';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/v1/registrations';

  createRegistration(
    payload: RegistrationRequest
  ): Observable<unknown> {
    return this.http.post<unknown>(
      this.apiUrl,
      payload
    );
  }

  getRegistrations(): Observable<unknown> {
    return this.http.get<unknown>(
      this.apiUrl
    );
  }
}