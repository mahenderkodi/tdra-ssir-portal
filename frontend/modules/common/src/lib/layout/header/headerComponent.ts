import { Component } from '@angular/core';
import { Input } from '@angular/core';
import {
  RouterLink
} from '@angular/router';

@Component({
  selector: 'tt-header',
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css',
  standalone: true,
})
export class HeaderComponent {
@Input() signInUrl = '';
@Input() contactUrl = '';
}
