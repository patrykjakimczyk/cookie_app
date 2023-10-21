export namespace RegexConstants {
  export const passwordRegex =
    '(?=.*[a-z])(?=.*[A-Z])(?=.*d)(?=.*[@$!%*?&])[a-zA-Zd@$!%*?&]{8,128}$';
  export const loginRegex = '^([a-zA-Zd_]{6,30})$';
}
