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
import {
  RecipeDetailsDTO,
  RecipeProductDTO,
} from 'src/app/shared/model/types/recipes-types';
import { portions } from 'src/app/shared/model/constants/recipes.constants';
import { Router } from '@angular/router';

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
  protected portions = portions;
  imageUrl: string | ArrayBuffer | null = '';

  protected recipeForm = this.fb.group({
    id: [0],
    recipeName: [
      '',
      [Validators.required, Validators.pattern(RegexConstants.recipeNameRegex)],
    ],
    cuisine: [''],
    preparation: [
      '',
      [
        Validators.required,
        Validators.pattern(RegexConstants.preparationRegex),
      ],
    ],
    preparationTime: [
      '',
      [Validators.required, Validators.min(1), Validators.pattern('[-0-9]+')],
    ],
    portions: ['', [Validators.required]],
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
      [Validators.required, Validators.min(1), Validators.pattern('[-0-9]+')],
    ],
    unit: ['', [Validators.required]],
  });

  constructor(
    private recipesService: RecipesService,
    private userService: UserService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  getUserName() {
    return this.userService.user.getValue().username;
  }

  getErrorMessage(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('min')) {
      return 'Value must be greater than 0';
    } else if (control.hasError('pattern')) {
      return 'Field value is too short, too long or contains forbidden characters';
    }

    return '';
  }

  getIngredientErrorMessage(control: AbstractControl): string {
    if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('min')) {
      return 'Quantity must be greater than 0';
    } else if (control.hasError('pattern')) {
      return 'Field value does not match required pattern';
    }

    return '';
  }

  onFileSelected(event: any) {
    console.log(event.target.files);
    const file: File = event.target.files[0];

    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (_event) => {
      this.imageUrl = reader.result;
    };
  }

  removeImage() {
    this.imageUrl = '';
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

  submitRecipeForm() {
    if (!this.recipeForm.valid) {
      return;
    }

    if (this.ingredientsToAdd.length === 0) {
      return;
    }

    var enc = new TextEncoder();

    const recipeToAdd: RecipeDetailsDTO = {
      id: +this.recipeForm.controls.id.value!,
      recipeName: this.recipeForm.controls.recipeName.value!,
      preparation: this.recipeForm.controls.preparation.value!,
      preparationTime: +this.recipeForm.controls.preparationTime.value!,
      cuisine: this.recipeForm.controls.cuisine.value!,
      portions: +this.recipeForm.controls.portions.value!,
      recipeImage: null,
      creatorName: this.getUserName(),
      products: this.ingredientsToAdd,
    };

    this.recipesService.createRecipe(recipeToAdd).subscribe({
      next: (addedRecipe: RecipeDetailsDTO) => {
        console.log(addedRecipe);
        this.router.navigate(['/recipes']);
      },
    });

    // this.ingredientsToAdd.push({
    //   id: 0,
    //   productName: this.ingredientForm.controls.productName.value!,
    //   category: this.ingredientForm.controls.category.value!,
    //   quantity: +this.ingredientForm.controls.quantity.value!,
    //   unit: this.ingredientForm.controls.unit.value! as Unit,
    // });

    // this.ingredientForm.reset();
  }

  searchForProducts() {
    if (this.ingredientForm.controls.productName.value) {
      this.recipesService
        .getProductsWithFilter(this.ingredientForm.controls.productName.value)
        .subscribe({
          next: (response) => {
            this.filteredProducts = of(response.content);
          },
        });
    }
  }

  setCategoryForProduct(category: Category) {
    this.ingredientForm.controls.category.setValue(category);
  }

  removeIngredientFromAdding(product: RecipeProductDTO) {
    this.ingredientsToAdd = this.ingredientsToAdd.filter((ingredientToAdd) => {
      return !(
        ingredientToAdd.productName === product.productName &&
        ingredientToAdd.category === product.category &&
        ingredientToAdd.quantity === product.quantity &&
        ingredientToAdd.unit === product.unit
      );
    });
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
