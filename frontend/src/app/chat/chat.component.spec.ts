import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { ChatComponent } from './chat.component';

describe('ChatComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: KeycloakService, useValue: { getToken: async () => 'tok' } },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders markdown to HTML', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    const html = fixture.componentInstance.render('**bold**');
    expect(html).toContain('<strong>bold</strong>');
  });

  it('detects today date', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    expect(fixture.componentInstance.isToday(new Date().toISOString())).toBeTrue();
  });
});

