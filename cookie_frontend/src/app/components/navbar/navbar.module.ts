import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';

import { NavbarComponent } from './navbar.component';
import { AppRoutingModule } from 'src/app/routing/app-routing.module';


@NgModule({
  declarations: [
    NavbarComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,

    MatButtonModule
  ],
  exports: [NavbarComponent],
  providers: [],
})
export class NavbarModule { }
