import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-login',
    imports: [FormsModule],
    template: `
    <div class="login-wrap">
      <div class="card login-card">
        <div class="logo">✈</div>
        <h1>Airport Blockchain</h1>
        <p class="subtitle">System udostepniania danych operacyjnych</p>

        <div class="field">
          <label>Nazwa uzytkownika</label>
          <input [(ngModel)]="username" (keyup.enter)="login()" placeholder="np. admin" autofocus />
        </div>
        <div class="field">
          <label>Haslo</label>
          <input type="password" [(ngModel)]="password" (keyup.enter)="login()" placeholder="••••••••" />
        </div>

        <button class="btn-primary" style="width:100%; margin-top:8px;"
                [disabled]="loading" (click)="login()">
          {{ loading ? 'Logowanie...' : 'Zaloguj sie' }}
        </button>

        @if (error) {
          <div class="error-msg">{{ error }}</div>
        }

        <div class="hint">
          <strong>Konta testowe:</strong><br>
          admin / admin123 &nbsp;·&nbsp; lot_user / airline123 &nbsp;·&nbsp; handler1 / handler123
        </div>
      </div>
    </div>
  `,
    styles: [`
    .login-wrap {
      min-height: 100vh; display: flex; align-items: center; justify-content: center;
      padding: 20px;
      background: radial-gradient(circle at 30% 20%, #1e293b, #0f172a);
    }
    .login-card { width: 100%; max-width: 380px; text-align: center; }
    .logo { font-size: 44px; margin-bottom: 8px; }
    h1 { font-size: 22px; font-weight: 700; }
    .subtitle { color: var(--text-dim); font-size: 13px; margin-bottom: 24px; }
    .field { text-align: left; margin-bottom: 16px; }
    .hint {
      margin-top: 20px; padding-top: 16px; border-top: 1px solid var(--border);
      font-size: 12px; color: var(--text-dim); line-height: 1.7;
    }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  login(): void {
    if (!this.username || !this.password) {
      this.error = 'Podaj nazwe uzytkownika i haslo';
      return;
    }
    this.loading = true;
    this.error = '';
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error = 'Nieprawidlowe dane logowania';
        this.loading = false;
      }
    });
  }
}
