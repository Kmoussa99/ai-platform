import { Component, OnInit, inject, computed, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService, ChatMessage } from '../core/chat.service';
import { marked } from 'marked';

const MODE_BADGES: Record<string, { bg: string; label: string }> = {
  GENERAL:      { bg: '#6C757D', label: 'GÉNÉRAL' },
  CODE:         { bg: '#8E44AD', label: 'CODE' },
  COMPTABILITE: { bg: '#2980B9', label: 'COMPTABILITÉ' },
  DROIT:        { bg: '#922B21', label: 'DROIT' },
  PAIE:         { bg: '#1E8449', label: 'PAIE' },
  DATA:         { bg: '#E67E22', label: 'DATA' },
};

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
})
export class ChatComponent implements OnInit, AfterViewChecked {
  svc = inject(ChatService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  @ViewChild('scroll') scroll?: ElementRef<HTMLDivElement>;

  input = '';
  mode = 'GENERAL';
  model = 'qwen2.5-coder:32b';
  search = '';
  MODE_BADGES = MODE_BADGES;
  modeKeys = Object.keys(MODE_BADGES);

  modeStyle = computed(() => MODE_BADGES[this.mode] || MODE_BADGES['GENERAL']);

  todayConvs = computed(() => this.filtered().filter(c => this.isToday(c.updatedAt || c.createdAt)));
  olderConvs = computed(() => this.filtered().filter(c => !this.isToday(c.updatedAt || c.createdAt)));

  private filtered() {
    const q = this.search.trim().toLowerCase();
    return this.svc.conversations().filter(c => !q || (c.title || '').toLowerCase().includes(q));
  }

  async ngOnInit() {
    try { await this.svc.load(); } catch (e) { console.warn('load conversations:', e); }
    const id = this.route.snapshot.paramMap.get('id');
    if (id) await this.svc.open(id);
  }

  ngAfterViewChecked() {
    if (this.scroll) this.scroll.nativeElement.scrollTop = this.scroll.nativeElement.scrollHeight;
  }

  async open(id: string) { this.router.navigate(['/chat', id]); await this.svc.open(id); }

  async newConversation() {
    const c = await this.svc.create('Nouvelle conversation', this.mode);
    this.router.navigate(['/chat', c.id]);
    await this.svc.open(c.id);
  }

  async send() {
    const content = this.input.trim();
    if (!content || this.svc.streaming()) return;
    this.input = '';
    await this.svc.send(content);
  }

  async deleteCurrent() {
    const c = this.svc.current();
    if (!c) return;
    if (!confirm(`Supprimer « ${c.title} » ?`)) return;
    await this.svc.delete(c.id);
    this.router.navigate(['/chat']);
  }

  async rate(m: ChatMessage, v: 1 | -1) {
    if (!m.id) return;
    m.feedback = v;
    try { await this.svc.feedback(m.id, v); } catch { /* silencieux */ }
  }

  render(text: string): string {
    return marked.parse(text || '', { async: false, breaks: true, gfm: true }) as string;
  }

  modeColor(m: string) { return (MODE_BADGES[m] || MODE_BADGES['GENERAL']).bg; }
  modeLabel(m: string) { return (MODE_BADGES[m] || MODE_BADGES['GENERAL']).label; }

  fmtTime(iso?: string) {
    if (!iso) return '';
    try { return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }); } catch { return ''; }
  }
  fmtDate(iso?: string) {
    if (!iso) return '';
    try { return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' }); } catch { return ''; }
  }
  isToday(iso?: string) {
    if (!iso) return true;
    try { const d = new Date(iso); return d.toDateString() === new Date().toDateString(); } catch { return false; }
  }
}

