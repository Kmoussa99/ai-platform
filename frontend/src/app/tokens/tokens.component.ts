import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

interface Token {
  id: string; name: string; prefix: string; scopes: string[];
  active: boolean; usageCount?: number; createdAt?: string; lastUsedAt?: string;
  rateLimit?: number;
}

@Component({
  selector: 'app-tokens',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tokens.component.html',
  styleUrls: ['./tokens.component.css'],
})
export class TokensComponent implements OnInit {
  private api = inject(ApiService);
  tokens = signal<Token[]>([]);
  secret = signal<string | null>(null);

  name = '';
  scopes: string[] = [];
  rateLimit = 100;
  expiresInDays: number | null = null;
  allScopes = [
    'CHAT_READ', 'CHAT_WRITE', 'AGENT_EXECUTE', 'DATA_INGEST',
    'PAYROLL_READ', 'PAYROLL_WRITE', 'CODE_EXECUTE',
    'KNOWLEDGE_READ', 'KNOWLEDGE_WRITE', 'MODEL_QUERY',
  ];

  async ngOnInit() { await this.refresh(); }

  toggle(s: string) {
    this.scopes = this.scopes.includes(s) ? this.scopes.filter(x => x !== s) : [...this.scopes, s];
  }

  async create() {
    try {
      const r: any = await this.api.createToken(this.name, this.scopes, this.rateLimit, this.expiresInDays);
      this.secret.set(r?.secret || null);
      this.name = ''; this.scopes = [];
      await this.refresh();
    } catch (e: any) { alert('Erreur création : ' + e.message); }
  }

  async revoke(id: string) {
    if (!confirm('Révoquer ce token ? Cette action est irréversible.')) return;
    await this.api.revokeToken(id); await this.refresh();
  }

  async rotate(id: string) {
    const r: any = await this.api.rotateToken(id);
    this.secret.set(r?.secret || null);
    await this.refresh();
  }

  async refresh() {
    try {
      const list = await this.api.listTokens();
      this.tokens.set((list as any) || []);
    } catch { this.tokens.set([]); }
  }

  copy(s: string) { navigator.clipboard?.writeText(s).catch(() => {}); }
}

