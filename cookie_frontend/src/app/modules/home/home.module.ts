import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { HomeComponent } from './home.component';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [HomeComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,

    MatCardModule,
    MatButtonModule,
  ],
  exports: [HomeComponent],
  providers: [],
})
export class DashboardModule {}
