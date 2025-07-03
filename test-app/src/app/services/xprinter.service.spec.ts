import { TestBed } from '@angular/core/testing';

import { XprinterService } from './xprinter.service';

describe('XprinterService', () => {
  let service: XprinterService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(XprinterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
