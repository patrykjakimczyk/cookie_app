import { Component } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroupDirective,
  Validators,
} from '@angular/forms';
import { UserService } from 'src/app/shared/services/user-service';
import { RecipesService } from '../recipes.service';
import { Observable, of } from 'rxjs';
import { ProductDTO } from 'src/app/shared/model/types/pantry-types';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { Category, categories } from 'src/app/shared/model/enums/category-enum';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import { RecipeProductDTO } from 'src/app/shared/model/types/recipes-types';

@Component({
  selector: 'app-create-recipe',
  templateUrl: './create-recipe.component.html',
  styleUrls: ['./create-recipe.component.scss'],
})
export class CreateRecipeComponent {
  protected productFilterValue = '';
  protected filteredProducts = new Observable<ProductDTO[]>();
  protected ingredientsToAdd: RecipeProductDTO[] = [];
  protected categories = categories;
  protected units = units;

  protected recipeForm = this.fb.group({
    id: [0],
    recipeName: ['', Validators.required],
    cuisine: ['', Validators.required],
    preparation: ['', Validators.required],
    preparationTime: [0, Validators.required],
    portions: [0, Validators.required],
    products: [],
  });

  protected ingredientForm = this.fb.group({
    id: [0],
    productName: [
      '',
      [
        Validators.required,
        Validators.pattern(RegexConstants.productNameRegex),
      ],
    ],
    category: ['', [Validators.required]],
    quantity: [
      '',
      [Validators.required, Validators.min(1), Validators.pattern('[0-9]+')],
    ],
    unit: ['', [Validators.required]],
  });

  constructor(
    private recipesService: RecipesService,
    private userService: UserService,
    private fb: FormBuilder
  ) {}

  printUserName() {
    return this.userService.user.getValue().username;
  }

  getIngredientErrorMessage(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('min')) {
      return 'Quantity must be greater than 0';
    } else if (control.hasError('pattern')) {
      return 'Field does not match required pattern';
    }

    return '';
  }

  searchForProducts() {
    console.log(this.ingredientForm.controls.productName.value);
    if (this.ingredientForm.controls.productName.value) {
      this.recipesService
        .getProductsWithFilter(this.ingredientForm.controls.productName.value)
        .subscribe({
          next: (response) => {
            console.log(response);

            this.filteredProducts = of(response.content);
          },
        });
    }
  }

  setCategoryForProduct(category: Category) {
    this.ingredientForm.controls.category.setValue(category);
  }

  submitIngredientForm(form: FormGroupDirective) {
    if (!this.ingredientForm.valid) {
      return;
    }

    this.ingredientsToAdd.push({
      id: 0,
      productName: this.ingredientForm.controls.productName.value!,
      category: this.ingredientForm.controls.category.value!,
      quantity: +this.ingredientForm.controls.quantity.value!,
      unit: this.ingredientForm.controls.unit.value! as Unit,
    });

    form.resetForm(); // this combination of two resets allows to reset form without displaying form fields as invalid
    this.ingredientForm.reset();
  }

  printShortUnit(recipeProduct: RecipeProductDTO) {
    if (recipeProduct.unit === Unit.GRAMS) {
      return 'g';
    } else if (recipeProduct.unit === Unit.MILLILITERS) {
      return 'ml';
    } else {
      return recipeProduct.quantity > 1 ? 'pcs' : 'pc';
    }
  }
}
