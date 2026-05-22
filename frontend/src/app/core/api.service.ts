import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * Façade HTTP centralisée pour le gateway AI Platform.
 * Toutes les routes sont préfixées par {apiUrl}/api/v1.
 * L'authentification (Bearer token Keycloak) est ajoutée par AuthInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  readonly base = environment.apiUrl + '/api/v1';

  // ---- Conversations / Chat ----
  listConversations = () => firstValueFrom(this.http.get<any[]>(`${this.base}/conversations`));
  createConversation = (title: string, contextMode = 'GENERAL') =>
    firstValueFrom(this.http.post<any>(`${this.base}/conversations`, { title, contextMode }));
  getMessages = (id: string) => firstValueFrom(this.http.get<any[]>(`${this.base}/conversations/${id}/messages`));
  deleteConversation = (id: string) => firstValueFrom(this.http.delete(`${this.base}/conversations/${id}`));
  feedback = (messageId: string, value: number | string) =>
    firstValueFrom(this.http.put(`${this.base}/messages/${messageId}/feedback?value=${value}`, {}));

  // ---- Tokens API ----
  listTokens = () => firstValueFrom(this.http.get<any[]>(`${this.base}/tokens`));
  createToken = (name: string, scopes: string[], rateLimit = 100, expiresInDays: number | null = null) =>
    firstValueFrom(this.http.post<any>(`${this.base}/tokens`, { name, scopes, rateLimit, expiresInDays }));
  revokeToken = (id: string) => firstValueFrom(this.http.delete(`${this.base}/tokens/${id}`));
  rotateToken = (id: string) => firstValueFrom(this.http.post<any>(`${this.base}/tokens/${id}/rotate`, {}));
  tokenUsage = (id: string) => firstValueFrom(this.http.get<any>(`${this.base}/tokens/${id}/usage`));

  // ---- Documents ----
  listDocuments = () => firstValueFrom(this.http.get<any[]>(`${this.base}/documents`));
  deleteDocument = (id: string) => firstValueFrom(this.http.delete(`${this.base}/documents/${id}`));
  reindexDocument = (id: string) => firstValueFrom(this.http.post(`${this.base}/documents/${id}/reindex`, {}));

  // ---- Payroll ----
  listEmployees = () => firstValueFrom(this.http.get<any[]>(`${this.base}/employees`));
  createEmployee = (e: any) => firstValueFrom(this.http.post<any>(`${this.base}/employees`, e));
  calculatePayroll = (employeeId: string, period: string, body: any = {}) =>
    firstValueFrom(this.http.post<any>(`${this.base}/payroll/calculate/${employeeId}/${period}`, body));
  calculateBatch = (period: string) =>
    firstValueFrom(this.http.post<any>(`${this.base}/payroll/calculate-batch/${period}`, {}));
  validateBulletin = (id: string) =>
    firstValueFrom(this.http.post<any>(`${this.base}/payroll/validate/${id}`, {}));
  simulatePayroll = (employee: any) =>
    firstValueFrom(this.http.post<any>(`${this.base}/payroll/simulate`, employee));
  exportDsf = (period: string) => firstValueFrom(this.http.get<any>(`${this.base}/payroll/export/dsf/${period}`));
  exportVirements = (period: string) => firstValueFrom(this.http.get<any>(`${this.base}/payroll/export/virements/${period}`));
  exportJournal = (period: string) => firstValueFrom(this.http.get<any>(`${this.base}/payroll/export/journal/${period}`));

  // ---- Training ----
  listTrainingExamples = (status?: string) =>
    firstValueFrom(this.http.get<any[]>(`${this.base}/training/examples${status ? '?status=' + status : ''}`));
  reviewExample = (id: string, body: any) =>
    firstValueFrom(this.http.put(`${this.base}/training/examples/${id}/review`, body));
  trainingStats = () => firstValueFrom(this.http.get<any>(`${this.base}/training/stats`));
  exportDataset = (format = 'alpaca') =>
    firstValueFrom(this.http.get<any>(`${this.base}/training/export?format=${format}`));

  // ---- ML jobs ----
  listMlModels = () => firstValueFrom(this.http.get<any[]>(`${this.base}/ml/models`));
  createMlJob = (cfg: any) => firstValueFrom(this.http.post<any>(`${this.base}/ml/jobs/create`, cfg));
  mlJobStatus = (id: string) => firstValueFrom(this.http.get<any>(`${this.base}/ml/jobs/${id}/status`));
  cancelMlJob = (id: string) => firstValueFrom(this.http.post(`${this.base}/ml/jobs/${id}/cancel`, {}));
}

