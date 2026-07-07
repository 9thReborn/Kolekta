import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Merchant, CustomerSummary, MisdirectedSummary, Statement, Overview } from './models';

@Injectable({ providedIn: 'root' })
export class AdminApi {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;

  getOverview()                 { return this.http.get<Overview>(`${this.base}/api/admin/overview`); }
  getMerchants()                { return this.http.get<Merchant[]>(`${this.base}/api/admin/merchants`); }
  getCustomers(merchantId: string) { return this.http.get<CustomerSummary[]>(`${this.base}/api/admin/merchants/${merchantId}/customers`); }
  getStatement(customerId: string) { return this.http.get<Statement>(`${this.base}/api/admin/customers/${customerId}/statement`); }
  getMisdirected()              { return this.http.get<MisdirectedSummary[]>(`${this.base}/api/admin/misdirected-payments`); }
  deleteMerchant(id: string)    { return this.http.delete<void>(`${this.base}/api/admin/merchants/${id}`); }
}
