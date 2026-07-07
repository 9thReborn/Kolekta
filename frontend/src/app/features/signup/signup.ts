import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './signup.html',
  styleUrl: './signup.scss',
})
export class Signup {
  private auth = inject(AuthService);
  private router = inject(Router);
  businessName = '';
  email = '';
  password = '';
  error = signal<string | null>(null);
  loading = signal(false);

  submit(): void {
    this.loading.set(true);
    this.error.set(null);
    this.auth.register(this.email, this.password, this.businessName).subscribe({
      next: () => this.router.navigate(['/app']),
      error: (e) => {
        this.error.set(e.status === 409 ? 'Email already registered' : 'Signup failed');
        this.loading.set(false);
      },
    });
  }
}
