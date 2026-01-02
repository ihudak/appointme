import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-root',
  imports: [],
  templateUrl: './app.html',
  styleUrl: './app.sass'
})
export class App {
  protected readonly title = signal('Appoint Me');
}
