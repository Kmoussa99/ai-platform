import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
})
export class AdminComponent implements OnInit {
  private kc = inject(KeycloakService);
  profile = signal<{ username: string; roles: string[] }>({ username: 'invité', roles: [] });
  loggedIn = signal(false);

  async ngOnInit() {
    try {
      this.loggedIn.set(await this.kc.isLoggedIn());
      this.profile.set({
        username: this.kc.getUsername() || 'invité',
        roles: this.kc.getUserRoles() || [],
      });
    } catch { /* */ }
  }
}

