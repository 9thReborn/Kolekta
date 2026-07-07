import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UserApi } from '../../core/user-api.service';
import { MerchantOverview } from '../../core/models';

@Component({
  selector: 'app-merchant-dashboard', standalone: true, imports: [RouterLink],
  templateUrl: './merchant-dashboard.html',
})
export class MerchantDashboard implements OnInit {
  private api = inject(UserApi);
  overview = signal<MerchantOverview | null>(null);
  ngOnInit(): void { this.api.getOverview().subscribe(d => this.overview.set(d)); }
}
