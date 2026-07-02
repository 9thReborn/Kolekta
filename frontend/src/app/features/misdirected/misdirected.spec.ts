import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Misdirected } from './misdirected';

describe('Misdirected', () => {
  let component: Misdirected;
  let fixture: ComponentFixture<Misdirected>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Misdirected],
    }).compileComponents();

    fixture = TestBed.createComponent(Misdirected);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
