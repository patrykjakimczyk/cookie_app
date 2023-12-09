import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { SuccessBannerComponent } from './components/success-banner/success-banner.component';
import { RouterModule } from '@angular/router';
import { DateFromStringPipe } from './pipes/date-from-string.pipe';
import { EnumPrintFormatterPipe } from './pipes/enum-print-formatter.pipe';
import { ValueOrDashPipe } from './pipes/value-or-dash.pipe';
import { PreviewCardComponent } from './components/preview-card/preview-card.component';
import { MatCardModule } from '@angular/material/card';
import { InsertNameComponent } from './components/insert-name/insert-name.component';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';

@NgModule({
  declarations: [
    SuccessBannerComponent,
    PreviewCardComponent,
    InsertNameComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,

    RouterModule,
    DateFromStringPipe,
    EnumPrintFormatterPipe,
    ValueOrDashPipe,

    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
  ],
  exports: [
    SuccessBannerComponent,
    PreviewCardComponent,
    InsertNameComponent,
    DateFromStringPipe,
    EnumPrintFormatterPipe,
    ValueOrDashPipe,
  ],
})
export class SharedModule {}
