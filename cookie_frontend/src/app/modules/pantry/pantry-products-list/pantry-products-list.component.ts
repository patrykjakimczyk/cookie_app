import { Component, Input } from '@angular/core';
import { GetPantryResponse } from 'src/app/shared/model/responses/pantry-response';

export interface PantryProduct {
  productName: string;
  category: string;
  quantity: string;
  expirationDate: string;
}

@Component({
  selector: 'app-pantry-products-list',
  templateUrl: './pantry-products-list.component.html',
  styleUrls: ['./pantry-products-list.component.scss'],
})
export class PantryProductsListComponent {
  @Input() pantry!: GetPantryResponse;
  products: PantryProduct[] = [
    {
      productName: 'Banana',
      category: 'Fruits',
      quantity: '3',
      expirationDate: '23-11-2023',
    },
    {
      productName: 'apple',
      category: 'Fruits',
      quantity: '33',
      expirationDate: '11-11-2023',
    },
  ];
}
