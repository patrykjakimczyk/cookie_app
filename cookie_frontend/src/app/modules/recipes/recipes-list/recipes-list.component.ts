import { Component, Input } from '@angular/core';
import { GetRecipesParams } from 'src/app/shared/model/types/recipes-types';
import { GetRecipesClient } from './get-recipes-clients/abstract-get-recipes-client';
import { PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-recipes-list',
  templateUrl: './recipes-list.component.html',
  styleUrls: ['./recipes-list.component.scss'],
})
export class RecipesListComponent {
  @Input() getRecipesClient!: GetRecipesClient;
  params!: GetRecipesParams;

  getRecipes(params: GetRecipesParams) {
    this.getRecipesClient.page = 0;
    this.getRecipesClient.getRecipes(params);
    this.params = params;
  }

  pageChange(event: PageEvent) {
    this.getRecipesClient.page = event.pageIndex;
    this.getRecipesClient.getRecipes(this.params!);
  }
}
