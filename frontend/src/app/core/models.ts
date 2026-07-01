export interface Merchant {
  merchantId: string;
  accountRef: string;
  name: string;
}

export interface CustomerSummary {
  customerId: string;
  name: string;
  email: string;
  accountNumber: string | null;
  bankName: string | null;
  balanceKobo: number;
  balanceText: string;
}

export interface MisdirectedSummary {
  id: string;
  merchantId: string | null;
  reason: string;
  amountKobo: number | null;
  amountText: string | null;
  status: string;
  createdAt: string;
}
