import { Routes } from '@angular/router';
import { authGuard } from './features/auth/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login-page/login-page').then(
        (m) => m.LoginPage
      ),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/pages/dashboard-page/dashboard-page').then(
        (m) => m.DashboardPage
      ),
  },
];

