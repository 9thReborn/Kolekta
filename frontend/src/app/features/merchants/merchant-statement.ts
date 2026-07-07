import { Component,inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe, Location } from '@angular/common';
import { UserApi } from '../../core/user-api.service';
import { Statement } from '../../core/models';

@Component({
  selector: 'app-merchant-statement',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './merchant-statement.html',
  styleUrl: './merchant-statement.scss',
})
export class MerchantStatement implements OnInit  {
  private api = inject(UserApi);
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
