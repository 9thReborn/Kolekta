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

export interface StatementLine {
  date: string;
  direction: string;
  amountKobo: number;
  amountText: string;
  runningBalanceKobo: number;
  runningBalanceText: string;
}

export interface Statement {
  customerId: string;
  customerName: string;
  currency: string;
  totalCreditsKobo: number;
  totalCreditsText: string;
  totalDebitsKobo: number;
  totalDebitsText: string;
  balanceKobo: number;
  balanceText: string;
  lines: StatementLine[];
}
