import { Routes } from '@angular/router';

import { PrivacyPolicy } from './pages/privacy-policy/privacy-policy';
import { TermsOfUse } from './pages/terms-of-use/terms-of-use';
import {Registration} from './pages/registration/registration';
export const routes: Routes = [
  {
  path: 'registration',
  component: Registration,
  title: 'Company Registration'
},
  {
    path: 'privacy-policy',
    component: PrivacyPolicy,
    title: 'Privacy Policy',
  },
  {
    path: 'terms-of-use',
    component: TermsOfUse,
    title: 'Terms of Use',
  },
    {
  path: '',
  component: Registration,
  title: 'Company Registration'
}
];