import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Claim } from '../models/claim.model';

@Injectable({
  providedIn: 'root'
})
export class ClaimService {
  private apiUrl = 'http://localhost:8080/api/claims';

  constructor(private http: HttpClient) {}

  createClaim(claim: Partial<Claim>): Observable<Claim> {
    return this.http.post<Claim>(this.apiUrl, claim);
  }

  getMyClaims(): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.apiUrl}/my-claims`);
  }

  getClaimById(id: number): Observable<Claim> {
    return this.http.get<Claim>(`${this.apiUrl}/${id}`);
  }

  updateClaim(id: number, claim: Partial<Claim>): Observable<Claim> {
    return this.http.put<Claim>(`${this.apiUrl}/${id}`, claim);
  }

  deleteClaim(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getAllClaims(): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.apiUrl}/all`);
  }

  getPendingClaims(): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.apiUrl}/pending`);
  }

  getStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statistics`);
  }

  approveClaim(id: number, data: { reviewComments: string, approvedAmount?: number }): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/${id}/approve`, data);
  }

  rejectClaim(id: number, data: { reviewComments: string }): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/${id}/reject`, data);
  }

  updateStatus(id: number, status: string): Observable<Claim> {
    return this.http.put<Claim>(`${this.apiUrl}/${id}/status`, { status });
  }
}
