import { Routes } from '@angular/router';

import { PrivacyPolicy } from './pages/privacy-policy/privacy-policy';
import { TermsOfUse } from './pages/terms-of-use/terms-of-use';

export const routes: Routes = [
  
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
];