import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

const AUTH_URLS = ['/api/v1/auth/login', '/api/v1/auth/refresh'];

const isRefreshing$ = new BehaviorSubject<boolean>(false);

const withCredentials = <T>(req: Parameters<HttpInterceptorFn>[0]) =>
  req.clone({ withCredentials: true }) as typeof req;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const credentialedReq = withCredentials(req);

  return next(credentialedReq).pipe(
    catchError((error: unknown) => {
      const isUnauthorized = error instanceof HttpErrorResponse && error.status === 401;
      const isExcludedUrl = AUTH_URLS.some((url) => credentialedReq.url.includes(url));

      if (!isUnauthorized || isExcludedUrl) {
        return throwError(() => error);
      }

      return handleRefresh(credentialedReq, next, authService, router);
    }),
  );
};

const handleRefresh: (
  ...args: [...Parameters<HttpInterceptorFn>, AuthService, Router]
) => ReturnType<HttpInterceptorFn> = (req, next, authService, router) => {
  if (isRefreshing$.getValue()) {
    return isRefreshing$.pipe(
      filter((refreshing) => !refreshing),
      take(1),
      switchMap(() => next(req)),
    );
  }

  isRefreshing$.next(true);

  return authService.refresh().pipe(
    switchMap(() => {
      isRefreshing$.next(false);
      return next(req);
    }),
    catchError((refreshError: unknown) => {
      isRefreshing$.next(false);
      authService.clearAuthentication();
      router.navigate(['/login']);
      return throwError(() => refreshError);
    }),
  );
};
