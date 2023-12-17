import { AuthorityDTO } from './types/user-types';

export class User {
  email: string;
  username: string;
  password: string;
  assignedPantry: boolean;
  role: string;
  auth: boolean;
  authorities: AuthorityDTO[];

  constructor() {
    this.email = '';
    this.username = '';
    this.password = '';
    this.assignedPantry = false;
    this.role = '';
    this.auth = false;
    this.authorities = [];
  }
}
