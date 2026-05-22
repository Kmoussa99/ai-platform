import { TestBed } from '@angular/core/testing';
import { KeycloakService } from 'keycloak-angular';
import { AdminComponent } from './admin.component';

describe('AdminComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminComponent],
      providers: [
        { provide: KeycloakService, useValue: {
            isLoggedIn: async () => true,
            getUsername: () => 'jane',
            getUserRoles: () => ['ROLE_ADMIN'],
          } },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(AdminComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('loads keycloak profile on init', async () => {
    const fixture = TestBed.createComponent(AdminComponent);
    await fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.profile().username).toBe('jane');
    expect(fixture.componentInstance.loggedIn()).toBeTrue();
  });
});

