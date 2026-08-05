import { Component } from '@angular/core';
import { PortalDashboard } from "../../pages/portal/dashboard/dashboard";
import { RouterModule } from '@angular/router';
 

@Component({
  selector: 'app-portal-layout',
  imports: [PortalDashboard,RouterModule],
  templateUrl: './portal-layout.html',
  styleUrl: './portal-layout.css',
})
export class PortalLayout {}
