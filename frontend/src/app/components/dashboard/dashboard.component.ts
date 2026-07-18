import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FlightService } from '../../services/flight.service';
import { FlightData, FLIGHT_STATUSES } from '../../models/flight.model';

interface FieldChange { label: string; from: string; to: string; }
interface HistoryEntry {
  timestamp: string;
  updatedBy: string;
  isCreation: boolean;
  changes: FieldChange[];
}
// Potwierdzenie akcji
interface ConfirmState {
  title: string;
  message: string;
  confirmLabel: string;
  action: () => void;
}

@Component({
    selector: 'app-dashboard',
    imports: [CommonModule, FormsModule],
    template: `
    <header class="topbar">
      <div class="brand">✈ Airport Blockchain</div>
      <div class="user-area">
        <span class="role-badge">{{ auth.role() }}</span>
        <span class="username">{{ auth.username() }}</span>
        <button class="btn-ghost btn-sm" (click)="logout()">Wyloguj</button>
      </div>
    </header>

    <main class="container">
      <div class="header-row">
        <div>
          <h2>Loty</h2>
          <p class="muted">{{ flights().length }} lotow w rejestrze blockchain</p>
        </div>
        <div style="display:flex; gap:8px;">
          <button class="btn-ghost btn-sm" (click)="load()">Odswiez</button>
          @if (auth.canCreate()) {
            <button class="btn-primary btn-sm" (click)="showCreate = true">+ Nowy lot</button>
          }
        </div>
      </div>

      @if (error) { <div class="error-msg">{{ error }}</div> }

      <div class="card" style="padding:0; overflow:hidden; margin-top:16px;">
        <table>
          <thead>
          <tr>
            <th>Lot</th><th>Linia</th><th>Trasa</th><th>Bramka</th>
            <th>Status</th><th>Wylot</th><th>Akcje</th>
          </tr>
          </thead>
          <tbody>
            @for (f of flights(); track f.flightId) {
              <tr>
                <td><strong>{{ f.flightId }}</strong></td>
                <td>{{ f.airline }}</td>
                <td>{{ f.origin }} → {{ f.destination }}</td>
                <td>{{ f.gate }}</td>
                <td><span class="badge badge-{{ f.status }}">{{ f.status }}</span></td>
                <td class="muted">{{ f.scheduledDep | date:'dd.MM HH:mm' }}</td>
                <td>
                  <div class="actions">
                    @if (auth.canChangeStatus()) {
                      <button class="btn-ghost btn-sm" (click)="openStatus(f)">Status</button>
                    }
                    @if (auth.canChangeGate()) {
                      <button class="btn-ghost btn-sm" (click)="openGate(f)">Bramka</button>
                    }
                    @if (auth.canViewHistory()) {
                      <button class="btn-ghost btn-sm" (click)="openHistory(f)">Historia</button>
                    }
                  </div>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="7" class="muted" style="text-align:center; padding:32px;">
                Brak lotow. {{ auth.canCreate() ? 'Dodaj pierwszy lot.' : '' }}
              </td></tr>
            }
          </tbody>
        </table>
      </div>
    </main>

    <!-- Modal: nowy lot -->
    @if (showCreate) {
      <div class="overlay" (click)="showCreate = false">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Nowy lot</h3>
          <div class="grid2">
            <div><label>ID lotu</label><input [(ngModel)]="form.flightId" placeholder="LOT101" /></div>
            <div><label>Linia</label><input [(ngModel)]="form.airline" placeholder="LOT" /></div>
            <div><label>Skad (IATA)</label><input [(ngModel)]="form.origin" placeholder="WAW" maxlength="3" /></div>
            <div><label>Dokad (IATA)</label><input [(ngModel)]="form.destination" placeholder="JFK" maxlength="3" /></div>
            <div><label>Bramka</label><input [(ngModel)]="form.gate" placeholder="D5" /></div>
            <div><label>Status</label>
              <select [(ngModel)]="form.status">
                @for (s of statuses; track s) { <option [value]="s">{{ s }}</option> }
              </select>
            </div>
          </div>
          <div><label>Wylot (ISO-8601)</label><input [(ngModel)]="form.scheduledDep" placeholder="2026-05-29T18:00:00Z" /></div>
          @if (modalError) { <div class="error-msg">{{ modalError }}</div> }
          <div class="modal-actions">
            <button class="btn-ghost" (click)="showCreate = false">Anuluj</button>
            <button class="btn-primary" (click)="confirmCreate()">Utworz</button>
          </div>
        </div>
      </div>
    }

    <!-- Modal: zmiana statusu -->
    @if (statusFlight) {
      <div class="overlay" (click)="statusFlight = null">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Zmien status — {{ statusFlight.flightId }}</h3>
          <label>Nowy status</label>
          <select [(ngModel)]="newStatus">
            @for (s of statuses; track s) { <option [value]="s">{{ s }}</option> }
          </select>
          @if (modalError) { <div class="error-msg">{{ modalError }}</div> }
          <div class="modal-actions">
            <button class="btn-ghost" (click)="statusFlight = null">Anuluj</button>
            <button class="btn-primary"
                    [disabled]="newStatus === statusFlight.status"
                    (click)="confirmStatus()">Zapisz</button>
          </div>
        </div>
      </div>
    }

    <!-- Modal: zmiana bramki -->
    @if (gateFlight) {
      <div class="overlay" (click)="gateFlight = null">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Zmien bramke — {{ gateFlight.flightId }}</h3>
          <label>Nowa bramka</label>
          <input [(ngModel)]="newGate" placeholder="C7" />
          @if (modalError) { <div class="error-msg">{{ modalError }}</div> }
          <div class="modal-actions">
            <button class="btn-ghost" (click)="gateFlight = null">Anuluj</button>
            <button class="btn-primary"
                    [disabled]="!newGate.trim() || newGate.trim() === gateFlight.gate"
                    (click)="confirmGate()">Zapisz</button>
          </div>
        </div>
      </div>
    }

    <!-- Modal: historia (os czasu zmian) -->
    @if (historyFlight) {
      <div class="overlay" (click)="historyFlight = null">
        <div class="modal" (click)="$event.stopPropagation()" style="max-width:560px;">
          <h3>Historia zmian — {{ historyFlight.flightId }}</h3>
          @if (historyLoading) {
            <p class="muted">Ladowanie...</p>
          } @else if (historyError) {
            <div class="error-msg">{{ historyError }}</div>
          } @else {
            <div class="timeline">
              @for (entry of historyEntries(); track $index) {
                <div class="tl-item">
                  <div class="tl-dot" [class.tl-dot-create]="entry.isCreation"></div>
                  <div class="tl-content">
                    <div class="tl-head">
                      <span class="role-badge">{{ entry.updatedBy }}</span>
                      <span class="tl-time">{{ entry.timestamp | date:'dd.MM.yyyy HH:mm:ss' }}</span>
                    </div>
                    @if (entry.isCreation) {
                      <div class="tl-action tl-create">Utworzenie lotu</div>
                    } @else if (entry.changes.length === 0) {
                      <div class="tl-action muted">Zapis bez zmian pol</div>
                    } @else {
                      @for (c of entry.changes; track c.label) {
                        <div class="tl-change">
                          <span class="tl-field">{{ c.label }}:</span>
                          <span class="tl-from">{{ c.from }}</span>
                          <span class="tl-arrow">→</span>
                          <span class="tl-to">{{ c.to }}</span>
                        </div>
                      }
                    }
                  </div>
                </div>
              }
            </div>
          }
          <div class="modal-actions">
            <button class="btn-ghost" (click)="historyFlight = null">Zamknij</button>
          </div>
        </div>
      </div>
    }

    <!-- Modal: POTWIERDZENIE (nad wszystkimi) -->
    @if (confirm) {
      <div class="overlay overlay-confirm" (click)="confirm = null">
        <div class="modal modal-confirm" (click)="$event.stopPropagation()">
          <h3>{{ confirm.title }}</h3>
          <p class="confirm-msg">{{ confirm.message }}</p>
          <div class="modal-actions">
            <button class="btn-ghost" (click)="confirm = null">Anuluj</button>
            <button class="btn-primary" (click)="runConfirm()">{{ confirm.confirmLabel }}</button>
          </div>
        </div>
      </div>
    }
  `,
    styles: [`
    .topbar { display:flex; justify-content:space-between; align-items:center;
      padding:14px 24px; background:var(--surface); border-bottom:1px solid var(--border); }
    .brand { font-weight:700; font-size:16px; }
    .user-area { display:flex; align-items:center; gap:12px; }
    .username { font-size:14px; color:var(--text-dim); }
    .container { max-width:1100px; margin:0 auto; padding:28px 24px; }
    .header-row { display:flex; justify-content:space-between; align-items:flex-end; }
    h2 { font-size:20px; font-weight:600; }
    .muted { color:var(--text-dim); font-size:13px; }
    .actions { display:flex; gap:6px; }
    .overlay { position:fixed; inset:0; background:rgba(0,0,0,.6);
      display:flex; align-items:center; justify-content:center; padding:20px; z-index:50; }
    .overlay-confirm { background:rgba(0,0,0,.5); z-index:60; }
    .modal { background:var(--surface); border:1px solid var(--border); border-radius:var(--radius);
      padding:24px; width:100%; max-width:480px; }
    .modal-confirm { max-width:400px; }
    .modal h3 { margin-bottom:18px; font-weight:600; }
    .confirm-msg { color:var(--text); font-size:14px; line-height:1.6; }
    .grid2 { display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-bottom:12px; }
    .modal-actions { display:flex; justify-content:flex-end; gap:8px; margin-top:20px; }
    .timeline { max-height:380px; overflow:auto; padding-right:4px; }
    .tl-item { display:flex; gap:14px; padding-bottom:18px; position:relative; }
    .tl-item:not(:last-child)::before { content:''; position:absolute; left:6px; top:16px; bottom:0;
      width:2px; background:var(--border); }
    .tl-dot { width:14px; height:14px; border-radius:50%; background:var(--surface-2);
      border:2px solid var(--primary); flex-shrink:0; margin-top:3px; z-index:1; }
    .tl-dot-create { background:var(--success); border-color:var(--success); }
    .tl-content { flex:1; }
    .tl-head { display:flex; align-items:center; gap:10px; margin-bottom:6px; }
    .tl-time { font-size:12px; color:var(--text-dim); }
    .tl-action { font-size:14px; }
    .tl-create { color:#4ade80; font-weight:500; }
    .tl-change { font-size:14px; padding:4px 0; display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
    .tl-field { color:var(--text-dim); }
    .tl-from { color:#f87171; text-decoration:line-through; }
    .tl-arrow { color:var(--text-dim); }
    .tl-to { color:#4ade80; font-weight:500; }
  `]
})
export class DashboardComponent implements OnInit {
  flights = signal<FlightData[]>([]);
  statuses = FLIGHT_STATUSES;
  error = '';
  modalError = '';

