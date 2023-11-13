import { AfterViewInit, Component, Input, OnInit } from '@angular/core';
import { GetPantryResponse } from 'src/app/shared/model/responses/pantry-response';
import { PantryService } from '../pantry.service';
import { PageEvent } from '@angular/material/paginator';
import { Subject } from 'rxjs';

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
  @Input() pantry$!: Subject<GetPantryResponse>;
  protected pantry?: GetPantryResponse;
  public readonly page_size = 20;
  public page = 0;
  public totalElements = 0;
  public currentElementsLength = 0;
  public products: PantryProduct[] = [];

  constructor(private pantryService: PantryService) {}

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

  private getPantryProducts() {
    if (this.pantry && this.pantry.id && this.pantry.pantryName) {
      this.pantryService
        .getPantryProducts(this.pantry.id, this.page)
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
