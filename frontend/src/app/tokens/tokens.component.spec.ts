import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { TokensComponent } from './tokens.component';

describe('TokensComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TokensComponent],
      providers: [
        provideHttpClient(),
        { provide: KeycloakService, useValue: { getToken: async () => 'tok' } },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(TokensComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('toggles a scope on/off', () => {
    const fixture = TestBed.createComponent(TokensComponent);
    const cmp = fixture.componentInstance;
    cmp.toggle('CHAT_READ');
    expect(cmp.scopes).toContain('CHAT_READ');
    cmp.toggle('CHAT_READ');
    expect(cmp.scopes).not.toContain('CHAT_READ');
  });
});