  showCreate = false;
  form: any = { status: 'ON_TIME' };

  statusFlight: FlightData | null = null;
  newStatus = 'ON_TIME';

  gateFlight: FlightData | null = null;
  newGate = '';

  historyFlight: FlightData | null = null;
  historyEntries = signal<HistoryEntry[]>([]);
  historyLoading = false;
  historyError = '';

  // Stan potwierdzenia
  confirm: ConfirmState | null = null;

  private readonly TRACKED: { key: keyof FlightData; label: string }[] = [
    { key: 'gate',         label: 'Bramka' },
    { key: 'status',       label: 'Status' },
    { key: 'airline',      label: 'Linia' },
    { key: 'origin',       label: 'Skad' },
    { key: 'destination',  label: 'Dokad' },
    { key: 'scheduledDep', label: 'Wylot' }
  ];

  constructor(
      public auth: AuthService,
      private flightSvc: FlightService,
      private router: Router
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.error = '';
    this.flightSvc.getAll().subscribe({
      next: data => this.flights.set(data),
      error: err => this.error = 'Blad pobierania lotow: ' + (err.error?.message || err.message)
    });
  }

  runConfirm(): void {
    const action = this.confirm?.action;
    this.confirm = null;
    action?.();
  }

  // ── Tworzenie lotu ─────────────────────────────────────────
  confirmCreate(): void {
    this.modalError = '';
    if (!this.form.flightId || !this.form.airline) {
      this.modalError = 'Wypelnij przynajmniej ID lotu i linie';
      return;
    }
    this.confirm = {
      title: 'Potwierdz utworzenie lotu',
      confirmLabel: 'Utworz lot',
      message: `Czy na pewno utworzyc lot ${this.form.flightId} `
          + `(${this.form.origin || '?'} → ${this.form.destination || '?'}), `
          + `bramka ${this.form.gate || '?'}, status ${this.form.status}? `
          + `Operacja zostanie trwale zapisana w blockchainie.`,
      action: () => this.doCreate()
    };
  }
  private doCreate(): void {
    this.flightSvc.create(this.form).subscribe({
      next: () => { this.showCreate = false; this.form = { status: 'ON_TIME' }; this.load(); },
      error: err => this.modalError = err.error?.details?.join(', ') || err.error?.message || 'Blad tworzenia lotu'
    });
  }

