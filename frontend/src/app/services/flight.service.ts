import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FlightData } from '../models/flight.model';

const API = 'http://localhost:8080/api/flights';

@Injectable({ providedIn: 'root' })
export class FlightService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<FlightData[]> {
    return this.http.get<FlightData[]>(API);
  }

  get(id: string): Observable<FlightData> {
    return this.http.get<FlightData>(`${API}/${id}`);
  }

  create(flight: Partial<FlightData>): Observable<FlightData> {
    return this.http.post<FlightData>(API, flight);
  }

  updateStatus(id: string, status: string): Observable<FlightData> {
    return this.http.patch<FlightData>(`${API}/${id}/status`, { status });
  }

  updateGate(id: string, gate: string): Observable<FlightData> {
    return this.http.patch<FlightData>(`${API}/${id}/gate`, { gate });
  }

  getHistory(id: string): Observable<any> {
    return this.http.get(`${API}/${id}/history`);
  }
}
