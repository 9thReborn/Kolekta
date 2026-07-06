import { HttpClient } from '@angular/common/http';
import { Injectable,inject } from '@angular/core';
import { Observable } from 'rxjs';
import {Merchant, CustomerSummary, MisdirectedSummary, Statement, Overview} from './models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class KolektaApi {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;

  createMerchant(name: string): Observable<Merchant> {
    return this.http.post<Merchant>(`${this.base}/api/merchants`, { name });
  }
  onboardCustomer(merchantId: string, body: { name: string; email: string; phone: string }): Observable<unknown> {
    return this.http.post(`${this.base}/api/merchants/${merchantId}/customers`, body);
  }
  getMerchants(): Observable<Merchant[]> {
    return this.http.get<Merchant[]>(`${this.base}/api/merchants`);
  }
  getCustomers(merchantId: string): Observable<CustomerSummary[]> {
    return this.http.get<CustomerSummary[]>(`${this.base}/api/merchants/${merchantId}/customers`);
  }
  getMisdirected(): Observable<MisdirectedSummary[]> {
    return this.http.get<MisdirectedSummary[]>(`${this.base}/api/misdirected-payments`);
  }
  deleteMerchant(merchantId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/merchants/${merchantId}`);
  }
  getStatement(customerId: string): Observable<Statement> {
    return this.http.get<Statement>(`${this.base}/api/customers/${customerId}/statement`);
  }
  getOverview(): Observable<Overview> {
    return this.http.get<Overview>(`${this.base}/api/overview`);
  }
}
