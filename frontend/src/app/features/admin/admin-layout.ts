import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <span class="brand">Kolekta <small style="opacity:.6">admin</small></span>
      <nav>
        <a routerLink="/admin/dashboard" routerLinkActive="active">Overview</a>
        <a routerLink="/admin/merchants" routerLinkActive="active">Merchants</a>
        <a routerLink="/admin/misdirected" routerLinkActive="active">Misdirected</a>
      </nav>
      <span class="spacer"></span>
      <span class="who">{{ email }}</span>
      <button class="logout" (click)="logout()">Log out</button>
    </header>
    <main><router-outlet /></main>
  `,
})
export class AdminLayout {
  private auth = inject(AuthService);
  email = this.auth.user()?.email;
  logout(): void { this.auth.logout(); }
}
