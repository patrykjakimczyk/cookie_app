import { GroupDTO } from './group-types';
import { RecipeDTO } from './recipes-types';
import { UserDTO } from './user-types';

export type MealDTO = {
  mealDate: Date;
  user: UserDTO;
  group: GroupDTO;
  recipe: RecipeDTO;
};
