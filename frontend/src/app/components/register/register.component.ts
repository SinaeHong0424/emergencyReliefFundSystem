import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AccessibilityService } from '../../services/accessibility.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
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
    this.accessibilityService.announce('Registration page loaded', 'polite');
    
    this.registerForm = this.formBuilder.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^\d{3}-\d{3}-\d{4}$/)]],
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      role: ['USER', [Validators.required]] 
    });
  }

  get f() {
    return this.registerForm.controls;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
    const message = this.showPassword ? 'Password visible' : 'Password hidden';
    this.accessibilityService.announce(message, 'polite');
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      Object.keys(this.registerForm.controls).forEach(key => {
        this.registerForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.error = '';

    console.log('Attempting registration...', this.registerForm.value);

    // the 'role' value is now included in this.registerFrom.value and sent to the backend
    this.authService.register(this.registerForm.value).subscribe({
      next: (response) => {
        console.log('Registration successful!', response);
        this.accessibilityService.announce('Registration successful. Redirecting to dashboard.', 'polite');
        this.loading = false;
        
        // Navigate to dashboard
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 100);
      },
      error: (error) => {
        console.error('Registration error:', error);
        this.error = error.error?.message || 'Registration failed. Please try again.';
        this.loading = false;
        this.accessibilityService.announce('Registration failed: ' + this.error, 'assertive');
      }
    });
  }
}
