import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PrintTestPage } from './print-test.page';

describe('PrintTestPage', () => {
  let component: PrintTestPage;
  let fixture: ComponentFixture<PrintTestPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(PrintTestPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
