import { Component, OnInit, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { SidebarComponent } from './core/sidebar/sidebar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
  private kc = inject(KeycloakService);

  kcEnabled = true;
  collapsed = signal(false);
  darkMode = signal<boolean>(typeof localStorage !== 'undefined' && localStorage.getItem('yeredon-dark') === '1');
  user = signal({ name: 'Utilisateur', initials: 'U', role: 'ROLE_VIEWER' });

  constructor() {
    effect(() => {
      if (typeof document !== 'undefined') {
        document.documentElement.setAttribute('data-theme', this.darkMode() ? 'dark' : 'light');
      }
      try { localStorage.setItem('yeredon-dark', this.darkMode() ? '1' : '0'); } catch {}
    });
  }

  async ngOnInit() {
    try {
      const username = this.kc.getUsername() || 'invité';
      const roles = this.kc.getUserRoles() || [];
      const role = roles.find(r => r.startsWith('ROLE_')) || 'ROLE_VIEWER';
      const initials = username.split(/[\s.@]+/).map(s => s[0]).slice(0, 2).join('').toUpperCase() || 'U';
      this.user.set({ name: username, initials, role });
    } catch { /* offline / SSR */ }
  }

  logout() { try { this.kc.logout(); } catch {} }
}

