import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CustomerSummary, MisdirectedSummary, Statement, MerchantOverview } from './models';

@Injectable({ providedIn: 'root' })
export class UserApi {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;

  getOverview()               { return this.http.get<MerchantOverview>(`${this.base}/api/users/me`); }
  getCustomers()              { return this.http.get<CustomerSummary[]>(`${this.base}/api/users/customers`); }
  onboardCustomer(b: { name: string; email: string; phone: string }) {
    return this.http.post(`${this.base}/api/users/customers`, b);
  }
  closeCustomer(id: string)   { return this.http.delete<void>(`${this.base}/api/users/customers/${id}`); }
  getStatement(id: string)    { return this.http.get<Statement>(`${this.base}/api/users/customers/${id}/statement`); }
  getMisdirected()            { return this.http.get<MisdirectedSummary[]>(`${this.base}/api/users/misdirected-payments`); }
}
