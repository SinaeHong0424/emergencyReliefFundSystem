import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ClaimService } from '../../services/claim.service';
import { AuthService } from '../../services/auth.service';
import { AccessibilityService } from '../../services/accessibility.service';
import { Claim } from '../../models/claim.model';
import { AuthResponse } from '../../models/user.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: AuthResponse | null = null;
  
  claims: Claim[] = [];
  recentClaims: Claim[] = [];
  pendingClaims: Claim[] = [];
  
  loading = true;
  error = '';
  stats = { total: 0, pending: 0, approved: 0, rejected: 0 };

  private destroy$ = new Subject<void>();

  constructor(
    private claimService: ClaimService,
    private authService: AuthService,
    private accessibilityService: AccessibilityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.accessibilityService.announce('Dashboard loaded', 'polite');

    console.log('Current User Role:', this.currentUser?.role);

    if (this.currentUser?.role === 'ROLE_ADMIN') {
        this.loadAdminData();
    } else {
        this.loadUserData();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadUserData(): void {
    this.loading = true;
    this.claimService.getMyClaims()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (claims) => {
          this.claims = claims;
          this.recentClaims = claims.slice(0, 5);
          this.calculateStats();
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to load user claims:', error);
          this.error = 'Failed to load your claims.';
          this.loading = false;
        }
      });
  }

  loadAdminData(): void {
    this.loading = true;
    this.claimService.getPendingClaims()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (claims) => {
          this.pendingClaims = claims;
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to load pending claims:', error);
          this.error = 'Failed to load admin data.';
          this.loading = false;
        }
      });
  }

  calculateStats(): void {
    if (!this.claims) return;
    this.stats.total = this.claims.length;
    this.stats.pending = this.claims.filter(c => c.status === 'PENDING').length;
    this.stats.approved = this.claims.filter(c => c.status === 'APPROVED').length;
    this.stats.rejected = this.claims.filter(c => c.status === 'REJECTED').length;
  }

  processClaim(id: number | undefined, status: string): void {
    if (!id) return;
    this.claimService.updateStatus(id, status)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.accessibilityService.announce(`Claim ${status}`, 'polite');
          this.pendingClaims = this.pendingClaims.filter(c => c.id !== id);
        },
        error: (error) => console.error(error)
      });
  }

  getStatusClass(status?: string): string {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'APPROVED': return 'status-approved';
      case 'REJECTED': return 'status-rejected';
      default: return 'status-default';
    }
  }

  getDisasterTypeLabel(type?: string): string {
    const labels: { [key: string]: string } = {
        'FLOOD': 'Flood',
        'HURRICANE': 'Hurricane',
        'WILDFIRE': 'Wildfire',
        'TORNADO': 'Tornado',
        'EARTHQUAKE': 'Earthquake',
        'OTHER': 'Other'
    };
    return labels[type || 'OTHER'] || type || 'Unknown';
  }

  navigateToMyClaims(): void { this.router.navigate(['/my-claims']); }
  navigateToNewClaim(): void { this.router.navigate(['/claim-form']); }
  logout(): void { this.authService.logout(); }
}