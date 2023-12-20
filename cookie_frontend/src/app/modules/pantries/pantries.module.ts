import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatMenuModule } from '@angular/material/menu';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

import { SharedModule } from 'src/app/shared/shared.module';
import { CreatePantryComponent } from './pantry/create-pantry/create-pantry.component';

import { PantryComponent } from './pantry/pantry.component';
import { PantriesListComponent } from './pantries-list.component';
import { PantryProductsListComponent } from './pantry/pantry-products-list/pantry-products-list.component';
import { PantryProductListElemComponent } from './pantry/pantry-products-list/pantry-product-list-elem/pantry-product-list-elem.component';
import { EditPantryProductComponent } from './pantry/pantry-products-list/pantry-product-list-elem/edit-pantry-product/edit-pantry-product.component';
import { PantryProductDetailsComponent } from './pantry/pantry-products-list/pantry-product-list-elem/pantry-product-details/pantry-product-details.component';
import { ReservePantryProductComponent } from './pantry/pantry-products-list/pantry-product-list-elem/reserve-pantry-product/reserve-pantry-product.component';

@NgModule({
  declarations: [
    PantryComponent,
    CreatePantryComponent,
    PantryProductsListComponent,
    PantryProductListElemComponent,
    EditPantryProductComponent,
    PantryProductDetailsComponent,
    ReservePantryProductComponent,
    PantriesListComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    RouterModule,
    ReactiveFormsModule,

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
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatMenuModule,
    MatAutocompleteModule,
  ],
  exports: [PantryComponent],
})
export class PantryModule {}
