import {
  Injectable,
  inject
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {TrackingStatusResponse} from './tracking-status-response';


@Injectable({
  providedIn: 'root'
})
export class RegistrationStatusService {

  private readonly http =
    inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/v1/registrations';


  getMyStatus():
    Observable<TrackingStatusResponse> {

    return this.http.get<TrackingStatusResponse>(
      `${this.apiUrl}/my-status`
    );
  }
}