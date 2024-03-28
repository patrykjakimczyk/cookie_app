import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { PantriesService } from 'src/app/modules/pantries/pantries.service';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';
import { AddToListInfo } from 'src/app/shared/model/types/pantry-types';
import { ShoppingListProductDTO } from 'src/app/shared/model/types/shopping-lists-types';

@Component({
  selector: 'app-add-to-list',
  templateUrl: './add-to-list.component.html',
  styleUrls: ['./add-to-list.component.scss'],
})
export class AddToListComponent implements OnInit {
  @ViewChild('submitButton') submitButton!: HTMLButtonElement;
  protected units = units;
  protected groupDetails!: GroupDetailsDTO;

  protected addForm = this.fb.group({
    quantity: [
      '',
      [Validators.required, Validators.min(1), Validators.pattern('[0-9]+')],
    ],
    unit: ['', Validators.required],
    listId: [{ value: '', disabled: true }, Validators.required],
  });

  constructor(
    public dialog: MatDialogRef<AddToListComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AddToListInfo,
    private fb: FormBuilder,
    private pantriesService: PantriesService
  ) {}

  ngOnInit(): void {
    this.pantriesService
      .getGroup(this.data.pantry.groupId)
      .subscribe((response: GroupDetailsDTO) => {
        this.groupDetails = response;
        this.addForm.controls['listId'].enable();
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
    this.dialog.close(false);
  }

  submit() {
    if (!this.addForm.valid) {
      return;
    }

    const productsToAdd: ShoppingListProductDTO[] = [
      {
        id: 0,
        product: {
          productId: 0,
          productName: this.data.pantryProduct.product.productName,
          category: this.data.pantryProduct.product.category,
        },
        quantity: +this.addForm.controls.quantity.value!,
        unit: this.addForm.controls.unit.value! as Unit,
        purchased: false,
      },
    ];

    this.pantriesService
      .addProductsToShoppingList(
        +this.addForm.controls.listId.value!,
        productsToAdd
      )
      .subscribe((_) => {
        this.dialog.close(true);
      });
  }
}
