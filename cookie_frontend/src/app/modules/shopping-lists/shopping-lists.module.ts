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

@NgModule({
  declarations: [ShoppingListsComponent, ShoppingListComponent, CreateShoppingListComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,

    SharedModule,

    MatButtonModule,
    MatCardModule,
    MatExpansionModule,
    MatSnackBarModule,
  ],
  exports: [ShoppingListsComponent],
})
export class ShoppingListsModule {}
