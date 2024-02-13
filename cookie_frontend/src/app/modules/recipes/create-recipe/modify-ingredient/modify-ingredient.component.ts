import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import { RecipeProductDTO } from 'src/app/shared/model/types/recipes-types';

@Component({
  selector: 'app-modify-ingredient',
  templateUrl: './modify-ingredient.component.html',
  styleUrls: ['./modify-ingredient.component.scss'],
})
export class ModifyIngredientComponent implements OnInit {
  protected units = units;
  protected editForm = this.fb.group({
    quantity: [
      '',
      [Validators.required, Validators.min(1), Validators.pattern('[-0-9]+')],
    ],
    unit: ['', [Validators.required]],
  });

  constructor(
    private fb: FormBuilder,
    public dialog: MatDialogRef<ModifyIngredientComponent>,
    @Inject(MAT_DIALOG_DATA) public ingredient: RecipeProductDTO
  ) {}

  ngOnInit(): void {
    this.editForm.controls.quantity.setValue(
      this.ingredient.quantity.toString()
    );
    this.editForm.controls.unit.setValue(this.ingredient.unit);
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

    this.ingredient.quantity = +this.editForm.controls.quantity.value!;
    this.ingredient.unit = this.editForm.controls.unit.value as Unit;

    this.dialog.close(this.ingredient);
  }
}
