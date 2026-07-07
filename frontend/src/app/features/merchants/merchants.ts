import { Component,inject, signal, OnInit } from '@angular/core';
import { AdminApi } from '../../core/admin-api.service';
import { Merchant } from '../../core/models';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-merchants',
  standalone: true,
  imports: [RouterLink,FormsModule],
  templateUrl: './merchants.html',
  styleUrl: './merchants.scss',
})
export class Merchants implements OnInit{
  private api = inject(AdminApi);

  merchants = signal<Merchant[]>([]);      // signals = reactive state (v17+)
  loading = signal(true);
  error = signal<string | null>(null);
  newMerchantName = '';
  creating = signal(false);

  ngOnInit(): void { this.load(); }

  private load(): void {
    this.loading.set(true);
    this.api.getMerchants().subscribe({
      next: (data) => { this.merchants.set(data); this.loading.set(false); },
      error: () => { this.error.set('Failed to load merchants'); this.loading.set(false); },
    });
  }

  delete(event: Event, merchantId: string): void {
    event.stopPropagation();                       // don't trigger the row's navigation
    if (!confirm('Delete this merchant?')) return;
    this.api.deleteMerchant(merchantId).subscribe({
      next: () => this.load(),                      // reload the list
      error: (err) => alert(err.error?.error ?? 'Delete failed'),
    });
  }
  // createMerchant(): void {
  //   const name = this.newMerchantName.trim();
  //   if (!name) return;
  //   this.creating.set(true);
  //   this.api.createMerchant(name).subscribe({
  //     next: () => { this.newMerchantName = ''; this.creating.set(false); this.load(); },
  //     error: () => { this.creating.set(false); alert('Failed to create merchant'); },
  //   });
  // }
}
