import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Conversation {
  id: string; title: string; messageCount?: number; contextMode?: string;
  pinned?: boolean; updatedAt?: string; createdAt?: string;
}

export interface ChatMessage {
  id?: string;
  role: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL';
  content: string;
  contextMode?: string;
  sources?: { id: string | number; label: string }[];
  createdAt?: string;
  feedback?: 1 | -1 | null;
  streaming?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private kc = inject(KeycloakService);
  private base = environment.apiUrl + '/api/v1/conversations';

  conversations = signal<Conversation[]>([]);
  current = signal<Conversation | null>(null);
  messages = signal<ChatMessage[]>([]);
  streaming = signal<boolean>(false);

  async load() {
    const list = await firstValueFrom(this.http.get<any>(this.base));
    const arr = Array.isArray(list) ? list : (list?.content || []);
    this.conversations.set(arr);
  }

  async create(title: string, contextMode = 'GENERAL'): Promise<Conversation> {
    const c = await firstValueFrom(this.http.post<Conversation>(this.base, { title, contextMode }));
    this.conversations.update(l => [c, ...l]);
    return c;
  }

  async open(id: string) {
    const conv = this.conversations().find(c => c.id === id) || null;
    this.current.set(conv);
    const msgs = await firstValueFrom(this.http.get<any>(`${this.base}/${id}/messages`));
    const arr = Array.isArray(msgs) ? msgs : (msgs?.content || []);
    this.messages.set(arr.map((m: any) => ({
      id: m.id,
      role: (m.role || 'USER').toUpperCase(),
      content: m.content || '',
      contextMode: m.contextMode || m.mode,
      sources: m.sources || [],
      createdAt: m.createdAt,
    })));
  }

  /**
   * Streaming SSE — fetch direct (HttpClient ne supporte pas SSE natif).
   * Token Bearer ajouté manuellement (pas via interceptor).
   */
  async send(content: string) {
    let conv = this.current();
    if (!conv) conv = await this.create(content.slice(0, 50));

    this.messages.update(m => [
      ...m,
      { role: 'USER', content, createdAt: new Date().toISOString() },
      { role: 'ASSISTANT', content: '', streaming: true, sources: [], createdAt: new Date().toISOString() },
    ]);
    this.streaming.set(true);

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
    };
    try {
      const tok = await this.kc.getToken();
      if (tok) headers['Authorization'] = 'Bearer ' + tok;
    } catch {}

    try {
      const resp = await fetch(`${this.base}/${conv.id}/messages`, {
        method: 'POST', headers, body: JSON.stringify({ content }),
      });
      if (!resp.ok || !resp.body) throw new Error('SSE HTTP ' + resp.status);
      const reader = resp.body.getReader();
      const decoder = new TextDecoder();
      let buf = '', full = '', sources: any[] = [];
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buf += decoder.decode(value, { stream: true });
        const events = buf.split(/\r?\n\r?\n/);
        buf = events.pop() || '';
        for (const ev of events) {
          const lines = ev.split(/\r?\n/);
          const evtLine = lines.find(l => l.startsWith('event:'));
          const dataParts = lines
            .filter(l => l.startsWith('data:'))
            .map(l => l.slice(5).replace(/^ /, ''));
          if (!dataParts.length) continue;
          const data = dataParts.join('\n');
          if (evtLine && evtLine.includes('sources')) {
            try { sources = JSON.parse(data); } catch {}
          } else if (!evtLine || !evtLine.includes('done')) {
            full += data;
            this.appendToLast(full);
          }
        }
      }
      this.finalizeLast(full, sources);
    } catch (e: any) {
      this.appendToLast('⚠️ Erreur : ' + (e?.message || e));
      this.finalizeLast(undefined, []);
    } finally {
      this.streaming.set(false);
    }
  }

  private appendToLast(full: string) {
    this.messages.update(m => {
      const c = [...m]; const last = c[c.length - 1];
      if (last && last.role === 'ASSISTANT') c[c.length - 1] = { ...last, content: full };
      return c;
    });
  }
  private finalizeLast(full: string | undefined, sources: any[]) {
    this.messages.update(m => {
      const c = [...m]; const last = c[c.length - 1];
      if (last && last.role === 'ASSISTANT') {
        c[c.length - 1] = {
          ...last,
          content: full ?? last.content,
          sources: sources || [],
          streaming: false,
        };
      }
      return c;
    });
  }

  async delete(id: string) {
    await firstValueFrom(this.http.delete(`${this.base}/${id}`));
    this.conversations.update(l => l.filter(c => c.id !== id));
    if (this.current()?.id === id) { this.current.set(null); this.messages.set([]); }
  }

  feedback(messageId: string, value: 1 | -1) {
    return firstValueFrom(this.http.put(
      `${environment.apiUrl}/api/v1/messages/${messageId}/feedback?value=${value}`, {}
    ));
  }
}
