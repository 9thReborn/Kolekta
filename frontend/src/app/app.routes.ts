import { Routes } from '@angular/router';
import { Landing } from './features/landing/landing';
import { Login } from './features/login/login';
import { Signup } from './features/signup/signup';
import { roleGuard } from './core/auth.guard';
import { MerchantLayout } from './features/merchants/merchant-layout';
import { MerchantDashboard } from './features/merchants/merchant-dashboard';
import { MerchantCustomers } from './features/merchants/merchant-customers';
import { MerchantStatement } from './features/merchants/merchant-statement';
import { MerchantMisdirected } from './features/merchants/merchant-misdirected';
import { AdminLayout } from './features/admin/admin-layout';
import { Dashboard } from './features/dashboard/dashboard';
import { Merchants } from './features/merchants/merchants';
import { Customers } from './features/customers/customers';
import { CustomerStatement } from './features/customer-statement/customer-statement';
import { Misdirected } from './features/misdirected/misdirected';

export const routes: Routes = [
  { path: '', component: Landing },
  { path: 'login', component: Login },
  { path: 'signup', component: Signup },
  {
    path: 'app',
    component: MerchantLayout,
    canActivate: [roleGuard('MERCHANT')],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: MerchantDashboard },
      { path: 'customers', component: MerchantCustomers },
      { path: 'customers/:customerId/statement', component: MerchantStatement },
      { path: 'misdirected', component: MerchantMisdirected },
    ],
  },
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [roleGuard('ADMIN')],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: Dashboard },
      { path: 'merchants', component: Merchants },
      { path: 'merchants/:merchantId/customers', component: Customers },
      { path: 'customers/:customerId/statement', component: CustomerStatement },
      { path: 'misdirected', component: Misdirected },
    ],
  },
];
