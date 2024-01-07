import { ShoppingListProductCheckboxEvent } from './shopping-list-products-elem/shopping-list-products-elem.component';
import { Component, Input, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import {
  GetShoppingListResponse,
  ShoppingListProductDTO,
} from 'src/app/shared/model/types/shopping-lists-types';
import { ShoppingListsService } from '../../shopping-lists.service';
import { Router } from '@angular/router';
import { FormBuilder } from '@angular/forms';
import {
  shoppingListSortColumnNames,
  sortDirecitons,
} from 'src/app/shared/model/enums/sort-enum';
import { UserService } from 'src/app/shared/services/user-service';
import { AuthorityEnum } from 'src/app/shared/model/enums/authority-enum';
import { PageEvent } from '@angular/material/paginator';

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
  public sortDirecitons = sortDirecitons;
  public productsIdsForAction: number[] = [];

  protected searchForm = this.fb.group({
    filterValue: [''],
    sortColName: [''],
    sortDirection: [''],
  });

  constructor(
    private shoppingListsService: ShoppingListsService,
    protected userService: UserService,
    private router: Router,
    private fb: FormBuilder
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
        .subscribe({
          next: (response) => {
            this.products = response.content;
            console.log(this.products);
            this.totalElements = response.totalElements;
            this.currentElementsLength = response.content.length;
          },
        });
    }
  }
}
