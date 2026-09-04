import {
  Component
} from '@angular/core';

import {
  TranslatePipe
} from '@ngx-translate/core';

@Component({
  selector: 'app-admin-users',

  standalone: true,

  imports: [
    TranslatePipe
  ],

  templateUrl: './users.html'
})
export class AdminUsersComponent {}
