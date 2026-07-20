import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {HeaderComponent} from "common";
import { Footer } from 'common';
import {TermsOfUse} from "./pages/terms-of-use/terms-of-use";
import {PrivacyPolicy} from "./pages/privacy-policy/privacy-policy";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,HeaderComponent,Footer,TermsOfUse,PrivacyPolicy],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly title = signal('sender-registry');
}
