import { NgModule } from '@angular/core';
import { RegistrationFormComponent } from './registration-form.component';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatDatepickerModule } from '@angular/material/datepicker';
import {
  ErrorStateMatcher,
  MatNativeDateModule,
  ShowOnDirtyErrorStateMatcher,
} from '@angular/material/core';
import { MatRadioModule } from '@angular/material/radio';

@NgModule({
  declarations: [RegistrationFormComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    RouterModule,

    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatRadioModule,
  ],
  exports: [RegistrationFormComponent],
  providers: [
    [[{ provide: ErrorStateMatcher, useClass: ShowOnDirtyErrorStateMatcher }]],
  ],
})
export class RegistrationFormModule {}
