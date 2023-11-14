import { Injectable } from '@angular/core';
import { User } from '../model/user';
import { getCookie, removeCookie } from 'typescript-cookie';
import { JwtHelperService } from '@auth0/angular-jwt';
import { BehaviorSubject } from 'rxjs';
import { LoginResponse } from '../model/responses/registration-response';

@Injectable({ providedIn: 'root' })
export class UserService {
  private jwtHelper = new JwtHelperService();
  user: BehaviorSubject<User>;

  constructor() {
    if (window.sessionStorage.getItem('user')) {
      const savedUser = JSON.parse(window.sessionStorage.getItem('user')!);
      this.user = new BehaviorSubject(savedUser);
    } else {
      this.user = new BehaviorSubject(new User());
    }
  }

  saveUser(user: User) {
    window.sessionStorage.setItem('user', JSON.stringify(user));
    this.user.next(user);
  }

  setUserAssignedPantry(assigned: boolean) {
    const user = this.user.getValue();
    user.assignedPantry = assigned;
    this.saveUser(user);
  }

  saveUserLoginData(jwt: string, loginResponse: LoginResponse) {
    const user = this.user.getValue();
    user.auth = true;
    user.username = loginResponse.username;
    user.password = '';
    user.assignedPantry = loginResponse.assignedPantry;
    this.extractUserDataFromJwt(user, jwt);

    let xsrf = getCookie('XSRF-TOKEN')!;
    window.sessionStorage.setItem('XSRF-TOKEN', xsrf);
    window.sessionStorage.setItem('JwtToken', jwt);
    window.sessionStorage.setItem('user', JSON.stringify(user));
    this.user.next(user);
  }

  logoutUser() {
    this.user.next(new User());
    window.sessionStorage.removeItem('user');
    window.sessionStorage.removeItem('JwtToken');
    window.sessionStorage.removeItem('XSRF-TOKEN');
    removeCookie('XSRF-TOKEN', { path: '/' });
  }

  private extractUserDataFromJwt(user: User, jwt: string) {
    let decodedJwt = this.jwtHelper.decodeToken(jwt)!;
    user.role = decodedJwt.role;
  }
}
