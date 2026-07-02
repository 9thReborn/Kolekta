import { Routes } from '@angular/router';
import { Merchants } from './features/merchants/merchants';
import { Customers } from './features/customers/customers';
import { CustomerStatement } from './features/customer-statement/customer-statement';

export const routes: Routes = [
  { path: '', redirectTo: 'merchants', pathMatch: 'full' },
  { path: 'merchants', component: Merchants },
  { path: 'merchants/:merchantId/customers', component: Customers },
  { path: 'customers/:customerId/statement', component: CustomerStatement },
];
