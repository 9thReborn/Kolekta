import { Routes } from '@angular/router';
import { Merchants } from './features/merchants/merchants';
import { Customers } from './features/customers/customers';

export const routes: Routes = [
  { path: '', redirectTo: 'merchants', pathMatch: 'full' },
  { path: 'merchants', component: Merchants },
  { path: 'merchants/:merchantId/customers', component: Customers },
];
