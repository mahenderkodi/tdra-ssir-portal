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
import { environment } from '../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class RegistrationStatusService {

  private readonly http =
    inject(HttpClient);

  private readonly apiUrl =
    `${environment.apiBaseUrl}/registrations`;


  getMyStatus():
    Observable<TrackingStatusResponse> {

    return this.http.get<TrackingStatusResponse>(
      `${this.apiUrl}/my-status`
    );
  }
}