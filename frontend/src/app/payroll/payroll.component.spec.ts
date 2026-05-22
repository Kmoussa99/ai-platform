import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { PayrollComponent } from './payroll.component';

describe('PayrollComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PayrollComponent],
      providers: [
        provideHttpClient(),
        { provide: KeycloakService, useValue: { getToken: async () => 'tok' } },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(PayrollComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('reset clears the form', () => {
    const fixture = TestBed.createComponent(PayrollComponent);
    const cmp = fixture.componentInstance;
    cmp.emp.matricule = 'EMP123';
    cmp.reset();
    expect(cmp.emp.matricule).toBe('');
  });

  it('returns 0 retention when no result', () => {
    const fixture = TestBed.createComponent(PayrollComponent);
    expect(fixture.componentInstance.retentionPct()).toBe(0);
  });
});

