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
import { environment } from '../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class SenderId {

  private readonly http =
    inject(HttpClient);

  private readonly LIST_OF_SENDERIDS =
    `${environment.apiBaseUrl}/sender-ids`;


  getDashboardStats():
    Observable<DashboardStatsResponse> {

    return this.http.get<DashboardStatsResponse>(
      `${this.LIST_OF_SENDERIDS}/stats`
    );
  }
}