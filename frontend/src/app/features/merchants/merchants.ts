import { Component,inject, signal, OnInit } from '@angular/core';
import { KolektaApi } from '../../core/kolekta-api';
import { Merchant } from '../../core/models';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-merchants',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './merchants.html',
  styleUrl: './merchants.scss',
})
export class Merchants implements OnInit{
  private api = inject(KolektaApi);

  merchants = signal<Merchant[]>([]);      // signals = reactive state (v17+)
  loading = signal(true);
  error = signal<string | null>(null);

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
}
