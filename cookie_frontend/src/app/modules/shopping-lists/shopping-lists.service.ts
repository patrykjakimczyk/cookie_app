import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreateShoppingListRequest,
  DeleteShoppingListResponse,
  GetShoppingListResponse,
  GetUserShoppingListsResponse,
  ShoppingListProductDTO,
  UpdateShoppingListRequest,
} from 'src/app/shared/model/types/shopping-lists-types';

@Injectable({ providedIn: 'root' })
export class ShoppingListsService {
  private readonly url = 'http://localhost:8081/';
  private readonly shopping_list_path = 'shopping-list';
  private readonly shopping_list_id_path = 'shopping-list/{id}';
  private readonly list_products_path = '/products';
  private readonly list_products_page_path = '/products/{page}';
  private readonly products_path = 'products';
  private readonly products_purchase_path = '/products/purchase';
  private readonly products_transfer_path = '/products/transfer';

  constructor(private http: HttpClient) {}

  createShoppingList(
    createListBody: CreateShoppingListRequest
  ): Observable<GetShoppingListResponse> {
    return this.http.post<GetShoppingListResponse>(
      this.url + this.shopping_list_path,
      createListBody
    );
  }

  getAllUserShoppingLists(): Observable<GetUserShoppingListsResponse> {
    return this.http.get<GetUserShoppingListsResponse>(
      this.url + this.shopping_list_path
    );
  }

  getShoppingList(listId: number): Observable<GetShoppingListResponse> {
    return this.http.get<GetShoppingListResponse>(
      this.url + this.shopping_list_id_path.replace('{id}', listId.toString())
    );
  }

  deleteShoppingList(listId: number): Observable<DeleteShoppingListResponse> {
    return this.http.delete<DeleteShoppingListResponse>(
      this.url + this.shopping_list_id_path.replace('{id}', listId.toString())
    );
  }

  updateShoppingList(
    listId: number,
    updateListBody: UpdateShoppingListRequest
  ): Observable<GetShoppingListResponse> {
    return this.http.patch<GetShoppingListResponse>(
      this.url + this.shopping_list_id_path.replace('{id}', listId.toString()),
      updateListBody
    );
  }

  getShoppingListsProducts(
    listId: number,
    page: number,
    filterValue: string,
    sortColName: string,
    sortDirection: string
  ): Observable<any> {
    let params = new HttpParams();

    params = params
      .append('filterValue', filterValue)
      .append('sortColName', sortColName)
      .append('sortDirection', sortDirection);

    return this.http.get<any>(
      `${this.url}${this.shopping_list_id_path.replace(
        '{id}',
        listId.toString()
      )}${this.list_products_page_path.replace('{page}', page.toString())}`,
      { params: params }
    );
  }

  addProductsToShoppingList(
    listId: number,
    productsToAdd: ShoppingListProductDTO[]
  ): Observable<void> {
    return this.http.post<void>(
      this.url +
        this.shopping_list_id_path.replace('{id}', listId.toString()) +
        this.list_products_path,
      productsToAdd
    );
  }

  removeShoppingListProducts(
    listId: number,
    productsIds: number[]
  ): Observable<void> {
    return this.http.delete<void>(
      this.url +
        this.shopping_list_id_path.replace('{id}', listId.toString()) +
        this.list_products_path,
      { body: productsIds }
    );
  }

  updateShoppingListProduct(
    listId: number,
    listProduct: ShoppingListProductDTO
  ): Observable<void> {
    return this.http.patch<void>(
      this.url +
        this.shopping_list_id_path.replace('{id}', listId.toString()) +
        this.list_products_path,
      listProduct
    );
  }

  changePurchaseStatusForProducts(
    listId: number,
    productsIds: number[]
  ): Observable<void> {
    return this.http.patch<void>(
      this.url +
        this.shopping_list_id_path.replace('{id}', listId.toString()) +
        this.products_purchase_path,
      productsIds
    );
  }

  transferProductsToPantry(listId: number): Observable<void> {
    return this.http.post<void>(
      this.url +
        this.shopping_list_id_path.replace('{id}', listId.toString()) +
        this.products_transfer_path,
      null
    );
  }

  getProductsWithFilter(filterValue: string): Observable<any> {
    let params = new HttpParams();

    params = params.append('filterValue', filterValue);

    return this.http.get<any>(`${this.url}${this.products_path}`, {
      params: params,
    });
  }
}
