import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler, Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable()
export class CookieErrorHandler implements ErrorHandler {
  constructor(private router: Router) {}

  handleError(error: any): void {
    if (error instanceof HttpErrorResponse) {
      this.router.navigate(['/error']);
    }

    console.error(error);
  }
}
