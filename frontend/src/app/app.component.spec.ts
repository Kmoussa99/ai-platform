import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter([]),
        { provide: KeycloakService, useValue: { getUsername: () => 'tester', getUserRoles: () => ['ROLE_ADMIN'], logout: () => {} } },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should default to light theme', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance.darkMode()).toBeFalse();
  });
});

