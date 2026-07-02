import { HttpClient } from '@angular/common/http';
import { Injectable,inject } from '@angular/core';
import { Observable } from 'rxjs';
import {Merchant, CustomerSummary, MisdirectedSummary, Statement} from './models';

@Injectable({
  providedIn: 'root',
})
export class KolektaApi {
  private http = inject(HttpClient);
  getMerchants(): Observable<Merchant[]> {
    return this.http.get<Merchant[]>('/api/merchants');
  }
  getCustomers(merchantId: string): Observable<CustomerSummary[]> {
    return this.http.get<CustomerSummary[]>(`/api/merchants/${merchantId}/customers`);
  }
  getMisdirected(): Observable<MisdirectedSummary[]> {
    return this.http.get<MisdirectedSummary[]>('/api/misdirected-payments');
  }
  deleteMerchant(merchantId: string): Observable<void> {
    return this.http.delete<void>(`/api/merchants/${merchantId}`);
  }
  getStatement(customerId: string): Observable<Statement> {
    return this.http.get<Statement>(`/api/customers/${customerId}/statement`);
  }
}
