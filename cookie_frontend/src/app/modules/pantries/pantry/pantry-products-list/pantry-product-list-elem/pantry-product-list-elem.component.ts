import {
  AuthorityEnum,
  authorityEnums,
} from '../../../../../shared/model/enums/authority.enum';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';

import { EditPantryProductComponent } from './edit-pantry-product/edit-pantry-product.component';
import { GetPantryResponse } from 'src/app/shared/model/responses/pantry-response';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import { PantryProductDetailsComponent } from './pantry-product-details/pantry-product-details.component';
import { ReservePantryProductComponent } from './reserve-pantry-product/reserve-pantry-product.component';
import {
  PantryProductDTO,
  ReserveType,
} from 'src/app/shared/model/types/pantry-types';
import { UserService } from 'src/app/shared/services/user-service';

export type PantryProductCheckboxEvent = {
  checked: boolean;
  pantryProductId: number;
};

@Component({
  selector: 'app-pantry-product-list-elem',
  templateUrl: './pantry-product-list-elem.component.html',
  styleUrls: ['./pantry-product-list-elem.component.scss'],
})
export class PantryProductListElemComponent {
  @Input() pantry!: GetPantryResponse;
  @Input() pantryProduct!: PantryProductDTO;
  @Input() isPantryProduct!: boolean;
  @Output() checkboxEvent = new EventEmitter<PantryProductCheckboxEvent>();
  @Output() reloadEvent = new EventEmitter<boolean>();
  @Output() pantryProductChange = new EventEmitter<PantryProductDTO>();
  protected units = units;
  protected authorityEnum = AuthorityEnum;

  constructor(private dialog: MatDialog, protected userService: UserService) {}

  printShortUnit(quantity: number) {
    if (this.pantryProduct.unit === Unit.GRAMS) {
      return 'g';
    } else if (this.pantryProduct.unit === Unit.MILLILITERS) {
      return 'ml';
    } else {
      return quantity > 1 ? 'pcs' : 'pc';
    }
  }

  checkboxClicked(event: MatCheckboxChange) {
    this.checkboxEvent.emit({
      checked: event.checked,
      pantryProductId: this.pantryProduct.id!,
    });
  }

  reserveButtonClicked() {
    this.showReservePopup('RESERVE');
  }

  unreserveButtonClicked() {
    this.showReservePopup('UNRESERVE');
  }

  showDetailsButtonClicked() {
    this.dialog.open(PantryProductDetailsComponent, {
      data: this.pantryProduct,
    });
  }

  editButtonClicked() {
    const editDialog = this.dialog.open(EditPantryProductComponent, {
      data: {
        pantryId: this.pantry.id,
        pantryProduct: this.pantryProduct,
        isPantryProduct: this.isPantryProduct,
      },
    });

    editDialog
      .afterClosed()
      .subscribe((modifiedPantryProduct: PantryProductDTO) => {
        this.reloadEvent.emit(true);
        this.pantryProduct = modifiedPantryProduct;
        this.pantryProductChange.emit(this.pantryProduct);
        console.log(this.pantryProduct);
      });
  }

  private showReservePopup(reserveType: ReserveType) {
    const reserveDialog = this.dialog.open(ReservePantryProductComponent, {
      data: {
        pantryId: this.pantry.id,
        pantryProduct: this.pantryProduct,
        reserve: reserveType,
      },
    });

    reserveDialog.afterClosed().subscribe((result: PantryProductDTO) => {
      if (result) {
        this.pantryProduct = result;
      }
    });
  }
}
