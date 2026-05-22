import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'chat', pathMatch: 'full' },
  { path: 'chat', loadComponent: () => import('./chat/chat.component').then(m => m.ChatComponent), canActivate: [authGuard] },
  { path: 'chat/:id', loadComponent: () => import('./chat/chat.component').then(m => m.ChatComponent), canActivate: [authGuard] },
  { path: 'tokens', loadComponent: () => import('./tokens/tokens.component').then(m => m.TokensComponent), canActivate: [authGuard] },
  { path: 'documents', loadComponent: () => import('./documents/documents.component').then(m => m.DocumentsComponent), canActivate: [authGuard] },
  { path: 'payroll', loadComponent: () => import('./payroll/payroll.component').then(m => m.PayrollComponent), canActivate: [authGuard] },
  { path: 'training', loadComponent: () => import('./training/training.component').then(m => m.TrainingComponent), canActivate: [authGuard] },
  { path: 'admin', loadComponent: () => import('./admin/admin.component').then(m => m.AdminComponent), canActivate: [authGuard] }
];

