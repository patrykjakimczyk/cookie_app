import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppRoutingModule } from './routing/app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { MatButtonModule } from '@angular/material/button';
import { DashboardModule } from '../modules/dashboard/dashboard.module';
import { LoginFormModule } from '../modules/login-form/login-form.module';
import { RegistrationFormModule } from '../modules/registration-form/registration-form.module';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  declarations: [AppComponent, NavbarComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatButtonModule,
    HttpClientModule,

    AppRoutingModule,
    DashboardModule,
    LoginFormModule,
    RegistrationFormModule,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
