import { Routes } from '@angular/router';
import { guestGuard } from './core/auth/guest-guard';
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
import { Signup } from './pages/auth/signup/signup';

// Admin pages
import { AdminDashboardComponent } from './pages/admin/dashboard/dashboard';
import { AdminRegistrationsComponent } from './pages/admin/registrations/registrations';
import { AdminRegistrationDetailComponent } from './pages/admin/registration-detail/registration-detail';
import { AdminUsersComponent } from './pages/admin/users/users';

// Company portal pages
import { PortalDashboard } from './pages/portal/dashboard/dashboard';
import { SenderIdNew } from './pages/portal/sender-id-new/sender-id-new';
import { SenderIdDetails } from './pages/portal/sender-id-details/sender-id-details';

import { Unauthorized } from './pages/unauthorized/unauthorized';


export const routes: Routes = [

  // Default application entry point
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'auth/login'
  },

  // Authentication pages share the AuthLayout
  {
    path: 'auth',
    component: AuthLayoutComponent,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },

      {
        path: 'login',
        component: Login,
        canActivate: [guestGuard],
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
      },

      {
        path: 'signup',
        component: Signup,
        canActivate: [guestGuard],
        title: 'Sign Up'
      }
    ]
  },

  // TDRA routes require login + an allowed TDRA role
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
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },

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

        // Only Super Admin should access staff management
        data: {
          roles: ['ROLE_TDRA_SUPER_ADMIN']
        },

        title: 'Manage Staff'
      }
    ]
  },

  // Company portal requires authentication; individual pages restrict company roles
  {
    path: 'portal',
    component: PortalLayout,
    canActivate: [authGuard],

    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },

      {
        path: 'dashboard',
        component: PortalDashboard,
        canActivate: [roleGuard],
        data: {
          roles: [
            'ROLE_COMPANY_PENDING',
            'ROLE_COMPANY_ADMIN'
          ]
        },
        title: 'Company Dashboard'
      },

      {
        path: 'sender-id/new',
        component: SenderIdNew,
        canActivate: [roleGuard],
        data: {
          roles: [
            'ROLE_COMPANY_PENDING',
            'ROLE_COMPANY_ADMIN'
          ]
        },
        title: 'Create Sender ID'
      },

      {
        path: 'sender-id/:id',
        component: SenderIdDetails
      }
    ]
  },

  {
    path: 'unauthorized',
    component: Unauthorized,
    title: 'Access Denied'
  },

  // Unknown URLs return to login
  {
    path: '**',
    redirectTo: 'auth/login'
  }
];