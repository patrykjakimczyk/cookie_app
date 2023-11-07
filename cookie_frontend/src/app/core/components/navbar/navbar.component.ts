import { Component, OnInit } from '@angular/core';
import { LoginFormService } from 'src/app/modules/login-form/login-form.service';

import { UserService } from 'src/app/shared/services/user-service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit {
  protected username: string = '';
  protected isLogged: boolean = false;

  constructor(
    private userService: UserService,
    private loginService: LoginFormService
  ) {}

  ngOnInit(): void {
    this.userService.user.subscribe((user) => {
      if (user.auth && user.username) {
        this.username = user.username;
        this.isLogged = user.auth;
      } else {
        this.username = '';
        this.isLogged = false;
      }
    });
  }

  logout() {
    this.userService.logoutUser();
  }
}