  // ── Zmiana statusu ─────────────────────────────────────────
  openStatus(f: FlightData): void { this.statusFlight = f; this.newStatus = f.status; this.modalError = ''; }
  confirmStatus(): void {
    const f = this.statusFlight!;
    this.confirm = {
      title: 'Potwierdz zmiane statusu',
      confirmLabel: 'Zapisz zmiane',
      message: `Zmienic status lotu ${f.flightId} z "${f.status}" na "${this.newStatus}"? `
          + `Zmiana zostanie trwale zapisana w blockchainie.`,
      action: () => this.doSaveStatus()
    };
  }
  private doSaveStatus(): void {
    this.flightSvc.updateStatus(this.statusFlight!.flightId, this.newStatus).subscribe({
      next: () => { this.statusFlight = null; this.load(); },
      error: err => this.modalError = err.error?.message || 'Blad zmiany statusu'
    });
  }

  // ── Zmiana bramki ──────────────────────────────────────────
  openGate(f: FlightData): void { this.gateFlight = f; this.newGate = f.gate; this.modalError = ''; }
  confirmGate(): void {
    const f = this.gateFlight!;
    this.confirm = {
      title: 'Potwierdz zmiane bramki',
      confirmLabel: 'Zapisz zmiane',
      message: `Zmienic bramke lotu ${f.flightId} z "${f.gate}" na "${this.newGate}"? `
          + `Zmiana zostanie trwale zapisana w blockchainie.`,
      action: () => this.doSaveGate()
    };
  }
  private doSaveGate(): void {
    this.flightSvc.updateGate(this.gateFlight!.flightId, this.newGate).subscribe({
      next: () => { this.gateFlight = null; this.load(); },
      error: err => this.modalError = err.error?.message || 'Blad zmiany bramki'
    });
  }

