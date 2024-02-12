import { Component } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  Validators,
} from '@angular/forms';

import { genders } from 'src/app/shared/model/enums/gender.enum';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { RegistrationFormService } from './registration-form.service';

@Component({
  selector: 'app-registration-form',
  templateUrl: './registration-form.component.html',
  styleUrls: ['./registration-form.component.scss'],
})
export class RegistrationFormComponent {
  readonly minBirthDate = new Date(new Date().getTime() - 3944619000000);
  readonly maxBirthDate = new Date(new Date().getTime() - 410240038000);

  private regexes = RegexConstants;
  protected genders = genders;
  protected registrationSucceded = false;
  protected formSubmitted = false;

  protected form = this.fb.group({
    username: [
      '',
      [Validators.required, Validators.pattern(this.regexes.usernameRegex)],
    ],
    email: ['', [Validators.required, Validators.email]],
    password: [
      '',
      [Validators.required, Validators.pattern(this.regexes.passwordRegex)],
    ],
    confirmPassword: [
      '',
      [Validators.required, Validators.pattern(this.regexes.passwordRegex)],
    ],
    birthDate: ['', [this.invalidDateValidation]],
    gender: [null],
  });

  constructor(
    private fb: FormBuilder,
    private service: RegistrationFormService
  ) {}

  getErrorMessage(formControl: FormControl): string {
    if (formControl.hasError('required')) {
      return 'Field is required';
    } else if (formControl.hasError('email')) {
      return 'Incorrect email address format';
    } else if (formControl.hasError('pattern')) {
      return 'Value does not follow required pattern';
    } else if (formControl.hasError('variousPasswords')) {
      return 'Passwords are different';
    } else if (formControl.hasError('matDatepickerMax')) {
      return 'You must be at least 13 years old';
    } else if (formControl.hasError('invalidDate')) {
      return 'Birth date is empty or incorrect';
    } else if (formControl.hasError('usernameTaken')) {
      return 'Provided username is already taken';
    } else if (formControl.hasError('emailTaken')) {
      return 'Provided e-mail is already taken';
    }

    return '';
  }

  submit() {
    this.formSubmitted = true;
    this.form.controls.birthDate.updateValueAndValidity(); //added this method call, because validation would't run without inserting any data into input

    if (
      this.form.controls.password.value !==
      this.form.controls.confirmPassword.value
    ) {
      this.form.controls.confirmPassword.setErrors({
        variousPasswords: 'true',
      });
      return;
    }

    if (this.form.invalid) {
      return;
    }

    this.service.register(this.form.value).subscribe({
      next: (response) => {
        if (response.length === 0) {
          this.registrationSucceded = true;
          return;
        }

        if (response.includes('username')) {
          this.form.controls.username.setErrors({ usernameTaken: true });
        }

        if (response.includes('email')) {
          this.form.controls.email.setErrors({ emailTaken: true });
        }
      },
      error: (error) => {
        console.log(error);
      },
    });
  }

  invalidDateValidation(control: AbstractControl) {
    if (!control.value) {
      return { invalidDate: true };
    }
    return null;
  }
}
