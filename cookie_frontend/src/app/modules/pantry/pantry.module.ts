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
import { DeletePantryComponent } from './delete-pantry/delete-pantry.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { SharedModule } from 'src/app/shared/shared.module';
import { CreatePantryComponent } from './create-pantry/create-pantry.component';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { PantryProductsListComponent } from './pantry-products-list/pantry-products-list.component';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatPaginatorModule } from '@angular/material/paginator';

@NgModule({
  declarations: [
    PantryComponent,
    ChangePantryNameComponent,
    DeletePantryComponent,
    CreatePantryComponent,
    PantryProductsListComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    RouterModule,

    SharedModule,

    MatButtonModule,
    MatDividerModule,
    MatExpansionModule,
    MatCardModule,
    MatInputModule,
    MatDialogModule,
    MatSnackBarModule,
    MatIconModule,
    MatListModule,
    MatCheckboxModule,
    MatPaginatorModule,
  ],
  exports: [PantryComponent],
})
export class PantryModule {}
