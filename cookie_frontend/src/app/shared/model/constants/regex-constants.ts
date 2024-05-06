import * as XRegExp from 'xregexp';

export namespace RegexConstants {
  export const passwordRegex =
    '^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[a-zA-Z0-9@$!%*?&]{8,128}$';
  export const usernameRegex = XRegExp('^([\\p{L}\\d_]{6,30})$');
  export const pantryNameRegex = XRegExp('^([\\p{L}\\d\\s]{3,30})$');
  export const placementRegex = XRegExp('^([\\p{L}\\d\\s]{0,30})$');
  export const groupNameRegex = XRegExp('^[\\p{L}\\d\\s]{3,30}$');
  export const productNameRegex = XRegExp('^([\\p{L}\\d\\s]{3,50})$');
  export const shoppingListRegex = XRegExp('^([\\p{L}\\d\\s]{3,30})$');
  export const recipeNameRegex = XRegExp('^([\\p{L}\\d\\s]{5,60})$');
  export const preparationRegex = XRegExp(
    '^([\\p{L}\\d\\s\'":\\-_@,.]{30,512})$'
  );
}
