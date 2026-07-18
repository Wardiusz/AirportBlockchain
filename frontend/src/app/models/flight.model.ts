export interface FlightData {
  flightId: string;
  airline: string;
  origin: string;
  destination: string;
  gate: string;
  status: string;
  scheduledDep: string;
  lastUpdated: string;
  updatedBy: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: 'AIRLINE' | 'HANDLER' | 'ADMIN';
}

export const FLIGHT_STATUSES = ['ON_TIME', 'DELAYED', 'CANCELLED', 'BOARDING', 'DEPARTED'];
