import { Routes } from '@angular/router';
import { Landing } from './features/landing/landing';
import { Dashboard } from './features/dashboard/dashboard';
import { Merchants } from './features/merchants/merchants';
import { Customers } from './features/customers/customers';
import { CustomerStatement } from './features/customer-statement/customer-statement';
import { Misdirected } from './features/misdirected/misdirected';

export const routes: Routes = [
  { path: '', component: Landing },
  { path: 'dashboard', component: Dashboard },
  { path: 'merchants', component: Merchants },
  { path: 'merchants/:merchantId/customers', component: Customers },
  { path: 'customers/:customerId/statement', component: CustomerStatement },
  { path: 'misdirected', component: Misdirected },
];
