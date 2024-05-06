import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResult } from 'src/app/shared/model/responses/page-result-response';
import { ProductDTO } from 'src/app/shared/model/types/product-types';
import {
  CreateShoppingListRequest,
  DeleteShoppingListResponse,
  GetShoppingListResponse,
  GetUserShoppingListsResponse,
  ShoppingListProductDTO,
  UpdateShoppingListRequest,
} from 'src/app/shared/model/types/shopping-lists-types';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class ShoppingListsService {
  private readonly url = environment.backendUrl;
  private readonly shopping_list_path = 'shopping-lists';
  private readonly shopping_list_id_path = 'shopping-lists/{id}';
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
    filterValue: string | null,
    sortColName: string | null,
    sortDirection: string | null
  ): Observable<PageResult<ShoppingListProductDTO>> {
    let params = new HttpParams();

    if (filterValue) {
      params = params.append('filterValue', filterValue);
    }
    if (sortColName) {
      params = params.append('sortColName', sortColName);
    }
    if (sortDirection) {
      params = params.append('sortDirection', sortDirection);
    }

    return this.http.get<PageResult<ShoppingListProductDTO>>(
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

  getProductsWithFilter(filterValue: string): Observable<ProductDTO[]> {
    let params = new HttpParams();

    params = params.append('filterValue', filterValue);

    return this.http.get<ProductDTO[]>(`${this.url}${this.products_path}`, {
      params: params,
    });
  }
}
