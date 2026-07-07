import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UserApi } from '../../core/user-api.service';
import { CustomerSummary } from '../../core/models';

@Component({
  selector: 'app-merchant-customers', standalone: true, imports: [RouterLink, FormsModule],
  templateUrl: './merchant-customers.html',
})
export class MerchantCustomers implements OnInit {
  private api = inject(UserApi);
  customers = signal<CustomerSummary[]>([]);
  loading = signal(true);
  newCustomer = { name: '', email: '', phone: '' };
  saving = signal(false);

  ngOnInit(): void { this.load(); }
  private load(): void {
    this.loading.set(true);
    this.api.getCustomers().subscribe({
      next: d => { this.customers.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
  onboard(): void {
    if (!this.newCustomer.name.trim()) return;
    this.saving.set(true);
    this.api.onboardCustomer({
      name: this.newCustomer.name.trim(), email: this.newCustomer.email.trim(), phone: this.newCustomer.phone.trim(),
    }).subscribe({
      next: () => { this.newCustomer = { name: '', email: '', phone: '' }; this.saving.set(false); this.load(); },
      error: (e) => { this.saving.set(false); alert(e.error?.description ?? 'Onboard failed'); },
    });
  }
  close(event: Event, id: string): void {
    event.stopPropagation();
    if (!confirm('Close this customer?')) return;
    this.api.closeCustomer(id).subscribe({ next: () => this.load(), error: () => alert('Close failed') });
  }
}
