import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { MealsComponent } from './meals.component';
import { FullCalendarModule } from '@fullcalendar/angular';
import { MatCardModule } from '@angular/material/card';

@NgModule({
  declarations: [MealsComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    RouterModule,

    FullCalendarModule,
    MatCardModule,
  ],
  exports: [MealsComponent],
})
export class MealsModule {}
