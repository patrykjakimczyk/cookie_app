type HttpPath = {
  url: string;
  method: string;
};

export const excludedMethods: HttpPath[] = [
  { url: '/user', method: 'GET' },
  { url: '/groups/[0-9]+/authorities$', method: 'PATCH' },
  { url: '/groups/[0-9]+/users$', method: 'POST' },
  { url: '/groups/[0-9]+/users?', method: 'DELETE' },
  { url: '/meals?', method: 'POST' },
  { url: '/meals/[0-9]+$', method: 'PATCH' },
];
