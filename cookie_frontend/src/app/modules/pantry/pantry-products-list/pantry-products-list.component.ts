import { units } from './../../../shared/model/enums/unit.enum';
import {
  sortDirecitons,
  sortColumnNames,
} from './../../../shared/model/enums/sort-enum';
import { Component, Input } from '@angular/core';
import { GetPantryResponse } from 'src/app/shared/model/responses/pantry-response';
import { PantryService } from '../pantry.service';
import { PageEvent } from '@angular/material/paginator';
import { Observable, Subject, debounceTime, config } from 'rxjs';
import { AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { CheckboxEvent } from './pantry-product-list-elem/pantry-product-list-elem.component';
import { Unit } from 'src/app/shared/model/enums/unit.enum';
import { categories } from 'src/app/shared/model/enums/cateory-enum';

export interface PantryProductDTO {
  id: number | null;
  productName: string;
  category: string;
  quantity: number;
  unit: Unit;
  reserved: number;
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
  protected pantry?: GetPantryResponse;
  public readonly page_size = 20;
  public showAddProducts = false;
  public page = 0;
  public totalElements = 0;
  public currentElementsLength = 0;
  public products: PantryProductDTO[] = [];
  public productsToAdd: PantryProductDTO[] = [];
  public productsToAddIdsToRemove: number[] = [];
  public sortColumnNames = sortColumnNames;
  public sortDirecitons = sortDirecitons;
  public productsIdsToRemove: number[] = [];
  public units = units;
  public categories = categories;
  public addProduct = false;
  public sendProducts = false;

  protected addForm = this.fb.group({
    id: [0],
    productName: [
      '',
      [Validators.required, Validators.pattern('[a-zA-Z0-9., ]{3,50}')],
    ],
    category: ['', [Validators.required]],
    quantity: [
      '',
      [Validators.required, Validators.min(1), Validators.pattern('[0-9]+')],
    ],
    unit: ['', [Validators.required]],
    reserved: [0],
    purchaseDate: [''],
    expirationDate: [''],
    placement: ['', [Validators.pattern('[a-zA-Z ]*')]],
  });

  protected searchForm = this.fb.group({
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

  getErrorMessage(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('min')) {
      return 'Quantity must be greater than 0';
    } else if (control.hasError('pattern')) {
      return 'Field does not match required pattern';
    } else if (control.hasError('matDatepickerParse')) {
      return 'Date is incorrect';
    }

    return '';
  }

  pageChange(event: PageEvent) {
    this.page = event.pageIndex;
    this.getPantryProducts();
  }

  reloadPage() {
    this.page = 0;
    this.getPantryProducts();
  }

  productToAddCheckboxEvent(event: CheckboxEvent) {
    if (event.checked) {
      this.productsToAddIdsToRemove.push(event.pantryProductId);
    } else {
      this.productsToAddIdsToRemove = this.productsToAddIdsToRemove.filter(
        (currentProduct) => currentProduct !== event.pantryProductId
      );
    }
  }

  checkboxClicked(event: CheckboxEvent) {
    if (event.checked) {
      this.productsIdsToRemove.push(event.pantryProductId);
    } else {
      this.productsIdsToRemove = this.productsIdsToRemove.filter(
        (currentProduct) => currentProduct !== event.pantryProductId
      );
    }
  }

  submitAddForm() {
    if (!this.addForm.valid) {
      return;
    }

    setTimeout(() => {
      if (!this.addProduct) {
        this.addProduct = true;
        this.productsToAdd.push({
          id: this.productsToAdd.length,
          productName: this.addForm.controls.productName.value!,
          category: this.addForm.controls.category.value!,
          quantity: +this.addForm.controls.quantity.value!,
          unit:
            this.addForm.controls.unit.value === Unit.GRAMS
              ? Unit.GRAMS
              : this.addForm.controls.unit.value === Unit.MILLILITERS
              ? Unit.MILLILITERS
              : Unit.PIECES,
          reserved: this.addForm.controls.reserved.value!,
          purchaseDate: this.addForm.controls.purchaseDate.value!,
          expirationDate: this.addForm.controls.expirationDate.value!,
          placement: this.addForm.controls.placement.value!,
        });
        this.addForm.reset();
        Object.entries(this.addForm.controls).forEach((control) => {
          control[1].setErrors(null);
        });
      } else {
        this.addProduct = false;
      }
    }, 10);
  }

  sendProductsToAdd() {
    this.productsToAdd.forEach((product) => {
      product.id = null;
    });

    this.pantryService
      .addProductsToPantry(this.pantry!.id, this.productsToAdd)
      .subscribe({
        next: (response) => {
          this.productsToAdd = [];
          this.productsToAddIdsToRemove = [];
          this.page = 0;
          this.getPantryProducts();
        },
      });
  }

  closeAddProducts() {
    this.productsToAdd = [];
    this.showAddProducts = false;
  }

  submitSearchForm() {
    this.page = 0;
    this.getPantryProducts();
  }

  removeProductsFromAdding() {
    if (this.pantry && this.pantry.id && this.pantry.pantryName) {
      this.productsToAddIdsToRemove.forEach((id) => {
        this.productsToAdd = this.productsToAdd.filter(
          (pantryProductDTO) => pantryProductDTO.id !== id
        );
      });

      this.productsToAddIdsToRemove = [];
    }
  }

  removeProductsFromPantry() {
    if (this.pantry && this.pantry.id && this.pantry.pantryName) {
      this.pantryService
        .removeProductsFromPantry(this.pantry.id, this.productsIdsToRemove)
        .subscribe({
          next: (_) => {
            this.productsIdsToRemove = [];
            this.page = 0;
            this.getPantryProducts();
          },
        });
    }
  }

  private getPantryProducts() {
    const filterValue = this.searchForm.controls.filterValue.value!;
    const sortColName = this.searchForm.controls.sortColName.value!;
    const SortDirection = this.searchForm.controls.sortDirection.value!;

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
            console.log(response.content);
            this.products = response.content;
            this.totalElements = response.totalElements;
            this.currentElementsLength = response.content.length;
          },
        });
    }
  }
}
