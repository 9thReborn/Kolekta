import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuthResponse {
  token: string;
  role: string;            // 'ADMIN' | 'MERCHANT'
  email: string;
  merchantId: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private base = environment.apiBaseUrl;

  private _user = signal<AuthResponse | null>(this.loadFromStorage());
  user = this._user.asReadonly();
  isLoggedIn = computed(() => this._user() !== null);
  role = computed(() => this._user()?.role ?? null);

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/auth/login`, { email, password })
      .pipe(tap(res => this.store(res)));
  }

  register(email: string, password: string, businessName: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/auth/register`, { email, password, businessName })
      .pipe(tap(res => this.store(res)));
  }

  logout(): void {
    localStorage.removeItem('kolekta_auth');
    this._user.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null { return this._user()?.token ?? null; }

  private store(res: AuthResponse): void {
    localStorage.setItem('kolekta_auth', JSON.stringify(res));
    this._user.set(res);
  }
  private loadFromStorage(): AuthResponse | null {
    const raw = localStorage.getItem('kolekta_auth');
    return raw ? JSON.parse(raw) as AuthResponse : null;
  }
}
