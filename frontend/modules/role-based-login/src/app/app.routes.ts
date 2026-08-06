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
import { ForgotPassword } from './pages/auth/forgot-password/forgot-password';
import { ResetPassword } from './pages/auth/reset-password/reset-password';

// Admin pages
import { AdminDashboardComponent } from './pages/admin/dashboard/dashboard'; 
import { AdminRegistrationsComponent } from './pages/admin/registrations/registrations';
import { AdminRegistrationDetailComponent } from './pages/admin/registration-detail/registration-detail';
import { AdminUsersComponent } from './pages/admin/users/users';

// Portal pages

import { Profile } from './pages/portal/profile/profile';
import { SenderIds } from './pages/portal/sender-ids/sender-ids';
import { SenderIdNew } from './pages/portal/sender-id-new/sender-id-new';
import { Users as PortalUsers } from './pages/portal/users/users';

// Other pages
import { Unauthorized } from './pages/unauthorized/unauthorized';

export const routes: Routes = [
  // 1. BASE REDIRECT TO SIGN-IN [1]
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'auth/login'
  },

  // 2. PUBLIC AUTHENTICATION ROUTING (Unified Namespace) [1]
  {
    path: 'auth',
    component: AuthLayoutComponent,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      {
        path: 'login',
        component: Login,
        title: 'Sign In'
      },
      {
        path: 'create-password',
        component: CreatePassword,
        title: 'Setup Password'
      },
      {
        path: 'forgot-password',
        component: ForgotPassword,
        title: 'Forgot Password'
      },
      {
        path: 'reset-password',
        component: ResetPassword,
        title: 'Reset Password'
      }
    ]
  },

  // 3. SECURE TDRA ADMINISTRATOR PORTAL [1, 3]
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, roleGuard],
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

  // 4. SECURE COMPANY PORTAL PAGES (Separated status tracking and dashboards) [1, 3]
  {
    path: 'portal',
    component: PortalLayout,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: [
        'ROLE_COMPANY_PENDING', // Allows pending users to enter the portal layout [1]
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
      // ROUTE A: Secure Status Tracking Page (Restricted only to Pending Applicants) [1]
      {
        path: 'track-status',
       // loadComponent: () => import('./pages/portal/track-status/track-status.component').then(m => m.TrackStatusComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_COMPANY_PENDING'] },
        title: 'Track Status'
      },
      // ROUTE B: Standard Corporate Dashboard (Restricted to Approved Companies) [1]
      {
        path: 'dashboard',
       // component: PortalDashboard,
        canActivate: [roleGuard],
        data: { roles: ['ROLE_COMPANY_ADMIN', 'ROLE_COMPANY_USER', 'ROLE_COMPANY_VIEWER'] },
        title: 'Company Dashboard'
      },
      {
        path: 'profile',
        component: Profile,
        canActivate: [roleGuard],
        data: { roles: ['ROLE_COMPANY_ADMIN', 'ROLE_COMPANY_USER', 'ROLE_COMPANY_VIEWER'] }
      },
      {
        path: 'sender-ids',
        component: SenderIds,
        canActivate: [roleGuard],
        data: { roles: ['ROLE_COMPANY_ADMIN', 'ROLE_COMPANY_USER', 'ROLE_COMPANY_VIEWER'] }
      },
      {
        path: 'sender-ids/new',
        component: SenderIdNew,
        canActivate: [roleGuard],
        data: { roles: ['ROLE_COMPANY_ADMIN', 'ROLE_COMPANY_USER'] }
      },
      {
        path: 'users',
        component: PortalUsers,
        canActivate: [roleGuard],
        data: { roles: ['ROLE_COMPANY_ADMIN'] } // Restricted to Company Admin only [1]
      }
    ]
  },

  // 5. UN-AUTHORIZED ACCESS VIEW
  {
    path: 'unauthorized',
    component: Unauthorized,
    title: 'Access Denied'
  },

  // 6. DEFAULT FALLBACK
  {
    path: '**',
    redirectTo: 'auth/login'
  }
];