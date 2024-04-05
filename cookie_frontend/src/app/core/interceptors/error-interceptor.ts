import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { excludedMethods } from './excluded-methods';
import { co, er } from '@fullcalendar/core/internal-common';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  constructor() {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const isExcluded = excludedMethods.some(
      (path) => req.url.includes(path.url) && req.method === path.method
    );

    return isExcluded
      ? next.handle(req)
      : next.handle(req).pipe(
          catchError((error: any) => {
            throw error;
          })
        );
  }
}
