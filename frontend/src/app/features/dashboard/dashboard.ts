import { Component,inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { KolektaApi } from '../../core/kolekta-api';
import { Overview } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private api = inject(KolektaApi);
  overview = signal<Overview | null>(null);

  ngOnInit(): void {
    this.api.getOverview().subscribe(data => this.overview.set(data));
  }
}
