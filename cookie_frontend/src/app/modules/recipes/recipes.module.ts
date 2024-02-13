import { BrowserModule } from '@angular/platform-browser';
import { RecipesComponent } from './recipes.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { NgModule } from '@angular/core';
import { RecipesSidePanelComponent } from './recipes-side-panel/recipes-side-panel.component';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTabsModule } from '@angular/material/tabs';
import { RecipesListComponent } from './recipes-list/recipes-list.component';
import { RecipeTileComponent } from './recipes-list/recipe-tile/recipe-tile.component';
import { RecipeDetailsComponent } from './recipe-details/recipe-details.component';
import { MatListModule } from '@angular/material/list';
import { MatDialogModule } from '@angular/material/dialog';
import { CreateRecipeComponent } from './create-recipe/create-recipe.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ModifyIngredientComponent } from './create-recipe/modify-ingredient/modify-ingredient.component';

@NgModule({
  declarations: [
    RecipesComponent,
    RecipesSidePanelComponent,
    RecipesListComponent,
    RecipeTileComponent,
    RecipeDetailsComponent,
    CreateRecipeComponent,
    ModifyIngredientComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    RouterModule,
    ReactiveFormsModule,

    SharedModule,

    MatButtonModule,
    MatDividerModule,
    MatCardModule,
    MatInputModule,
    MatIconModule,
    MatCheckboxModule,
    MatPaginatorModule,
    MatSelectModule,
    MatRadioModule,
    MatExpansionModule,
    MatTabsModule,
    MatListModule,
    MatDialogModule,
    MatFormFieldModule,
    MatAutocompleteModule,
    MatTooltipModule,
  ],
  exports: [RecipesComponent],
})
export class RecipesModule {}
