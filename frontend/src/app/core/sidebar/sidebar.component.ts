import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface NavItem { id: string; icon: string; label: string; path: string; }

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css'],
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Input() darkMode = false;
  @Input() kcEnabled = true;
  @Input() user: { name: string; initials: string; role: string } =
    { name: 'Utilisateur', initials: 'U', role: 'ROLE_VIEWER' };
  @Input() model = 'qwen2.5:1.5b';

  @Output() toggleCollapse = new EventEmitter<void>();
  @Output() toggleDark = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  navItems: NavItem[] = [
    { id: 'chat',      icon: 'bi-chat-dots',          label: 'Conversations',        path: 'chat' },
    { id: 'tokens',    icon: 'bi-key',                label: 'API Tokens',           path: 'tokens' },
    { id: 'documents', icon: 'bi-journal-bookmark',   label: 'Base de connaissance', path: 'documents' },
    { id: 'payroll',   icon: 'bi-briefcase',          label: 'Paie',                 path: 'payroll' },
    { id: 'training',  icon: 'bi-cpu',                label: 'Entraînement',         path: 'training' },
  ];
  adminItems: NavItem[] = [
    { id: 'admin', icon: 'bi-gear', label: 'Administration', path: 'admin' },
  ];
}

