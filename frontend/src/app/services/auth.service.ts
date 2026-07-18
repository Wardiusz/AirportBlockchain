import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse } from '../models/flight.model';

const API = 'http://localhost:8080/api';
const TOKEN_KEY = 'airport_token';
const ROLE_KEY = 'airport_role';
const USER_KEY = 'airport_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Signals — reaktywny stan zalogowania
  role = signal<string | null>(localStorage.getItem(ROLE_KEY));
  username = signal<string | null>(localStorage.getItem(USER_KEY));

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API}/auth/login`, { username, password })
      .pipe(tap(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(ROLE_KEY, res.role);
        localStorage.setItem(USER_KEY, res.username);
        this.role.set(res.role);
        this.username.set(res.username);
      }));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(USER_KEY);
    this.role.set(null);
    this.username.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // Pomocnicze — czy rola moze wykonac dana akcje
  canCreate(): boolean {
    return ['AIRLINE', 'ADMIN'].includes(this.role() ?? '');
  }
  canChangeStatus(): boolean {
    return ['AIRLINE', 'ADMIN'].includes(this.role() ?? '');
  }
  canChangeGate(): boolean {
    return ['AIRLINE', 'HANDLER', 'ADMIN'].includes(this.role() ?? '');
  }
  canViewHistory(): boolean {
    return this.role() === 'ADMIN';
  }
}
