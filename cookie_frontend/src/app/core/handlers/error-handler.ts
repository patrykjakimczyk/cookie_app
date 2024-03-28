import { ErrorHandler, Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable()
export class CookieErrorHandler implements ErrorHandler {
  constructor(private router: Router) {}

  handleError(error: any): void {
    this.router.navigate(['/error']);
  }
}
