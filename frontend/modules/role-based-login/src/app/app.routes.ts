import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth-guard';
import { roleGuard } from './core/auth/role-guard';

// Layouts
import { AuthLayout } from './layouts/auth-layout/auth-layout';
import { AdminLayout } from './layouts/admin-layout/admin-layout';
import { PortalLayout } from './layouts/portal-layout/portal-layout';

// Authentication pages
import { Login } from './pages/auth/login/login';
import {
  CreatePassword
} from './pages/auth/create-password/create-password';

// Admin pages
import {
  Dashboard as AdminDashboard
} from './pages/admin/dashboard/dashboard';

import {
  Registrations
} from './pages/admin/registrations/registrations';

import {
  RegistrationDetail
} from './pages/admin/registration-detail/registration-detail';

import {
  Users as AdminUsers
} from './pages/admin/users/users';

// Portal pages
import {
  Dashboard as PortalDashboard
} from './pages/portal/dashboard/dashboard';

import {
  Profile
} from './pages/portal/profile/profile';

import {
  SenderIds
} from './pages/portal/sender-ids/sender-ids';

import {
  SenderIdNew
} from './pages/portal/sender-id-new/sender-id-new';

import {
  Users as PortalUsers
} from './pages/portal/users/users';

// Other pages
import {
  Unauthorized
} from './pages/unauthorized/unauthorized';

export const routes: Routes = [
  /*
   * Public authentication pages
   */
  {
    path: '',
    component: AuthLayout,
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
   */
  {
    path: 'admin',
    component: AdminLayout,

    canActivate: [
      authGuard,
      roleGuard
    ],

    data: {
      roles: [
        'ROLE_TDRA_SUPER_ADMIN'
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
        component: AdminDashboard
      },
      {
        path: 'registrations',
        component: Registrations
      },
      {
        path: 'registrations/:id',
        component: RegistrationDetail
      },
      {
        path: 'users',
        component: AdminUsers
      }
    ]
  },

  /*
   * Company portal pages
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
        'ROLE_COMPANY_ADMIN'
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
 