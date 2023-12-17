import { AuthorityDTO } from '../types/user-types';

export type RegistrationResponse = {
  duplicates: string[];
};

export type LoginResponse = {
  username: string;
  assignedPantry: boolean;
  authorities: AuthorityDTO[];
};
