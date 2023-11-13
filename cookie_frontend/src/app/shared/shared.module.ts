import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

import { SuccessBannerComponent } from './components/success-banner/success-banner.component';
import { RouterModule } from '@angular/router';
import { DateFromStringPipe } from './pipes/date-from-string.pipe';
import { FirstCapitalPipe } from './pipes/first-capital.pipe';

@NgModule({
  declarations: [SuccessBannerComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,
    DateFromStringPipe,
    FirstCapitalPipe,

    MatIconModule,
    MatButtonModule,
  ],
  exports: [SuccessBannerComponent, DateFromStringPipe, FirstCapitalPipe],
})
export class SharedModule {}
