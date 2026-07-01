import { TestBed } from '@angular/core/testing';

import { KolektaApi } from './kolekta-api';

describe('KolektaApi', () => {
  let service: KolektaApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KolektaApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
