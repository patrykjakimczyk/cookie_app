import { ShoppingListProductCheckboxEvent } from './shopping-list-products-elem/shopping-list-products-elem.component';
import { Component, Input, OnInit } from '@angular/core';
import { Observable, Subject, of } from 'rxjs';
import {
  GetShoppingListResponse,
  ShoppingListProductDTO,
} from 'src/app/shared/model/types/shopping-lists-types';
import { ShoppingListsService } from '../../shopping-lists.service';
import { Router } from '@angular/router';
import {
  AbstractControl,
  FormBuilder,
  FormGroupDirective,
  Validators,
} from '@angular/forms';
import {
  shoppingListSortColumnNames,
  sortDirections,
} from 'src/app/shared/model/enums/sort.enum';
import { UserService } from 'src/app/shared/services/user-service';
import { AuthorityEnum } from 'src/app/shared/model/enums/authority.enum';
import { PageEvent } from '@angular/material/paginator';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import { Category, categories } from 'src/app/shared/model/enums/category.enum';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationPopupComponent } from 'src/app/shared/components/confirmation-popup/confirmation-popup.component';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { ProductDTO } from 'src/app/shared/model/types/product-types';

@Component({
  selector: 'app-shopping-list-products',
  templateUrl: './shopping-list-products.component.html',
  styleUrls: ['./shopping-list-products.component.scss'],
})
export class ShoppingListProductsComponent implements OnInit {
  @Input() shoppingList$!: Subject<GetShoppingListResponse>;
  protected shoppingList?: GetShoppingListResponse;
  protected authorityEnum = AuthorityEnum;
  public readonly page_size = 20;
  public page = 0;
  public productsToAddPage = 0;
  public totalElements = 0;
  public currentElementsLength = 0;
  public products: ShoppingListProductDTO[] = [];
  public showAddProducts = false;
  public sortColumnNames = shoppingListSortColumnNames;
  public sortDirecitons = sortDirections;
  public productsIdsForAction: number[] = [];
  public productsToAdd: ShoppingListProductDTO[] = [];
  public productsToAddIdsToRemove: number[] = [];
  public productsToAddCurrPage: ShoppingListProductDTO[] = [];
  public filteredProducts = new Observable<ProductDTO[]>();
  public categories = categories;
  public units = units;