  // ── Historia ───────────────────────────────────────────────
  openHistory(f: FlightData): void {
    this.historyFlight = f;
    this.historyLoading = true;
    this.historyError = '';
    this.historyEntries.set([]);
    this.flightSvc.getHistory(f.flightId).subscribe({
      next: data => { this.historyEntries.set(this.buildTimeline(data)); this.historyLoading = false; },
      error: err => { this.historyError = 'Blad: ' + (err.error?.message || err.message); this.historyLoading = false; }
    });
  }

  private buildTimeline(raw: any): HistoryEntry[] {
    let records: any[];
    try { records = typeof raw === 'string' ? JSON.parse(raw) : raw; }
    catch { this.historyError = 'Nie udalo sie sparsowac historii'; return []; }
    if (!Array.isArray(records)) return [];

    const parsed = records
        .map(r => {
          const data = typeof r.data === 'string' ? JSON.parse(r.data) : r.data;
          return { timestamp: data?.lastUpdated || r.timestamp, data: data as FlightData };
        })
        .sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

    const entries: HistoryEntry[] = [];
    let prev: FlightData | null = null;
    for (const rec of parsed) {
      const d = rec.data;
      if (!prev) {
        entries.push({ timestamp: rec.timestamp, updatedBy: d.updatedBy, isCreation: true, changes: [] });
      } else {
        const changes: FieldChange[] = [];
        for (const t of this.TRACKED) {
          const from = String(prev[t.key] ?? '');
          const to = String(d[t.key] ?? '');
          if (from !== to) changes.push({ label: t.label, from, to });
        }
        entries.push({ timestamp: rec.timestamp, updatedBy: d.updatedBy, isCreation: false, changes });
      }
      prev = d;
    }
    return entries.reverse();
  }

  logout(): void { this.auth.logout(); this.router.navigate(['/login']); }
}