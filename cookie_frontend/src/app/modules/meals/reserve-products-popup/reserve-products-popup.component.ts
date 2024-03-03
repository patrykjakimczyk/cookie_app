import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-reserve-products-popup',
  templateUrl: './reserve-products-popup.component.html',
  styleUrls: ['./reserve-products-popup.component.scss'],
})
export class ReserveProductsPopupComponent {
  constructor(public dialog: MatDialogRef<ReserveProductsPopupComponent>) {}

  close() {
    this.dialog.close(false);
  }

  reserve() {
    this.dialog.close(true);
  }
}
