import { GetUserRecipesClient } from './recipes-list/get-recipes-clients/get-user-recipes-client';
import { UserService } from 'src/app/shared/services/user-service';
import { Component, OnInit } from '@angular/core';
import { GetAllRecipesClient } from './recipes-list/get-recipes-clients/get-all-recipes-client';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-recipes',
  templateUrl: './recipes.component.html',
  styleUrls: ['./recipes.component.scss'],
})
export class RecipesComponent implements OnInit {
  protected showReturnToMeals = false;

  constructor(
    public getAllRecipesClient: GetAllRecipesClient,
    public getUserRecipesClient: GetUserRecipesClient,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      let mealPlanning = params['mealPlanning'];

      if (mealPlanning !== null) {
        this.showReturnToMeals = mealPlanning;
      } else {
        this.showReturnToMeals = true;
      }
    });
  }

  returnToMeals() {
    this.router.navigate(['/meals'], {
      queryParams: { scheduleMeal: true },
    });
  }

  isUserLogged() {
    return this.userService.isUserLogged();
  }

  goToCreateRecipe() {
    this.router.navigate(['/recipes/create']);
  }
}
