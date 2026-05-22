import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { TrainingComponent } from './training.component';

describe('TrainingComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainingComponent],
      providers: [
        provideHttpClient(),
        { provide: KeycloakService, useValue: { getToken: async () => 'tok' } },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(TrainingComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('maps status to bootstrap badge classes', () => {
    const fixture = TestBed.createComponent(TrainingComponent);
    const cmp = fixture.componentInstance;
    expect(cmp.badgeFor('COMPLETED')).toBe('badge-success');
    expect(cmp.badgeFor('FAILED')).toBe('badge-danger');
    expect(cmp.badgeFor('RUNNING')).toBe('badge-info');
    expect(cmp.badgeFor('OTHER')).toBe('badge-warning');
  });
});

