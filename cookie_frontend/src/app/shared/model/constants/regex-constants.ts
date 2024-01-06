export namespace RegexConstants {
  export const passwordRegex =
    '^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[a-zA-Z0-9@$!%*?&]{8,128}$';
  export const loginRegex = '^([a-zA-Z0-9d_]{6,30})$';
  export const pantryNameRegex = '^([a-zA-Z\\d\\s]{3,30})$';
  export const shoppingListRegex = '^([a-zA-Z\\d\\s]{3,30})$';
  export const groupNameRegex = '^([a-zA-Z\\d\\s]{3,30})$';
}
