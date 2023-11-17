import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PantryProductDTO } from '../pantry-products-list.component';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { PantryService } from '../../pantry.service';
import { GetPantryResponse } from 'src/app/shared/model/responses/pantry-response';

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
  protected editClicked = false;

  constructor(private pantryService: PantryService) {}

  checkboxClicked(event: MatCheckboxChange) {
    this.checkboxEvent.emit({
      checked: event.checked,
      pantryProductId: this.pantryProduct.id,
    });
  }

  editButtonClicked() {
    this.editClicked = true;
  }

  saveButtonClicked() {
    console.log(this.pantryProduct);
    this.pantryService
      .modifyPantryProduct(this.pantry.id, this.pantryProduct)
      .subscribe({
        next: (_) => {
          this.editClicked = false;
        },
      });
  }
}
