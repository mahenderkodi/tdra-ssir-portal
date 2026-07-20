import { Component } from '@angular/core';
import { Input } from '@angular/core';

@Component({
  selector: 'tt-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.css',
  standalone: true,
})
export class HeaderComponent {
@Input() signInUrl = '';
@Input() contactUrl = '';
}
