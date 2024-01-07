import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { SharedModule } from 'src/app/shared/shared.module';
import { ShoppingListsComponent } from './shopping-lists.component';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { RouterModule } from '@angular/router';
import { ShoppingListComponent } from './shopping-list/shopping-list.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { CreateShoppingListComponent } from './shopping-list/create-shopping-list/create-shopping-list.component';
import { ShoppingListProductsComponent } from './shopping-list/shopping-list-products/shopping-list-products.component';
import { ShoppingListProductsElemComponent } from './shopping-list/shopping-list-products/shopping-list-products-elem/shopping-list-products-elem.component';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ReactiveFormsModule } from '@angular/forms';
import { MatListModule } from '@angular/material/list';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { EditShoppingListProductsElemComponent } from './shopping-list/shopping-list-products/shopping-list-products-elem/edit-shopping-list-products-elem/edit-shopping-list-products-elem.component';

@NgModule({
  declarations: [
    ShoppingListsComponent,
    ShoppingListComponent,
    CreateShoppingListComponent,
    ShoppingListProductsComponent,
    ShoppingListProductsElemComponent,
    EditShoppingListProductsElemComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,
    ReactiveFormsModule,

    SharedModule,

    MatButtonModule,
    MatCardModule,
    MatExpansionModule,
    MatSnackBarModule,
    MatInputModule,
    MatAutocompleteModule,
    MatListModule,
    MatCheckboxModule,
    MatPaginatorModule,
    MatSelectModule,
    MatMenuModule,
    MatIconModule,
  ],
  exports: [ShoppingListsComponent],
})
export class ShoppingListsModule {}
