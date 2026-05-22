import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

interface Employee { id?: string; matricule: string; nom: string; prenoms: string; salaireBrutBase: number; }
interface Bulletin {
  id?: string; salaireBrutTotal: number; cnpsSalarial: number; its: number;
  netAPayer: number; coutTotalEmployeur: number; status?: string;
  cnpsPatronal?: number; primes?: number;
}

@Component({
  selector: 'app-payroll',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payroll.component.html',
  styleUrls: ['./payroll.component.css'],
})
export class PayrollComponent implements OnInit {
  private api = inject(ApiService);

  emp: Employee = { matricule: '', nom: '', prenoms: '', salaireBrutBase: 250000 };
  result = signal<Bulletin | null>(null);
  employees = signal<Employee[]>([]);
  loading = signal(false);

  async ngOnInit() { await this.loadEmployees(); }

  async simulate() {
    this.loading.set(true);
    try {
      const r = await this.api.simulatePayroll(this.emp);
      this.result.set(r as any);
    } catch (e: any) { alert('Erreur : ' + e.message); }
    finally { this.loading.set(false); }
  }

  reset() {
    this.emp = { matricule: '', nom: '', prenoms: '', salaireBrutBase: 250000 };
    this.result.set(null);
  }

  async loadEmployees() {
    try {
      const l = await this.api.listEmployees();
      this.employees.set((l as any) || []);
    } catch { this.employees.set([]); }
  }

  retentionPct() {
    const r = this.result(); if (!r) return 0;
    if (!r.salaireBrutTotal) return 0;
    return (((r.cnpsSalarial + r.its) / r.salaireBrutTotal) * 100).toFixed(1);
  }
}

