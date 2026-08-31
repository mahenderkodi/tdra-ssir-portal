//imports below are for using by this ts file.
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';//placeholder where Angular places the page belonging to the current URL.

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html', //Allow this App's HTML template to use RouterOutlet.
  styleUrl: './app.css',
})

//Without @component App is just a typescript class, not an angular component
export class App {
  
}


