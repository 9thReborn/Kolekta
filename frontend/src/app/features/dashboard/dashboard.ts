import { Component,inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminApi } from '../../core/admin-api.service';
import { Overview } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private api = inject(AdminApi);
  overview = signal<Overview | null>(null);

  ngOnInit(): void {
    this.api.getOverview().subscribe(data => this.overview.set(data));
  }
}
