import { Component, inject, signal, OnInit  } from '@angular/core';
import { DatePipe } from '@angular/common';
import { UserApi } from '../../core/user-api.service';
import { MisdirectedSummary } from '../../core/models';

@Component({
  selector: 'app-merchant-misdirected',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './merchant-misdirected.html',
})
export class MerchantMisdirected implements OnInit {
  private api = inject(UserApi);
  items = signal<MisdirectedSummary[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getMisdirected().subscribe({
      next: (data) => { this.items.set(data); this.loading.set(false); },
      error: () => { this.error.set('Failed to load queue'); this.loading.set(false); },
    });
  }
}
