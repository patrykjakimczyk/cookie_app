import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { UserService } from 'src/app/shared/services/user-service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit {
  protected username: string = '';
  protected isLogged: boolean = false;
  protected showMenuButtons = true;

  constructor(
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      let mealPlanning = params['mealPlanning'];

      if (mealPlanning !== null) {
        this.showMenuButtons = !mealPlanning;
      } else {
        this.showMenuButtons = true;
      }
    });

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
    this.router.navigate(['/']);
  }

  goToHome() {
    this.router.navigate(['/']);
  }
}
