import { Component } from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { RegexConstants } from 'src/app/shared/model/regex-constants';

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss'],
})
export class LoginFormComponent {
  private regexes = RegexConstants;
  protected formSubmitted = false;
  protected form = this.fb.group({
    email: [''],
    password: [''],
  });

  constructor(private fb: FormBuilder) {}

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
    this.formSubmitted = true;
    this.setValidators();

    if (this.form.invalid) {
      return;
    }

    console.log(this.form);
  }

  private setValidators() {
    this.form.controls.email.setValidators([
      Validators.required,
      Validators.email,
    ]);
    this.form.controls.password.setValidators([
      Validators.required,
      Validators.pattern(this.regexes.passwordRegex),
    ]);
  }
}
