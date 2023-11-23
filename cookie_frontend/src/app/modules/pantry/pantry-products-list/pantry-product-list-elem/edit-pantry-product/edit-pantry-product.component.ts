import { PantryService } from './../../../pantry.service';
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { PantryProductDTO } from '../../pantry-products-list.component';
import { units } from 'src/app/shared/model/enums/unit.enum';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';

export type EditPantryInfo = {
  pantryId: number;
  pantryProduct: PantryProductDTO;
  isPantryProduct: boolean;
};

@Component({
  selector: 'app-edit-pantry-product',
  templateUrl: './edit-pantry-product.component.html',
  styleUrls: ['./edit-pantry-product.component.scss'],
})
export class EditPantryProductComponent implements OnInit {
  private pantryId!: number;
  protected pantryProduct!: PantryProductDTO;
  protected isPantryProduct!: boolean;
  protected units = units;
  protected editForm!: FormGroup;

  constructor(
    private pantryService: PantryService,
    private fb: FormBuilder,
    public dialog: MatDialogRef<EditPantryProductComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditPantryInfo
  ) {}

  ngOnInit(): void {
    this.pantryId = this.data.pantryId;
    this.pantryProduct = this.data.pantryProduct;
    console.log(this.pantryProduct);
    this.isPantryProduct = this.data.isPantryProduct;
    this.editForm = this.fb.group({
      id: [this.pantryProduct.id],
      productName: [this.pantryProduct.productName],
      category: [this.pantryProduct.category],
      quantity: [
        this.pantryProduct.quantity,
        [Validators.required, Validators.min(1), Validators.pattern('[0-9]+')],
      ],
      unit: [this.pantryProduct.unit],
      purchaseDate: [this.pantryProduct.purchaseDate],
      expirationDate: [this.pantryProduct.expirationDate],
      placement: [this.pantryProduct.placement],
    });
  }

  getErrorMessage(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('min')) {
      return 'Quantity must be greater than 0';
    } else if (control.hasError('pattern')) {
      return 'Quantity must be a number';
    } else if (control.hasError('matDatepickerParse')) {
      return 'Date is incorrect';
    }

    return '';
  }

  close() {
    this.dialog.close();
  }

  submit() {
    if (!this.editForm.valid) {
      return;
    }

    if (this.isPantryProduct) {
      this.pantryService
        .modifyPantryProduct(this.pantryId, this.editForm.value)
        .subscribe({
          next: (_) => {
            this.dialog.close();
          },
        });
    } else {
      this.dialog.close(this.editForm.value);
    }
  }
}
