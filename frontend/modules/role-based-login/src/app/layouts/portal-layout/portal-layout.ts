import { Component } from '@angular/core';
import { PortalDashboard } from "../../pages/portal/dashboard/dashboard";
 

@Component({
  selector: 'app-portal-layout',
  imports: [PortalDashboard],
  templateUrl: './portal-layout.html',
  styleUrl: './portal-layout.css',
})
export class PortalLayout {}
