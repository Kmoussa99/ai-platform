import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

export const authGuard: CanActivateFn = async () => {
  const kc = inject(KeycloakService);
  const router = inject(Router);
  if (await kc.isLoggedIn()) return true;
  await kc.login({ redirectUri: window.location.origin + window.location.pathname });
  return false;
};

