import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PantryProductDTO } from '../pantry-products-list.component';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { PantryService } from '../../pantry.service';
import { GetPantryResponse } from 'src/app/shared/model/responses/pantry-response';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import { MatDialog } from '@angular/material/dialog';
import { EditPantryProductComponent } from './edit-pantry-product/edit-pantry-product.component';
import { PantryProductDetailsComponent } from './pantry-product-details/pantry-product-details.component';
import {
  ReservePantryProductComponent,
  ReserveType,
} from './reserve-pantry-product/reserve-pantry-product.component';

export type CheckboxEvent = {
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
  @Output() checkboxEvent = new EventEmitter<CheckboxEvent>();
  @Output() reloadEvent = new EventEmitter<boolean>();
  protected units = units;

  constructor(private dialog: MatDialog) {}

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
      pantryProductId: this.pantryProduct.id,
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
      data: { pantryId: this.pantry.id, pantryProduct: this.pantryProduct },
    });

    editDialog.afterClosed().subscribe(() => this.reloadEvent.emit(true));
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
