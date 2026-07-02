import { Component,inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe, Location } from '@angular/common';
import { KolektaApi } from '../../core/kolekta-api';
import { Statement } from '../../core/models';

@Component({
  selector: 'app-customer-statement',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './customer-statement.html',
  styleUrl: './customer-statement.scss',
})
export class CustomerStatement implements OnInit  {
  private api = inject(KolektaApi);
  private route = inject(ActivatedRoute);
  private location = inject(Location);

  customerId = this.route.snapshot.paramMap.get('customerId')!;
  statement = signal<Statement | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getStatement(this.customerId).subscribe({
      next: (data) => { this.statement.set(data); this.loading.set(false); },
      error: () => { this.error.set('Failed to load statement'); this.loading.set(false); },
    });
  }
  back(): void { this.location.back(); }
}
