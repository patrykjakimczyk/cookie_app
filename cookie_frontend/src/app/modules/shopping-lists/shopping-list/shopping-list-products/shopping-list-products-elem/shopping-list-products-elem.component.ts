import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { AuthorityEnum } from 'src/app/shared/model/enums/authority.enum';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import {
  GetShoppingListResponse,
  ShoppingListProductDTO,
} from 'src/app/shared/model/types/shopping-lists-types';
import { UserService } from 'src/app/shared/services/user-service';
import { EditShoppingListProductsElemComponent } from './edit-shopping-list-products-elem/edit-shopping-list-products-elem.component';

export type ShoppingListProductCheckboxEvent = {
  checked: boolean;
  listProductId: number;
};

@Component({
  selector: 'app-shopping-list-products-elem',
  templateUrl: './shopping-list-products-elem.component.html',
  styleUrls: ['./shopping-list-products-elem.component.scss'],
})
export class ShoppingListProductsElemComponent {
  @Input() shoppingList!: GetShoppingListResponse;
  @Input() listProduct!: ShoppingListProductDTO;
  @Input() isShoppingListProduct!: boolean;
  @Output() checkboxEvent =
    new EventEmitter<ShoppingListProductCheckboxEvent>();
  @Output() reloadEvent = new EventEmitter<boolean>();
  protected units = units;
  protected authorityEnum = AuthorityEnum;

  constructor(private dialog: MatDialog, protected userService: UserService) {}

  printShortUnit(quantity: number) {
    if (this.listProduct.unit === Unit.GRAMS) {
      return 'g';
    } else if (this.listProduct.unit === Unit.MILLILITERS) {
      return 'ml';
    } else {
      return quantity > 1 ? 'pcs' : 'pc';
    }
  }

  checkboxClicked(event: MatCheckboxChange) {
    this.checkboxEvent.emit({
      checked: event.checked,
      listProductId: this.listProduct.id!,
    });
  }

  editButtonClicked() {
    const editDialog = this.dialog.open(EditShoppingListProductsElemComponent, {
      data: {
        listId: this.shoppingList.id,
        listProduct: this.listProduct,
        isShoppingListProduct: this.isShoppingListProduct,
      },
    });
    editDialog
      .afterClosed()
      .subscribe((modifiedListProduct: ShoppingListProductDTO) => {
        this.reloadEvent.emit(true);
        this.listProduct = modifiedListProduct;
      });
  }
}
