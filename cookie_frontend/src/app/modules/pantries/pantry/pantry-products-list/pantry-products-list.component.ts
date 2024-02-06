import {
  AuthorityEnum,
  authorityEnums,
} from './../../../../shared/model/enums/authority-enum';
import { Component, Input } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Observable, Subject, of } from 'rxjs';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  FormGroupDirective,
  NgForm,
  Validators,
} from '@angular/forms';

import { GetPantryResponse } from 'src/app/shared/model/responses/pantry-response';
import { PantryProductCheckboxEvent } from './pantry-product-list-elem/pantry-product-list-elem.component';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import { Category, categories } from 'src/app/shared/model/enums/category-enum';
import {
  pantrySortColumnNames,
  sortDirections,
} from 'src/app/shared/model/enums/sort-enum';
import { PantriesService } from '../../pantries.service';
import { UserService } from 'src/app/shared/services/user-service';
import { Router } from '@angular/router';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';

export type ProductDTO = {
  productName: string;
  category: Category;
};

export type PantryProductDTO = {
  id: number | null;
  productName: string;
  category: string;
  quantity: number;
  unit: Unit;
  reserved: number;
  purchaseDate: string;
  expirationDate: string;
  placement: string;
};
@Component({
  selector: 'app-pantry-products-list',
  templateUrl: './pantry-products-list.component.html',
  styleUrls: ['./pantry-products-list.component.scss'],
})
export class PantryProductsListComponent {
  @Input() pantry$!: Subject<GetPantryResponse>;
  protected pantry?: GetPantryResponse;
  protected authorityEnum = AuthorityEnum;
  public readonly page_size = 20;
  public showAddProducts = false;
  public page = 0;
  public productsToAddPage = 0;
  public totalElements = 0;
  public currentElementsLength = 0;
  public products: PantryProductDTO[] = [];
  public productsToAdd: PantryProductDTO[] = [];
  public productsToAddCurrPage: PantryProductDTO[] = [];
  public productsToAddIdsToRemove: number[] = [];
  public sortColumnNames = pantrySortColumnNames;
  public sortDirecitons = sortDirections;
  public productsIdsToRemove: number[] = [];
  public units = units;
  public categories = categories;
  public sendProducts = false;
  public filteredProducts = new Observable<ProductDTO[]>();

  protected addForm = this.fb.group({
    id: [0],
    productName: [
      '',
      [
        Validators.required,
        Validators.pattern(RegexConstants.productNameRegex),
      ],
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
    placement: ['', [Validators.pattern(RegexConstants.placementRegex)]],
  });

  protected searchForm = this.fb.group({
    filterValue: [''],
    sortColName: [''],
    sortDirection: [''],
  });

  constructor(
    private pantriesService: PantriesService,
    private router: Router,
    private fb: FormBuilder,
    protected userService: UserService
  ) {}

  ngOnInit(): void {
    this.pantry$.subscribe((pantry: GetPantryResponse) => {
      if (!pantry.id || !pantry.pantryName) {
        this.router.navigate(['/pantries']);
      }
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

  searchForProducts() {
    if (this.addForm.controls.productName.value) {
      this.pantriesService
        .getProductsWithFilter(this.addForm.controls.productName.value)
        .subscribe({
          next: (response) => {
            this.filteredProducts = of(response.content);
          },
        });
    }
  }

  selectCategoryForProduct(category: string) {
    this.addForm.controls.category.setValue(category);
  }

  productsToAddPageChange(event: PageEvent) {
    this.displayProductsToAddPage(event.pageIndex);
  }

  displayProductsToAddPage(pageNr: number) {
    this.productsToAddPage = pageNr;
    this.productsToAddCurrPage = this.productsToAdd.slice(
      pageNr * this.page_size,
      (pageNr + 1) * this.page_size
    );
  }

  pageChange(event: PageEvent) {
    this.page = event.pageIndex;
    this.getPantryProducts();
  }

  reloadPage() {
    this.page = 0;
    this.getPantryProducts();
  }

  productToAddCheckboxEvent(event: PantryProductCheckboxEvent) {
    if (event.checked) {
      this.productsToAddIdsToRemove.push(event.pantryProductId);
    } else {
      this.productsToAddIdsToRemove = this.productsToAddIdsToRemove.filter(
        (currentProduct) => currentProduct !== event.pantryProductId
      );
    }
  }

  checkboxClicked(event: PantryProductCheckboxEvent) {
    if (event.checked) {
      this.productsIdsToRemove.push(event.pantryProductId);
    } else {
      this.productsIdsToRemove = this.productsIdsToRemove.filter(
        (currentProduct) => currentProduct !== event.pantryProductId
      );
    }
  }

  submitAddForm(form: FormGroupDirective) {
    if (!this.addForm.valid) {
      return;
    }

    this.productsToAdd.push({
      id: this.productsToAdd.length,
      productName: this.addForm.controls.productName.value!,
      category: this.addForm.controls.category.value!,
      quantity: +this.addForm.controls.quantity.value!,
      unit: this.addForm.controls.unit.value as Unit,
      reserved: this.addForm.controls.reserved.value!,
      purchaseDate: this.addForm.controls.purchaseDate.value!,
      expirationDate: this.addForm.controls.expirationDate.value!,
      placement: this.addForm.controls.placement.value!,
    });
    form.resetForm(); // this combination of two resets allows to reset form without displaying form fields as invalid
    this.addForm.reset();
    this.displayProductsToAddPage(0);
  }

  sendProductsToAdd() {
    this.pantriesService
      .addProductsToPantry(this.pantry!.id, this.productsToAdd)
      .subscribe({
        next: (_) => {
          this.closeAddProducts();
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
      this.displayProductsToAddPage(0);
    }
  }

  removeProductsFromPantry() {
    if (this.pantry && this.pantry.id && this.pantry.pantryName) {
      this.pantriesService
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
      this.pantriesService
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
