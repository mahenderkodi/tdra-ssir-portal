import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth-guard';
import { roleGuard } from './core/auth/role-guard';

// Layouts
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout';
import { PortalLayout } from './layouts/portal-layout/portal-layout';

// Authentication pages
import { Login } from './pages/auth/login/login';
import { CreatePassword } from './pages/auth/create-password/create-password';

// Admin pages
import { AdminDashboardComponent } from './pages/admin/dashboard/dashboard'; 
import { AdminRegistrationsComponent } from './pages/admin/registrations/registrations';
import { AdminRegistrationDetailComponent } from './pages/admin/registration-detail/registration-detail';
import { AdminUsersComponent } from './pages/admin/users/users';

// Portal pages
import { Dashboard as PortalDashboard } from './pages/portal/dashboard/dashboard';
import { Profile } from './pages/portal/profile/profile';
import { SenderIds } from './pages/portal/sender-ids/sender-ids';
import { SenderIdNew } from './pages/portal/sender-id-new/sender-id-new';
import { Users as PortalUsers } from './pages/portal/users/users';

// Other pages
import { Unauthorized } from './pages/unauthorized/unauthorized';

export const routes: Routes = [
  /*
   * Public authentication pages (Wrapped in AuthLayoutComponent)
   */
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login'
      },
      {
        path: 'login',
        component: Login
      },
      {
        path: 'create-password',
        component: CreatePassword
      }
    ]
  },

  /*
   * TDRA administrator pages
   * Updated: Allowed roles expanded to include all TDRA staff roles [1]
   */
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [
      authGuard,
      roleGuard
    ],
    data: {
      roles: [
        'ROLE_TDRA_SUPER_ADMIN',
        'ROLE_TDRA_REVIEWER',
        'ROLE_TDRA_APPROVER',
        'ROLE_TDRA_AUDITOR'
      ]
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'registrations'
      },
      {
        path: 'dashboard',
        component: AdminDashboardComponent
      },
      {
        path: 'registrations',
        component: AdminRegistrationsComponent
      },
      {
        path: 'registrations/:id',
        component: AdminRegistrationDetailComponent
      },
      {
        path: 'users',
        component: AdminUsersComponent
      }
    ]
  },

  /*
   * Company portal pages
   * Updated: Allowed roles expanded to include all corporate roles [1]
   */
  {
    path: 'portal',
    component: PortalLayout,
    canActivate: [
      authGuard,
      roleGuard
    ],
    data: {
      roles: [
        'ROLE_COMPANY_ADMIN',
        'ROLE_COMPANY_USER',
        'ROLE_COMPANY_VIEWER'
      ]
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      },
      {
        path: 'dashboard',
        component: PortalDashboard
      },
      {
        path: 'profile',
        component: Profile
      },
      {
        path: 'sender-ids',
        component: SenderIds
      },
      {
        path: 'sender-ids/new',
        component: SenderIdNew
      },
      {
        path: 'users',
        component: PortalUsers
      }
    ]
  },

  /*
   * User is logged in but does not have the role.
   */
  {
    path: 'unauthorized',
    component: Unauthorized
  },

  /*
   * Unknown URL
   */
  {
    path: '**',
    redirectTo: 'login'
  }
];