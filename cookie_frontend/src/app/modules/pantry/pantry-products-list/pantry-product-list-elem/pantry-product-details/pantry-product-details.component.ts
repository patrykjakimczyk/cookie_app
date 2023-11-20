import { Component, Inject, OnInit } from '@angular/core';
import { PantryProductDTO } from '../../pantry-products-list.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Unit } from 'src/app/shared/model/enums/unit.enum';

@Component({
  selector: 'app-pantry-product-details',
  templateUrl: './pantry-product-details.component.html',
  styleUrls: ['./pantry-product-details.component.scss'],
})
export class PantryProductDetailsComponent implements OnInit {
  protected pantryProduct!: PantryProductDTO;

  constructor(
    public dialog: MatDialogRef<PantryProductDetailsComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PantryProductDTO
  ) {}

  ngOnInit(): void {
    this.pantryProduct = this.data;
  }

  printShortUnit() {
    if (this.pantryProduct.unit === Unit.GRAMS) {
      return 'g';
    } else if (this.pantryProduct.unit === Unit.MILLILITERS) {
      return 'ml';
    } else {
      return this.pantryProduct.quantity > 1 ? 'pcs' : 'pc';
    }
  }

  close() {
    this.dialog.close();
  }
}
