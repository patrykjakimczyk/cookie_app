import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';

import { units } from 'src/app/shared/model/enums/unit.enum';
import {
  EditPantryInfo,
  PantryProductDTO,
} from 'src/app/shared/model/types/pantry-types';
import { PantriesService } from 'src/app/modules/pantries/pantries.service';

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
    private pantriesService: PantriesService,
    private fb: FormBuilder,
    public dialog: MatDialogRef<EditPantryProductComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditPantryInfo
  ) {}

  ngOnInit(): void {
    this.pantryId = this.data.pantryId;
    this.pantryProduct = this.data.pantryProduct;
    this.isPantryProduct = this.data.isPantryProduct;
    this.editForm = this.fb.group({
      id: [this.pantryProduct.id],
      product: this.fb.group({
        productId: [this.pantryProduct.product.productId],
        productName: [this.pantryProduct.product.productName],
        category: [this.pantryProduct.product.category],
      }),
      quantity: [
        this.pantryProduct.quantity,
        [Validators.required, Validators.min(1), Validators.pattern('[0-9]+')],
      ],
      unit: [this.pantryProduct.unit],
      reserved: [this.pantryProduct.reserved],
      purchaseDate: [
        this.stringToDateTransform(this.pantryProduct.purchaseDate),
      ],
      expirationDate: [
        this.stringToDateTransform(this.pantryProduct.expirationDate),
      ],
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

    this.editForm.controls['purchaseDate'].setValue(
      this.editForm.controls['purchaseDate'].value
        ? new Date(
            this.editForm.controls['purchaseDate'].value
          ).toLocaleDateString('en-GB')
        : ''
    );

    this.editForm.controls['expirationDate'].setValue(
      this.editForm.controls['expirationDate'].value
        ? new Date(
            this.editForm.controls['expirationDate'].value
          ).toLocaleDateString('en-GB')
        : ''
    );

    if (this.isPantryProduct) {
      this.pantriesService
        .modifyPantryProduct(this.pantryId, this.editForm.value)
        .subscribe((_) => {
          this.dialog.close();
        });
    } else {
      this.dialog.close(this.editForm.value);
    }
  }

  private stringToDateTransform(value: string | null | undefined): Date | null {
    if (!value) {
      return null;
    }

    return new Date(
      +value.substring(6, 10),
      +value.substring(3, 5) - 1,
      +value.substring(0, 2)
    );
  }
}