  protected searchForm = this.fb.group({
    filterValue: [''],
    sortColName: [''],
    sortDirection: [''],
  });

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
    purchased: [false],
  });

  constructor(
    private shoppingListsService: ShoppingListsService,
    protected userService: UserService,
    private router: Router,
    private fb: FormBuilder,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.shoppingList$.subscribe((shoppingList: GetShoppingListResponse) => {
      if (!shoppingList.id || !shoppingList.listName) {
        this.router.navigate(['/shopping-lists']);
      }

      this.shoppingList = shoppingList;
      this.getShoppinglistProducts();
    });
  }

  submitSearchForm() {
    this.page = 0;
    this.getShoppinglistProducts();
  }

  pageChange(event: PageEvent) {
    this.page = event.pageIndex;
    this.getShoppinglistProducts();
  }

  updateListProductToAdd(
    updatedProduct: ShoppingListProductDTO,
    index: number
  ) {
    const currentIndex = this.page_size * this.page + index;
    this.productsToAdd[currentIndex] = updatedProduct;
  }

  checkboxClicked(event: ShoppingListProductCheckboxEvent) {
    if (event.checked) {
      this.productsIdsForAction.push(event.listProductId);
    } else {
      this.productsIdsForAction = this.productsIdsForAction.filter(
        (currentProduct) => currentProduct !== event.listProductId
      );
    }
  }

  reloadPage() {
    this.page = 0;
    this.getShoppinglistProducts();
  }

  getErrorMessage(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('min')) {
      return 'Quantity must be greater than 0';
    } else if (control.hasError('pattern')) {
      return 'Field does not match required pattern';
    }

    return '';
  }

  productToAddCheckboxEvent(event: ShoppingListProductCheckboxEvent) {
    if (event.checked) {
      this.productsToAddIdsToRemove.push(event.listProductId);
    } else {
      this.productsToAddIdsToRemove = this.productsToAddIdsToRemove.filter(
        (currentProduct) => currentProduct !== event.listProductId
      );
    }
  }

  closeAddProducts() {
    this.productsToAdd = [];
    this.showAddProducts = false;
  }

  removeProductsFromAdding() {
    if (
      this.shoppingList &&
      this.shoppingList.id &&
      this.shoppingList.listName
    ) {
      this.productsToAddIdsToRemove.forEach((id) => {
        this.productsToAdd = this.productsToAdd.filter(
          (listProductDTO) => listProductDTO.id !== id
        );
      });

      this.productsToAddIdsToRemove = [];
      this.displayProductsToAddPage(0);
    }
  }

  removeProductsFromList() {
    if (
      this.shoppingList &&
      this.shoppingList.id &&
      this.shoppingList.listName &&
      this.productsIdsForAction
    ) {
      this.shoppingListsService
        .removeShoppingListProducts(
          this.shoppingList.id,
          this.productsIdsForAction
        )
        .subscribe((_) => {
          this.productsIdsForAction = [];
          this.reloadPage();
        });
    }
  }

  changePurchaseStatusForProducts() {
    if (
      this.shoppingList &&
      this.shoppingList.id &&
      this.shoppingList.listName &&
      this.productsIdsForAction
    ) {
      this.shoppingListsService
        .changePurchaseStatusForProducts(
          this.shoppingList.id,
          this.productsIdsForAction
        )
        .subscribe((_) => {
          this.productsIdsForAction = [];
          this.reloadPage();
        });
    }
  }

  sendProductsToAdd() {
    this.productsToAdd.forEach((product) => (product.id = 0));

    this.shoppingListsService
      .addProductsToShoppingList(this.shoppingList!.id, this.productsToAdd)
      .subscribe((_) => {
        this.closeAddProducts();
        this.productsToAddIdsToRemove = [];
        this.page = 0;
        this.getShoppinglistProducts();
      });
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

  transferAvailable() {
    return !this.products.find((product) => product.purchased);
  }

  transferProductsToPantry() {
    const confirmDialog = this.dialog.open(ConfirmationPopupComponent, {
      data: {
        header: 'Transfer products',
        body: "Are you sure you want to transfer purchased products to group's pantry? It will result in purchased shopping list products removal.",
        button: 'Confirm',
      },
    });

    confirmDialog.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.shoppingListsService
          .transferProductsToPantry(this.shoppingList!.id)
          .subscribe((_) => {
            this.reloadPage();
          });
      }
    });
  }

  searchForProducts() {
    if (this.addForm.controls.productName.value) {
      this.shoppingListsService
        .getProductsWithFilter(this.addForm.controls.productName.value)
        .subscribe((response) => {
          this.filteredProducts = of(response.content);
        });
    }
  }

  submitAddForm(form: FormGroupDirective) {
    if (!this.addForm.valid) {
      return;
    }

    this.productsToAdd.push({
      id: this.productsToAdd.length,
      product: {
        productId: 0,
        productName: this.addForm.controls.productName.value!,
        category: this.addForm.controls.category.value! as Category,
      },
      quantity: +this.addForm.controls.quantity.value!,
      unit:
        this.addForm.controls.unit.value === Unit.GRAMS
          ? Unit.GRAMS
          : this.addForm.controls.unit.value === Unit.MILLILITERS
          ? Unit.MILLILITERS
          : Unit.PIECES,
      purchased: this.addForm.controls.purchased.value!,
    });
    form.resetForm();
    this.addForm.reset();
    this.displayProductsToAddPage(0);
  }

  private getShoppinglistProducts() {
    const filterValue = this.searchForm.controls.filterValue.value!;
    const sortColName = this.searchForm.controls.sortColName.value!;
    const SortDirection = this.searchForm.controls.sortDirection.value!;

    if (
      this.shoppingList &&
      this.shoppingList.id &&
      this.shoppingList.listName
    ) {
      this.shoppingListsService
        .getShoppingListsProducts(
          this.shoppingList.id,
          this.page,
          filterValue,
          sortColName,
          SortDirection
        )
        .subscribe((response) => {
          this.products = response.content;
          this.totalElements = response.totalElements;
          this.currentElementsLength = response.content.length;
        });
    }
  }
}
