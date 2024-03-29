import { Injectable } from '@angular/core';
import { User } from '../model/user';
import { removeCookie } from 'typescript-cookie';
import { JwtHelperService } from '@auth0/angular-jwt';
import { BehaviorSubject } from 'rxjs';
import { LoginResponse } from '../model/responses/registration-responses';
import { AuthorityEnum } from '../model/enums/authority.enum';
import { AuthorityDTO } from '../model/types/user-types';

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

  isUserLogged(): boolean {
    return (
      this.user.getValue().email !== undefined && this.user.getValue().auth
    );
  }

  saveUser(user: User) {
    window.sessionStorage.setItem('user', JSON.stringify(user));
    this.user.next(user);
  }

  setUserAuthorities(authorities: AuthorityDTO[]) {
    const user = this.user.getValue();
    user.authorities = authorities;
    this.saveUser(user);
  }

  saveUserLoginData(jwt: string, loginResponse: LoginResponse) {
    const user = this.user.getValue();
    user.auth = true;
    user.username = loginResponse.username;
    user.password = '';
    user.assignedPantry = loginResponse.assignedPantry;
    this.extractUserDataFromJwt(user, jwt);

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

  userHasAuthority(requiredAuthority: AuthorityEnum): boolean {
    const user = this.user.getValue();

    for (let authority of user.authorities) {
      if (authority.authority === requiredAuthority) {
        return true;
      }
    }

    return false;
  }

  private extractUserDataFromJwt(user: User, jwt: string) {
    let decodedJwt = this.jwtHelper.decodeToken(jwt)!;
    user.role = decodedJwt.role;
  }
}
