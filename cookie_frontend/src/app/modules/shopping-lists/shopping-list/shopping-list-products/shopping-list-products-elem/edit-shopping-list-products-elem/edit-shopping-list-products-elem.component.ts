import { Component, Inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ShoppingListsService } from 'src/app/modules/shopping-lists/shopping-lists.service';
import { units } from 'src/app/shared/model/enums/unit.enum';
import {
  EditShoppingListProductInfo,
  ShoppingListProductDTO,
} from 'src/app/shared/model/types/shopping-lists-types';

@Component({
  selector: 'app-edit-shopping-list-products-elem',
  templateUrl: './edit-shopping-list-products-elem.component.html',
  styleUrls: ['./edit-shopping-list-products-elem.component.scss'],
})
export class EditShoppingListProductsElemComponent {
  private listId!: number;
  protected listProduct!: ShoppingListProductDTO;
  protected isShoppingListProduct!: boolean;
  protected units = units;
  protected editForm!: FormGroup;

  constructor(
    private shoppingListService: ShoppingListsService,
    private fb: FormBuilder,
    public dialog: MatDialogRef<EditShoppingListProductsElemComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditShoppingListProductInfo
  ) {}

  ngOnInit(): void {
    this.listId = this.data.listId;
    this.listProduct = this.data.listProduct;
    this.isShoppingListProduct = this.data.isShoppingListProduct;
    this.editForm = this.fb.group({
      id: [this.listProduct.id],
      product: this.fb.control({
        productId: this.listProduct.product.productId,
        productName: this.listProduct.product.productName,
        category: this.listProduct.product.category,
      }),
      quantity: [
        this.listProduct.quantity,
        [Validators.required, Validators.min(1), Validators.pattern('[0-9]+')],
      ],
      unit: [this.listProduct.unit],
      purchased: [this.listProduct.purchased],
    });
  }

  getErrorMessage(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('min')) {
      return 'Quantity must be greater than 0';
    } else if (control.hasError('pattern')) {
      return 'Quantity must be a number';
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

    if (this.isShoppingListProduct) {
      console.log(this.editForm.value);
      this.shoppingListService
        .updateShoppingListProduct(this.listId, this.editForm.value)
        .subscribe({
          next: (_) => {
            this.dialog.close(this.editForm.value);
          },
        });
    } else {
      this.dialog.close(this.editForm.value);
    }
  }
}
