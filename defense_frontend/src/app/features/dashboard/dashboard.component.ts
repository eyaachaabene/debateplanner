import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '@core/services';
import { Role } from '@core/models';

interface NavItem {
  icon: string;
  label: string;
  route: string;
  roles: Role[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    RouterOutlet,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule
  ],
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav mode="side" opened class="sidenav">
        <div class="sidenav-header">
          <mat-icon>school</mat-icon>
          <span>ENSAJ - PFE</span>
        </div>

        <mat-nav-list>
          @for (item of filteredNavItems(); track item.route) {
            <a mat-list-item [routerLink]="item.route" routerLinkActive="active">
              <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
              <span matListItemTitle>{{ item.label }}</span>
            </a>
          }
        </mat-nav-list>
      </mat-sidenav>

      <mat-sidenav-content class="content">
        <mat-toolbar color="primary">
          <span class="toolbar-title">Gestion des Soutenances</span>
          <span class="spacer"></span>

          <button mat-icon-button [matMenuTriggerFor]="userMenu">
            <mat-icon>account_circle</mat-icon>
          </button>

          <mat-menu #userMenu="matMenu">
            <div class="user-info" mat-menu-item disabled>
              <strong>{{ authService.getUsername() || 'Utilisateur' }}</strong>
              <small>{{ displayRole() }}</small>
            </div>
            <mat-divider></mat-divider>
            <button mat-menu-item (click)="logout()">
              <mat-icon>logout</mat-icon>
              <span>Se déconnecter</span>
            </button>
          </mat-menu>
        </mat-toolbar>

        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container {
      height: 100vh;
    }

    .sidenav {
      width: 260px;
      background: #fafafa;
      border-right: 1px solid #e0e0e0;
    }

    .sidenav-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 20px;
      font-size: 18px;
      font-weight: 600;
      color: #3f51b5;
      border-bottom: 1px solid #e0e0e0;
    }

    .sidenav-header mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    mat-nav-list {
      padding-top: 8px;
    }

    mat-nav-list a {
      margin: 4px 8px;
      border-radius: 8px;
    }

    mat-nav-list a.active {
      background: rgba(63, 81, 181, 0.1);
      color: #3f51b5;
    }

    mat-nav-list a.active mat-icon {
      color: #3f51b5;
    }

    .content {
      display: flex;
      flex-direction: column;
    }

    mat-toolbar {
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .toolbar-title {
      font-size: 18px;
    }

    .spacer {
      flex: 1;
    }

    .main-content {
      flex: 1;
      padding: 24px;
      background: #f5f5f5;
      overflow-y: auto;
    }

    .user-info {
      display: flex;
      flex-direction: column;
      padding: 12px 16px;
    }

    .user-info strong {
      font-size: 14px;
    }

    .user-info small {
      font-size: 12px;
      color: #666;
      margin-top: 2px;
    }
  `]
})
export class DashboardComponent {
  readonly authService = inject(AuthService);

  private navItems: NavItem[] = [
    { icon: 'dashboard', label: 'Tableau de bord', route: '/dashboard', roles: [Role.ADMIN] },
    { icon: 'people', label: 'Étudiants', route: '/dashboard/students', roles: [Role.ADMIN] },
    { icon: 'person', label: 'Professeurs', route: '/dashboard/professors', roles: [Role.ADMIN] },
    { icon: 'meeting_room', label: 'Salles', route: '/dashboard/rooms', roles: [Role.ADMIN] },
    { icon: 'event', label: 'Soutenances', route: '/dashboard/defenses', roles: [Role.ADMIN] },
    { icon: 'assessment', label: 'Résultats', route: '/dashboard/results', roles: [Role.ADMIN] },
    { icon: 'gavel', label: 'Mes jurys', route: '/dashboard/jury', roles: [Role.PROFESSOR] },
    { icon: 'school', label: 'Ma soutenance', route: '/dashboard/my-defense', roles: [Role.STUDENT] },
    { icon: 'grade', label: 'Mes résultats', route: '/dashboard/my-results', roles: [Role.STUDENT] }
  ];

  filteredNavItems = computed(() => {
    const userRole = this.authService.getCurrentRole();
    if (!userRole) return [];
    return this.navItems.filter(item => item.roles.includes(userRole));
  });

  displayRole(): string {
    const role = this.authService.getCurrentRole();
    if (role === Role.ADMIN) return 'Administrateur';
    if (role === Role.PROFESSOR) return 'Professeur';
    if (role === Role.STUDENT) return 'Étudiant';
    return 'Connecté';
  }

  logout(): void {
    this.authService.logout();
  }
}