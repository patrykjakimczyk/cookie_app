import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PantryComponent } from './pantry.component';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { ChangePantryNameComponent } from './change-pantry-name/change-pantry-name.component';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [PantryComponent, ChangePantryNameComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,

    MatButtonModule,
    MatDividerModule,
    MatExpansionModule,
    MatCardModule,
    MatInputModule,
    MatDialogModule,
  ],
  exports: [PantryComponent],
})
export class PantryModule {}
