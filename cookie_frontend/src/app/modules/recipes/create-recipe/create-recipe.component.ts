import {
  MealType,
  mealTypes,
} from './../../../shared/model/enums/meal-type.enum';
import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroupDirective,
  Validators,
} from '@angular/forms';
import { UserService } from 'src/app/shared/services/user-service';
import { RecipesService } from '../recipes.service';
import { Observable, of } from 'rxjs';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { Category, categories } from 'src/app/shared/model/enums/category.enum';
import { Unit, units } from 'src/app/shared/model/enums/unit.enum';
import {
  RecipeDetailsDTO,
  RecipeProductDTO,
} from 'src/app/shared/model/types/recipes-types';
import { portions } from 'src/app/shared/model/constants/recipes.constants';
import { ActivatedRoute, Router } from '@angular/router';
import { CreateRecipeRequest } from 'src/app/shared/model/requests/recipe-requests';
import { CreateRecipeResponse } from 'src/app/shared/model/responses/recipes-response';
import { MatDialog } from '@angular/material/dialog';
import { ModifyIngredientComponent } from './modify-ingredient/modify-ingredient.component';
import { ProductDTO } from 'src/app/shared/model/types/product-types';

@Component({
  selector: 'app-create-recipe',
  templateUrl: './create-recipe.component.html',
  styleUrls: ['./create-recipe.component.scss'],
})
export class CreateRecipeComponent implements OnInit {
  protected edit = false;
  protected updateImage = false;
  protected wrongImageFormat = false;
  protected noIngrediendsAdded = false;
  protected productFilterValue = '';
  protected filteredProducts = new Observable<ProductDTO[]>();
  protected ingredientsToAdd: RecipeProductDTO[] = [];
  protected categories = categories;
  protected units = units;
  protected portions = portions;
  protected mealTypes = mealTypes;
  protected imageUrl: string | ArrayBuffer | null = '';
  protected image: Blob | null = null;

  protected recipeForm = this.fb.group({
    id: [0],
    recipeName: [
      '',
      [Validators.required, Validators.pattern(RegexConstants.recipeNameRegex)],
    ],
    mealType: ['', [Validators.required]],
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
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    if (this.router.url.includes('edit')) {
      this.setRecipeDataForEdit();
    }
  }

  private setRecipeDataForEdit() {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.recipesService
        .getRecipeDetails(+id)
        .subscribe((recipeDetails: RecipeDetailsDTO) => {
          if (recipeDetails.creatorName !== this.getUserName()) {
            this.router.navigate(['/']);
          }
          this.edit = true;

          this.recipeForm.controls.id.setValue(recipeDetails.id);
          this.recipeForm.controls.recipeName.setValue(
            recipeDetails.recipeName
          );
          this.recipeForm.controls.mealType.setValue(recipeDetails.mealType);
          this.recipeForm.controls.cuisine.setValue(recipeDetails.cuisine);
          this.recipeForm.controls.preparation.setValue(
            recipeDetails.preparation
          );
          this.recipeForm.controls.preparationTime.setValue(
            recipeDetails.preparationTime.toString()
          );
          this.recipeForm.controls.portions.setValue(
            recipeDetails.portions.toString()
          );
          this.ingredientsToAdd = recipeDetails.products;
          if (recipeDetails.recipeImage) {
            this.imageUrl =
              'data:image/JPEG;png;base64,' + recipeDetails.recipeImage;
          }
        });
    }
  }

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
    const file: Blob = event.target.files[0];

    if (file.type !== 'image/jpeg' && file.type !== 'image/png') {
      this.wrongImageFormat = true;
      return;
    }
    this.wrongImageFormat = false;

    this.image = file;
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (_event) => {
      this.imageUrl = reader.result;
    };

    if (this.edit && !this.updateImage) {
      this.updateImage = true;
    }
  }

  removeImage() {
    this.image = null;
    this.imageUrl = '';
    this.wrongImageFormat = false;

    if (this.edit && !this.updateImage) {
      this.updateImage = true;
    }
  }

  modifyIngredient(recipeProductDTO: RecipeProductDTO, productIdx: number) {
    const modifyDialog = this.dialog.open(ModifyIngredientComponent, {
      data: recipeProductDTO,
    });

    modifyDialog
      .afterClosed()
      .subscribe((modifiedIngredient: RecipeProductDTO) => {
        if (modifiedIngredient) {
          this.ingredientsToAdd[productIdx] = modifiedIngredient;
        }
      });
  }

  submitIngredientForm(form: FormGroupDirective) {
    if (!this.ingredientForm.valid) {
      return;
    }

    this.ingredientsToAdd.push({
      id: 0,
      product: {
        productId: 0,
        productName: this.ingredientForm.controls.productName.value!,
        category: this.ingredientForm.controls.category.value! as Category,
      },
      quantity: +this.ingredientForm.controls.quantity.value!,
      unit: this.ingredientForm.controls.unit.value! as Unit,
    });

    form.resetForm(); // this combination of two resets allows to reset form without displaying form fields as invalid
    this.ingredientForm.reset();
  }

  submitRecipeForm() {
    if (this.ingredientsToAdd.length === 0) {
      this.noIngrediendsAdded = true;
      return;
    }
    this.noIngrediendsAdded = false;

    if (!this.recipeForm.valid) {
      return;
    }

    const formData = new FormData();
    if (this.image && (this.updateImage || !this.edit)) {
      formData.append('image', this.image!);
    }

    const recipe: CreateRecipeRequest = {
      id: +this.recipeForm.controls.id.value!,
      recipeName: this.recipeForm.controls.recipeName.value!,
      preparation: this.recipeForm.controls.preparation.value!,
      preparationTime: +this.recipeForm.controls.preparationTime.value!,
      mealType: this.recipeForm.controls.mealType.value as MealType,
      cuisine: this.recipeForm.controls.cuisine.value!,
      portions: +this.recipeForm.controls.portions.value!,
      updateImage: this.updateImage,
      products: this.ingredientsToAdd,
    };

    formData.append('recipe', JSON.stringify(recipe));

    let recipeRequest = null;
    if (this.edit) {
      recipeRequest = this.recipesService.editRecipe(formData);
    } else {
      recipeRequest = this.recipesService.createRecipe(formData);
    }

    recipeRequest.subscribe((response: CreateRecipeResponse) => {
      this.router.navigate(['/recipes/', response.recipeId]);
    });
  }

  searchForProducts() {
    if (this.ingredientForm.controls.productName.value) {
      this.recipesService
        .getProductsWithFilter(this.ingredientForm.controls.productName.value)
        .subscribe((response) => {
          this.filteredProducts = of(response);
        });
    }
  }

  compareOptions(obj1: any, obj2: any) {
    return obj1 === obj2;
  }

  setCategoryForProduct(category: Category) {
    this.ingredientForm.controls.category.setValue(category);
  }

  removeIngredientFromAdding(recipeProduct: RecipeProductDTO) {
    this.ingredientsToAdd = this.ingredientsToAdd.filter((ingredientToAdd) => {
      return !(
        ingredientToAdd.product.productName ===
          recipeProduct.product.productName &&
        ingredientToAdd.product.category === recipeProduct.product.category &&
        ingredientToAdd.quantity === recipeProduct.quantity &&
        ingredientToAdd.unit === recipeProduct.unit
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
