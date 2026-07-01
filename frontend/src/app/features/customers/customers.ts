import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { KolektaApi } from '../../core/kolekta-api';
import { CustomerSummary } from '../../core/models';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './customers.html',
  styleUrl: './customers.scss',
})
export class Customers implements OnInit {
  private api = inject(KolektaApi);
  private route = inject(ActivatedRoute);

  // read the :merchantId segment from the URL
  merchantId = this.route.snapshot.paramMap.get('merchantId')!;
  customers = signal<CustomerSummary[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getCustomers(this.merchantId).subscribe({
      next: (data) => { this.customers.set(data); this.loading.set(false); },
      error: () => { this.error.set('Failed to load customers'); this.loading.set(false); },
    });
  }
}
