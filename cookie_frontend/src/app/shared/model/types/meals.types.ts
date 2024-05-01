import { GroupDTO } from './group-types';
import { RecipeDTO } from './recipes-types';
import { UserDTO } from './user-types';

export type MealDTO = {
  id: number;
  mealDate: Date;
  username: String;
  group: GroupDTO;
  recipe: RecipeDTO;
};
