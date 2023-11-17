import {
  sortDirecitons,
  sortColumnNames,
} from './../../../shared/model/enums/sort-enum';
import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { GetPantryResponse } from 'src/app/shared/model/responses/pantry-response';
import { PantryService } from '../pantry.service';
import { PageEvent } from '@angular/material/paginator';
import { Subject } from 'rxjs';
import { FormBuilder } from '@angular/forms';
import { MatCheckbox, MatCheckboxChange } from '@angular/material/checkbox';
import { CheckboxEvent } from './pantry-product-list-elem/pantry-product-list-elem.component';

export interface PantryProductDTO {
  id: number;
  productName: string;
  category: string;
  quantity: string;
  purchaseDate: string;
  expirationDate: string;
  placement: string;
}

@Component({
  selector: 'app-pantry-products-list',
  templateUrl: './pantry-products-list.component.html',
  styleUrls: ['./pantry-products-list.component.scss'],
})
export class PantryProductsListComponent {
  @Input() pantry$!: Subject<GetPantryResponse>;
  @ViewChild('removeButton', { read: ElementRef }) removeButton!: ElementRef;
  protected pantry?: GetPantryResponse;
  public readonly page_size = 20;
  public page = 0;
  public totalElements = 0;
  public currentElementsLength = 0;
  public products: PantryProductDTO[] = [];
  public sortColumnNames = sortColumnNames;
  public sortDirecitons = sortDirecitons;
  public productsIdsToRemove: number[] = [];

  protected form = this.fb.group({
    filterValue: [''],
    sortColName: [''],
    sortDirection: [''],
  });

  constructor(private pantryService: PantryService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.pantry$.subscribe((pantry: GetPantryResponse) => {
      this.pantry = pantry;
      this.getPantryProducts();
    });
  }

  pageChange(event: PageEvent) {
    this.page = event.pageIndex;
    this.getPantryProducts();
  }

  checkboxClicked(event: CheckboxEvent) {
    if (event.checked) {
      this.productsIdsToRemove.push(event.pantryProductId);
    } else {
      this.productsIdsToRemove = this.productsIdsToRemove.filter(
        (currentProduct) => currentProduct !== event.pantryProductId
      );
    }

    if (this.productsIdsToRemove.length > 0) {
      this.removeButton.nativeElement.disabled = false;
    } else {
      this.removeButton.nativeElement.disabled = true;
    }
  }

  submit() {
    this.page = 0;
    this.getPantryProducts();
  }

  removeProductsFromPantry() {
    if (this.pantry && this.pantry.id && this.pantry.pantryName) {
      this.pantryService
        .removeProductsFromPantry(this.pantry.id, this.productsIdsToRemove)
        .subscribe({
          next: (_) => {
            this.productsIdsToRemove = [];
            this.removeButton.nativeElement.disabled = true;
            this.page = 0;
            this.getPantryProducts();
          },
        });
    }
  }

  private getPantryProducts() {
    const filterValue = this.form.controls.filterValue.value!;
    const sortColName = this.form.controls.sortColName.value!;
    const SortDirection = this.form.controls.sortDirection.value!;

    if (this.pantry && this.pantry.id && this.pantry.pantryName) {
      this.pantryService
        .getPantryProducts(
          this.pantry.id,
          this.page,
          filterValue,
          sortColName,
          SortDirection
        )
        .subscribe({
          next: (response) => {
            this.products = response.content;
            this.totalElements = response.totalElements;
            this.currentElementsLength = response.content.length;
          },
        });
    }
  }
}
