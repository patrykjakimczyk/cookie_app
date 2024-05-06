import {
  maxPrepTimes,
  portions,
} from 'src/app/shared/model/constants/recipes.constants';
import {
  recipeSortColumnNames,
  sortDirections,
} from '../../../shared/model/enums/sort.enum';
import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Output,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
} from '@angular/forms';
import { GetRecipesParams } from 'src/app/shared/model/types/recipes-types';
import { MealType, mealTypes } from 'src/app/shared/model/enums/meal-type.enum';
import { MatCheckbox, MatCheckboxChange } from '@angular/material/checkbox';

@Component({
  selector: 'app-recipes-side-panel',
  templateUrl: './recipes-side-panel.component.html',
  styleUrls: ['./recipes-side-panel.component.scss'],
})
export class RecipesSidePanelComponent implements AfterViewInit {
  @ViewChildren(MatCheckbox)
  mealTypeCheckboxes!: QueryList<MatCheckbox>;
  @Output() filterRequest = new EventEmitter<GetRecipesParams>();
  protected sortColumnNames = recipeSortColumnNames;
  protected sortDirections = sortDirections;
  protected mealTypes = mealTypes;
  protected prepTimes = maxPrepTimes;
  protected portions = portions;
  protected filterForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.filterForm = this.createFilterForm();
  }

  ngAfterViewInit(): void {
    this.formSubmitted();
  }

  formSubmitted() {
    const params: GetRecipesParams = {
      filterValue: this.filterForm.controls['filterValue'].value,
      mealTypes: this.filterForm.controls['mealTypes'].value,
      prepTime: this.filterForm.controls['prepTime'].value,
      portions: this.filterForm.controls['portions'].value,
      sortColName: this.filterForm.controls['sortColName'].value,
      sortDirection: this.filterForm.controls['SortDirection'].value,
    };

    this.filterRequest.emit(params);
  }

  onMealTypeChange(event: MatCheckboxChange, value: MealType) {
    const formArray = this.filterForm.get('mealTypes') as FormArray;

    if (event.checked) {
      formArray.push(new FormControl(value));
    } else {
      let i = 0;

      formArray.controls.forEach((ctrl: AbstractControl) => {
        if (ctrl.value == value) {
          formArray.removeAt(i);
          return;
        }

        i++;
      });
    }
  }

  resetFilters() {
    this.filterForm = this.createFilterForm();
    this.mealTypeCheckboxes.forEach((checkbox) => {
      checkbox.checked = false;
    });
  }

  private createFilterForm() {
    return this.fb.group({
      filterValue: [''],
      mealTypes: new FormArray([]),
      prepTime: [0],
      portions: [0],
      sortColName: [''],
      SortDirection: [''],
    });
  }
}
