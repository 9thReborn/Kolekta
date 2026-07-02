import { Component, inject, signal, OnInit  } from '@angular/core';
import { DatePipe } from '@angular/common';
import { KolektaApi } from '../../core/kolekta-api';
import { MisdirectedSummary } from '../../core/models';

@Component({
  selector: 'app-misdirected',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './misdirected.html',
  styleUrl: './misdirected.scss',
})
export class Misdirected implements OnInit {
  private api = inject(KolektaApi);
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
