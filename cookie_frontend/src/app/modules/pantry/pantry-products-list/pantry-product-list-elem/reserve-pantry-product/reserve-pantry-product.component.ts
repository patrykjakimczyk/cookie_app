import { Component, Inject } from '@angular/core';
import { PantryService } from '../../../pantry.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import {
  PantryProductDTO,
  ReservePantryProductInfo,
  ReserveType,
} from 'src/app/shared/model/types/pantry-types';

@Component({
  selector: 'app-reserve-pantry-product',
  templateUrl: './reserve-pantry-product.component.html',
  styleUrls: ['./reserve-pantry-product.component.scss'],
})
export class ReservePantryProductComponent {
  private pantryId!: number;
  protected pantryProduct!: PantryProductDTO;
  protected reserveType!: ReserveType;
  protected reserveForm!: FormGroup;

  constructor(
    private pantryService: PantryService,
    private fb: FormBuilder,
    public dialog: MatDialogRef<ReservePantryProductComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ReservePantryProductInfo
  ) {}

  ngOnInit(): void {
    this.pantryId = this.data.pantryId;
    this.pantryProduct = this.data.pantryProduct;
    this.reserveType = this.data.reserve;
    this.reserveForm = this.fb.group({
      reserved: [
        1,
        [
          Validators.required,
          Validators.min(1),
          Validators.max(
            this.reserveType === 'RESERVE'
              ? this.pantryProduct.quantity
              : this.pantryProduct.reserved
          ),
          Validators.pattern('[0-9]+'),
        ],
      ],
    });
  }

  printReserveType() {
    return this.reserveType === 'RESERVE' ? 'Reserve' : 'Unreserve';
  }

  getErrorMessage(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('min')) {
      return 'Reserved quantity must be greater than 0';
    } else if (control.hasError('max')) {
      return `Reserved quantity cannot be greater than ${
        this.reserveType === 'RESERVE'
          ? this.pantryProduct.quantity
          : this.pantryProduct.reserved
      }`;
    } else if (control.hasError('pattern')) {
      return 'Reserved quantity must be a number';
    }

    return '';
  }

  close() {
    this.dialog.close(null);
  }

  submit() {
    if (!this.reserveForm.valid) {
      return;
    }

    let reservedCount;

    if (this.reserveType === 'RESERVE') {
      reservedCount = this.reserveForm.controls['reserved'].value;
    } else {
      reservedCount = this.reserveForm.controls['reserved'].value * -1;
    }

    console.log(reservedCount);

    this.pantryService
      .reservePantryProduct(
        this.pantryId,
        this.pantryProduct.id!,
        reservedCount
      )
      .subscribe({
        next: (response: PantryProductDTO) => {
          this.dialog.close(response);
        },
      });
  }
}
