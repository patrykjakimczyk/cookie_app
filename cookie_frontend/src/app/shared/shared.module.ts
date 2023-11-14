import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

import { SuccessBannerComponent } from './components/success-banner/success-banner.component';
import { RouterModule } from '@angular/router';
import { DateFromStringPipe } from './pipes/date-from-string.pipe';
import { CategoryNameFormatterPipe } from './pipes/category-name-formatter.pipe';

@NgModule({
  declarations: [SuccessBannerComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,
    DateFromStringPipe,
    CategoryNameFormatterPipe,

    MatIconModule,
    MatButtonModule,
  ],
  exports: [
    SuccessBannerComponent,
    DateFromStringPipe,
    CategoryNameFormatterPipe,
  ],
})
export class SharedModule {}
