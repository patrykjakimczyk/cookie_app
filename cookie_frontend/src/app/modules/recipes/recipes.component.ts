import { GetUserRecipesClient } from './recipes-list/get-recipes-clients/get-user-recipes-client';
import { UserService } from 'src/app/shared/services/user-service';
import { Component } from '@angular/core';
import { GetAllRecipesClient } from './recipes-list/get-recipes-clients/get-all-recipes-client';
import { Router } from '@angular/router';

@Component({
  selector: 'app-recipes',
  templateUrl: './recipes.component.html',
  styleUrls: ['./recipes.component.scss'],
})
export class RecipesComponent {
  constructor(
    public getAllRecipesClient: GetAllRecipesClient,
    public getUserRecipesClient: GetUserRecipesClient,
    private userService: UserService,
    private router: Router
  ) {}

  isUserLogged() {
    return this.userService.isUserLogged();
  }

  goToCreateRecipe() {
    this.router.navigate(['/recipes/create']);
  }
}
