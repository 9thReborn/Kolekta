import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminApi } from '../../core/admin-api.service';
import { CustomerSummary } from '../../core/models';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './customers.html',
  styleUrl: './customers.scss',
})
export class Customers implements OnInit {
  private api = inject(AdminApi);
  private route = inject(ActivatedRoute);

  merchantId = this.route.snapshot.paramMap.get('merchantId')!;
  customers = signal<CustomerSummary[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  newCustomer = { name: '', email: '', phone: '' };
  saving = signal(false);

  ngOnInit(): void { this.load(); }

  private load(): void {
    this.loading.set(true);
    this.api.getCustomers(this.merchantId).subscribe({
      next: (data) => { this.customers.set(data); this.loading.set(false); },
      error: () => { this.error.set('Failed to load customers'); this.loading.set(false); },
    });
  }

  // onboard(): void {
  //   if (!this.newCustomer.name.trim()) return;
  //   this.saving.set(true);
  //   this.api.onboardCustomer(this.merchantId, {
  //     name: this.newCustomer.name.trim(),
  //     email: this.newCustomer.email.trim(),
  //     phone: this.newCustomer.phone.trim(),
  //   }).subscribe({
  //     next: () => { this.newCustomer = { name: '', email: '', phone: '' }; this.saving.set(false); this.load(); },
  //     error: () => { this.saving.set(false); alert('Failed to onboard (sandbox may be at its 2-account limit)'); },
  //   });
  // }
}
