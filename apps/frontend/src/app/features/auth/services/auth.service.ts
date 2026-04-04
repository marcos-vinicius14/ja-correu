import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, of, tap } from 'rxjs';

import { LoginRequest, RegisterRequest } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly #http = inject(HttpClient);
  readonly #authenticated = signal(false);

  readonly isAuthenticated = this.#authenticated.asReadonly();

  checkAuthentication() {
    return this.#http.get('/api/v1/auth/status').pipe(
      map(() => {
        this.#authenticated.set(true);
        return true;
      }),
      catchError(() => {
        this.#authenticated.set(false);
        return of(false);
      }),
    );
  }

  login(request: LoginRequest) {
    return this.#http
      .post<void>('/api/v1/auth/login', request)
      .pipe(tap(() => this.#authenticated.set(true)));
  }

  register(request: RegisterRequest) {
    return this.#http.post<void>('/api/v1/auth/register', request);
  }

  refresh() {
    return this.#http.post<void>('/api/v1/auth/refresh', null, {
      withCredentials: true,
    });
  }

  logout() {
    return this.#http
      .post<void>('/api/v1/auth/logout', null)
      .pipe(tap(() => this.#authenticated.set(false)));
  }

  clearAuthentication() {
    this.#authenticated.set(false);
  }
}
