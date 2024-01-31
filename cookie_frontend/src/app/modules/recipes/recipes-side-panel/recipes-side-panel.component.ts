import {
  maxPrepTimes,
  portions,
} from 'src/app/shared/model/constants/recipes.constants';
import {
  recipeSortColumnNames,
  sortDirections,
} from './../../../shared/model/enums/sort-enum';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { GetRecipesParams } from 'src/app/shared/model/types/recipes-types';

@Component({
  selector: 'app-recipes-side-panel',
  templateUrl: './recipes-side-panel.component.html',
  styleUrls: ['./recipes-side-panel.component.scss'],
})
export class RecipesSidePanelComponent implements OnInit {
  @Output() filterRequest = new EventEmitter<GetRecipesParams>();
  protected sortColumnNames = recipeSortColumnNames;
  protected sortDirections = sortDirections;
  protected prepTimes = maxPrepTimes;
  protected portions = portions;
  protected filterForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.filterForm = this.createFilterForm();
  }

  ngOnInit(): void {}

  formSubmitted() {
    const params: GetRecipesParams = {
      filterValue: this.filterForm.controls['filterValue'].value!,
      prepTime: this.filterForm.controls['prepTime'].value!,
      portions: this.filterForm.controls['portions'].value!,
      sortColName: this.filterForm.controls['sortColName'].value!,
      sortDirection: this.filterForm.controls['SortDirection'].value!,
    };

    this.filterRequest.emit(params);
  }

  resetFilters() {
    this.filterForm = this.createFilterForm();
  }

  private createFilterForm() {
    return this.fb.group({
      filterValue: [''],
      prepTime: [0],
      portions: [0],
      sortColName: [''],
      SortDirection: [''],
    });
  }
}
