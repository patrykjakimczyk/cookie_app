import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CreatePantryRequest,
  UpdatePantryRequest,
} from 'src/app/shared/model/requests/pantry-requests';
import {
  DeletePantryResponse,
  GetPantryResponse,
  GetUserPantriesResponse,
} from 'src/app/shared/model/responses/pantry-response';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';
import { PantryProductDTO } from 'src/app/shared/model/types/pantry-types';
import { ShoppingListProductDTO } from 'src/app/shared/model/types/shopping-lists-types';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class PantriesService {
  private readonly url = environment.backendUrl;
  private readonly pantry_path = 'pantry';
  private readonly pantry_id_path = 'pantry/{id}';
  private readonly products_path = 'products';
  private readonly pantry_products_path = '/products';
  private readonly group_id_url = 'group/{id}';
  private readonly shopping_list_id_path = 'shopping-lists/{id}';
  private readonly list_products_path = '/products';

  constructor(private http: HttpClient) {}

  getUserPantry(pantryId: number): Observable<GetPantryResponse> {
    return this.http.get<GetPantryResponse>(
      this.url + this.pantry_path + '/' + pantryId
    );
  }

  getAllUserPantries(): Observable<GetUserPantriesResponse> {
    return this.http.get<GetUserPantriesResponse>(this.url + this.pantry_path);
  }

  createUserPantry(
    request: CreatePantryRequest
  ): Observable<GetPantryResponse> {
    return this.http.post<GetPantryResponse>(
      this.url + this.pantry_path,
      request
    );
  }

  updateUserPantry(
    pantryId: number,
    request: UpdatePantryRequest
  ): Observable<GetPantryResponse> {
    return this.http.patch<GetPantryResponse>(
      this.url + this.pantry_id_path.replace('{id}', pantryId.toString()),
      request
    );
  }

  deleteUserPantry(pantryId: number): Observable<DeletePantryResponse> {
    return this.http.delete<DeletePantryResponse>(
      this.url + this.pantry_id_path.replace('{id}', pantryId.toString())
    );
  }

  getPantryProducts(
    pantryId: number,
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
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}/${page}`,
      { params: params }
    );
  }

  addProductsToPantry(
    pantryId: number,
    pantryProducts: PantryProductDTO[]
  ): Observable<any> {
    return this.http.post<any>(
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}`,
      pantryProducts
    );
  }

  removeProductsFromPantry(
    pantryId: number,
    productsIds: number[]
  ): Observable<any> {
    return this.http.delete<void>(
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}`,
      { body: productsIds }
    );
  }

  modifyPantryProduct(
    pantryId: number,
    product: PantryProductDTO
  ): Observable<any> {
    return this.http.patch<void>(
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}`,
      product
    );
  }

  reservePantryProduct(
    pantryId: number,
    pantryProductId: number,
    reserved: number
  ): Observable<PantryProductDTO> {
    return this.http.patch<PantryProductDTO>(
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}/${pantryProductId}`,
      { reserved: reserved }
    );
  }

  getProductsWithFilter(filterValue: string): Observable<any> {
    let params = new HttpParams();

    params = params.append('filterValue', filterValue);

    return this.http.get<any>(`${this.url}${this.products_path}`, {
      params: params,
    });
  }

  getGroup(groupId: number) {
    return this.http.get<GroupDetailsDTO>(
      this.url + this.group_id_url.replace('{id}', groupId.toString())
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
}
