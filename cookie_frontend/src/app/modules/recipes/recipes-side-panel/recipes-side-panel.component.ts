import {
  maxPrepTimes,
  portions,
} from 'src/app/shared/model/constants/recipes.constants';
import {
  recipeSortColumnNames,
  sortDirecitons,
} from './../../../shared/model/enums/sort-enum';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-recipes-side-panel',
  templateUrl: './recipes-side-panel.component.html',
  styleUrls: ['./recipes-side-panel.component.scss'],
})
export class RecipesSidePanelComponent implements OnInit {
  @Output() filterRequest = new EventEmitter<any>();
  protected sortColumnNames = recipeSortColumnNames;
  protected sortDirecitons = sortDirecitons;
  protected prepTimes = maxPrepTimes;
  protected portions = portions;
  protected filterForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.filterForm = this.createFilterForm();
  }

  ngOnInit(): void {}

  formSubmitted() {
    console.log(this.filterForm.value);
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
