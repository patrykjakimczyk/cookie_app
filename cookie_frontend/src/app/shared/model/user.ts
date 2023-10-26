export class User {
  email: string;
  username: string;
  password: string;
  role: string;
  auth: boolean;

  constructor() {
    this.email = '';
    this.username = '';
    this.password = '';
    this.role = '';
    this.auth = false;
  }
}
