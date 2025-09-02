export interface User {
  id?: number;
  username: string;
  fullName: string;
  email: string;
  phone?: string;
  role?: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  fullName: string;
  email: string;
  role: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  email: string;
  phone: string;
}
