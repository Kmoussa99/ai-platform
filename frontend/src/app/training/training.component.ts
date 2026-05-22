import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  selector: 'app-training',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './training.component.html',
  styleUrls: ['./training.component.css'],
})
export class TrainingComponent implements OnInit, OnDestroy {
  private api = inject(ApiService);

  cfg: any = { base_model: 'qwen2.5-coder:7b', method: 'LORA', dataset_path: 's3://ai-datasets/train.jsonl', epochs: 3 };
  jobs = signal<any[]>([]);
  stats = signal<any>(null);
  private timer?: any;

  async ngOnInit() {
    await this.refresh();
    this.timer = setInterval(() => this.refresh(), 5000);
  }
  ngOnDestroy() { if (this.timer) clearInterval(this.timer); }

  async launch() {
    try { await this.api.createMlJob(this.cfg); await this.refresh(); }
    catch (e: any) { alert('Erreur : ' + e.message); }
  }

  async exportDataset() {
    try {
      const data = await this.api.exportDataset('alpaca');
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'dataset-alpaca.json'; a.click();
      URL.revokeObjectURL(url);
    } catch (e: any) { alert('Export échoué : ' + e.message); }
  }

  async refresh() {
    try { this.jobs.set((await this.api.listMlModels()) as any || []); } catch { /* */ }
    try { this.stats.set(await this.api.trainingStats()); } catch { /* */ }
  }

  badgeFor(s: string) {
    if (s === 'COMPLETED' || s === 'READY') return 'badge-success';
    if (s === 'FAILED') return 'badge-danger';
    if (s === 'RUNNING') return 'badge-info';
    return 'badge-warning';
  }

  iconFor(s: string) {
    if (s === 'COMPLETED' || s === 'READY') return 'bi-check-circle-fill';
    if (s === 'FAILED') return 'bi-x-circle-fill';
    if (s === 'RUNNING') return 'bi-arrow-repeat';
    return 'bi-hourglass-split';
  }
}

