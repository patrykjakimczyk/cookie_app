import { Component } from '@angular/core';
import { GetAllRecipesClient } from './recipes-list/get-recipes-clients/get-all-recipes-client';

@Component({
  selector: 'app-recipes',
  templateUrl: './recipes.component.html',
  styleUrls: ['./recipes.component.scss'],
})
export class RecipesComponent {
  constructor(public getAllRecipesClient: GetAllRecipesClient) {}
}
