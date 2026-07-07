import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-merchant-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <span class="brand">Kolekta</span>
      <nav>
        <a routerLink="/app/dashboard" routerLinkActive="active">Dashboard</a>
        <a routerLink="/app/customers" routerLinkActive="active">Customers</a>
        <a routerLink="/app/misdirected" routerLinkActive="active">Misdirected</a>
      </nav>
      <span class="spacer"></span>
      <span class="who">{{ email }}</span>
      <button class="logout" (click)="logout()">Log out</button>
    </header>
    <main><router-outlet /></main>
  `,
})
export class MerchantLayout {
  private auth = inject(AuthService);
  email = this.auth.user()?.email;
  logout(): void { this.auth.logout(); }
}
