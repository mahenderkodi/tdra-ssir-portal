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

import {
  DashboardStatsResponse
} from './dashboard-stats-response';


@Injectable({
  providedIn: 'root'
})
export class SenderId {

  private readonly http =
    inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/v1/sender-ids';


  getDashboardStats():
    Observable<DashboardStatsResponse> {

    return this.http.get<DashboardStatsResponse>(
      `${this.apiUrl}/stats`
    );
  }
}