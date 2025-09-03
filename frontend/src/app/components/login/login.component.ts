import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AccessibilityService } from '../../services/accessibility.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  error = '';
  showPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private accessibilityService: AccessibilityService
  ) {}

  ngOnInit(): void {
    this.accessibilityService.announce('Login page loaded', 'polite');
    
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
    const message = this.showPassword ? 'Password visible' : 'Password hidden';
    this.accessibilityService.announce(message, 'polite');
  }

  openAccessibilitySettings(): void {
    console.log('Accessibility settings clicked');
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.error = '';

    console.log('Attempting login...');

    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        console.log('Login successful!', response);
        this.accessibilityService.announce('Login successful. Redirecting to dashboard.', 'polite');
        this.loading = false;
        
        // Navigate to dashboard
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 100);
      },
      error: (error) => {
        console.error('Login error:', error);
        this.error = 'Invalid username or password';
        this.loading = false;
        this.accessibilityService.announce('Login failed: ' + this.error, 'assertive');
      }
    });
  }
}
