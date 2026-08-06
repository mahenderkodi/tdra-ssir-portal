import { Routes } from '@angular/router';
import {guestGuard} from './core/auth/guest-guard';
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
import { PortalDashboard as PortalDashboard } from './pages/portal/dashboard/dashboard';
import { Profile } from './pages/portal/profile/profile';
import { SenderIds } from './pages/portal/sender-ids/sender-ids';
import { SenderIdNew } from './pages/portal/sender-id-new/sender-id-new';
import { Users as PortalUsers } from './pages/portal/users/users';
import { TrackStatus } from './pages/portal/track-status/track-status';

// Other pages
import { Unauthorized } from './pages/unauthorized/unauthorized';

export const routes: Routes = [
  // 1. BASE REDIRECT TO SIGN-IN [1]
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'auth/login'
  },

  // 2. PUBLIC AUTHENTICATION ROUTING (Unified Namespace under AuthLayoutComponent) [1, 3]
  {
    path: 'auth',
    component: AuthLayoutComponent,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      {
        path: 'login',
        component: Login,
        title: 'Sign In',
        canActivate: [guestGuard]
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
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        component: AdminDashboardComponent,
        title: 'TDRA Admin Dashboard'
      },
      {
        path: 'registrations',
        component: AdminRegistrationsComponent,
        title: 'Onboarding Queue'
      },
      {
        path: 'registrations/:id',
        component: AdminRegistrationDetailComponent,
        title: 'Inspect Request'
      },
      {
        path: 'users',
        component: AdminUsersComponent,
        canActivate: [roleGuard],
        data: { expectedRoles: ['ROLE_TDRA_SUPER_ADMIN'] },
        title: 'Manage Staff'
      }
    ]
  },

  // 4. SECURE COMPANY PORTAL PAGES [1, 3]
  {
  path: 'portal',
  component: PortalLayout,
  canActivate: [authGuard],

  children: [
    {
      path: 'track-status',
      component: TrackStatus,
      canActivate: [roleGuard],

      data: {
        roles: [
          'ROLE_COMPANY_PENDING'
        ]
      },

      title: 'Application Status'
    },
    {
      path: 'dashboard',
      component: PortalDashboard,
      canActivate: [roleGuard],

      data: {
        roles: [
          'ROLE_COMPANY_ADMIN'
        ]
      },

      title: 'Corporate Dashboard'
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