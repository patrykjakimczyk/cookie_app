import { NgModule } from '@angular/core';
import { RegistrationFormComponent } from './registration-form.component';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

@NgModule({
  declarations: [RegistrationFormComponent],
  imports: [BrowserModule, BrowserAnimationsModule],
  exports: [RegistrationFormComponent],
  providers: [],
})
export class RegistrationFormModule {}
