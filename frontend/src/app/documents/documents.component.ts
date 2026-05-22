import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { ApiService } from '../core/api.service';
import { environment } from '../../environments/environment';

interface Doc {
  id: string; filename: string; status: string;
  chunkCount?: number; accessLevel: string; createdAt: string;
  size?: number;
}

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './documents.component.html',
  styleUrls: ['./documents.component.css'],
})
export class DocumentsComponent implements OnInit {
  private http = inject(HttpClient);
  private api = inject(ApiService);
  private base = environment.apiUrl + '/api/v1/documents';

  docs = signal<Doc[]>([]);
  progress = signal(0);
  dragOver = signal(false);

  async ngOnInit() { await this.refresh(); }

  onDragOver(e: DragEvent) { e.preventDefault(); this.dragOver.set(true); }
  onDrop(e: DragEvent) {
    e.preventDefault(); this.dragOver.set(false);
    const f = e.dataTransfer?.files?.[0]; if (f) this.upload(f);
  }
  onFile(e: Event) {
    const f = (e.target as HTMLInputElement).files?.[0]; if (f) this.upload(f);
  }

  upload(f: File) {
    const fd = new FormData(); fd.append('file', f);
    this.http.post(`${this.base}/upload`, fd, { reportProgress: true, observe: 'events' })
      .subscribe({
        next: async (ev: any) => {
          if (ev.type === HttpEventType.UploadProgress && ev.total) {
            this.progress.set(Math.round(100 * ev.loaded / ev.total));
          } else if (ev.type === HttpEventType.Response) {
            this.progress.set(0); await this.refresh();
          }
        },
        error: (e) => { this.progress.set(0); alert('Upload échoué : ' + e.message); }
      });
  }

  async reindex(id: string) { await this.api.reindexDocument(id); await this.refresh(); }
  async delete(id: string) {
    if (!confirm('Supprimer ce document et ses chunks ?')) return;
    await this.api.deleteDocument(id); await this.refresh();
  }

  async refresh() {
    try {
      const list = await this.api.listDocuments();
      this.docs.set((list as any) || []);
    } catch { this.docs.set([]); }
  }
}

