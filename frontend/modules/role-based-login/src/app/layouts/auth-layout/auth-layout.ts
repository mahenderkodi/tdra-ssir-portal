import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './auth-layout.html', // Pointing to the external HTML file [3]
  styleUrls: ['./auth-layout.css'] // Standardizing style sheets
})
export class AuthLayoutComponent {}