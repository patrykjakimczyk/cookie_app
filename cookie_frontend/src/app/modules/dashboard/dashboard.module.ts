import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { DashboardComponent } from './dashboard.component';

@NgModule({
  declarations: [DashboardComponent],
  imports: [BrowserModule, BrowserAnimationsModule],
  exports: [DashboardComponent],
  providers: [],
})
export class DashboardModule {}
