import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { mustChangePasswordGuard } from './core/guards/must-change-password.guard';
import { Role } from './core/models/enums';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
    canActivate: [mustChangePasswordGuard]
  },
  {
    path: 'change-password',
    loadComponent: () => import('./features/auth/change-password/change-password.component').then(m => m.ChangePasswordComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard, mustChangePasswordGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/dashboard/home/home.component').then(m => m.HomeComponent),
        canActivate: [mustChangePasswordGuard]
      },
      // Admin routes
      {
        path: 'students',
        loadComponent: () => import('./features/students/student-list/student-list.component').then(m => m.StudentListComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'students/new',
        loadComponent: () => import('./features/students/student-form/student-form.component').then(m => m.StudentFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'students/:id/edit',
        loadComponent: () => import('./features/students/student-form/student-form.component').then(m => m.StudentFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'professors',
        loadComponent: () => import('./features/professors/professor-list/professor-list.component').then(m => m.ProfessorListComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'professors/new',
        loadComponent: () => import('./features/professors/professor-form/professor-form.component').then(m => m.ProfessorFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'professors/:id/edit',
        loadComponent: () => import('./features/professors/professor-form/professor-form.component').then(m => m.ProfessorFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'rooms',
        loadComponent: () => import('./features/rooms/room-list/room-list.component').then(m => m.RoomListComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'rooms/new',
        loadComponent: () => import('./features/rooms/room-form/room-form.component').then(m => m.RoomFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'rooms/:id/edit',
        loadComponent: () => import('./features/rooms/room-form/room-form.component').then(m => m.RoomFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'defenses',
        loadComponent: () => import('./features/defenses/defense-list/defense-list.component').then(m => m.DefenseListComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'defenses/new',
        loadComponent: () => import('./features/defenses/defense-form/defense-form.component').then(m => m.DefenseFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'defenses/:id/edit',
        loadComponent: () => import('./features/defenses/defense-form/defense-form.component').then(m => m.DefenseFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      },
      {
        path: 'defenses/:id',
        loadComponent: () => import('./features/defenses/defense-detail/defense-detail.component').then(m => m.DefenseDetailComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN, Role.PROFESSOR] }
      },
      // Professor routes
      {
        path: 'jury',
        loadComponent: () => import('./features/jury/jury-dashboard/jury-dashboard.component').then(m => m.JuryDashboardComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.PROFESSOR] }
      },
      {
        path: 'jury/:defenseId/grade',
        loadComponent: () => import('./features/jury/grade-form/grade-form.component').then(m => m.GradeFormComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.PROFESSOR] }
      },
      // Student routes
      {
        path: 'my-defense',
        loadComponent: () => import('./features/student-view/my-defense/my-defense.component').then(m => m.MyDefenseComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.STUDENT] }
      },
      {
        path: 'my-results',
        loadComponent: () => import('./features/student-view/my-results/my-results.component').then(m => m.MyResultsComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.STUDENT] }
      },
      // Results (Admin view)
      {
        path: 'results',
        loadComponent: () => import('./features/results/results-list/results-list.component').then(m => m.ResultsListComponent),
        canActivate: [mustChangePasswordGuard, roleGuard],
        data: { roles: [Role.ADMIN] }
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
