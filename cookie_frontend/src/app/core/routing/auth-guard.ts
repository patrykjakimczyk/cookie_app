import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';
import { UserService } from 'src/app/shared/services/user-service';

@Injectable({ providedIn: 'root' })
export class AuthGuard {
  jwtHelper: JwtHelperService;

  constructor(private router: Router, private userService: UserService) {
    this.jwtHelper = new JwtHelperService();
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const user = this.userService.user.getValue();
    const jwt = sessionStorage.getItem('JwtToken');

    if (state.url === '/' && user.auth && !this.jwtHelper.isTokenExpired(jwt)) {
      this.router.navigate(['/meals']);
      return true;
    }

    if (user.auth) {
      if (this.jwtHelper.isTokenExpired(jwt)) {
        this.userService.logoutUser();
        this.router.navigate(['/']);

        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }
}
