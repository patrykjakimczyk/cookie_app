import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { LoginFormService } from './login-form.service';
import { UserService } from 'src/app/shared/services/user-service';
import { User } from 'src/app/shared/model/user';

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss'],
})
export class LoginFormComponent {
  private regexes = RegexConstants;
  protected authenticationFailed = false;
  protected form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: [
      '',
      [Validators.required, Validators.pattern(this.regexes.passwordRegex)],
    ],
  });

  constructor(
    private fb: FormBuilder,
    private loginService: LoginFormService,
    private userService: UserService,
    private router: Router
  ) {}

  getErrorMessage(formControl: FormControl) {
    if (formControl.hasError('required')) {
      return 'Field is required';
    } else if (formControl.hasError('email')) {
      return 'Incorrect email address format';
    } else if (formControl.hasError('pattern')) {
      return 'Password does not follow a required pattern';
    }

    return '';
  }

  submit() {
    this.authenticationFailed = false;
    this.form.markAsPristine();

    if (this.form.invalid) {
      return;
    }

    let user = new User();
    if (this.form.value.email) {
      user.email = this.form.value!.email;
    }

    if (this.form.value.password) {
      user.password = this.form.value.password;
    }

    this.userService.saveUser(user);
    this.loginService.login().subscribe({
      next: (response: any) => {
        this.authenticationFailed = false;
        const jwtToken = response.headers.get('Authorization')!;
        this.userService.saveUserLoginData(jwtToken, response.body);
        this.router.navigate(['/']);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.userService.logoutUser();
          this.authenticationFailed = true;
        }
      },
    });
  }
}
