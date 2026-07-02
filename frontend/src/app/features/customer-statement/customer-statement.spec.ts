import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerStatement } from './customer-statement';

describe('CustomerStatement', () => {
  let component: CustomerStatement;
  let fixture: ComponentFixture<CustomerStatement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerStatement],
    }).compileComponents();

    fixture = TestBed.createComponent(CustomerStatement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
