// En production, l'app est servie par nginx qui proxie /api/* vers le gateway.
// On laisse donc apiUrl vide → tous les appels HTTP sont relatifs au domaine courant.
export const environment = {
  production: true,
  apiUrl: ''
};

