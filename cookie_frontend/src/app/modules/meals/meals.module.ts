import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { MealsComponent } from './meals.component';
import { FullCalendarModule } from '@fullcalendar/angular';
import { MatCardModule } from '@angular/material/card';
import { SharedModule } from 'src/app/shared/shared.module';
import { EnumPrintFormatterPipe } from 'src/app/shared/pipes/enum-print-formatter.pipe';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@NgModule({
  declarations: [MealsComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    RouterModule,

    SharedModule,

    FullCalendarModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
  ],
  exports: [MealsComponent],
  providers: [EnumPrintFormatterPipe],
})
export class MealsModule {}
