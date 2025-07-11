import { registerPlugin } from '@capacitor/core';

import type { CapacitorXprinterPlugin } from './definitions';

const CapacitorXprinter = registerPlugin<CapacitorXprinterPlugin>('CapacitorXprinter', {
  web: () => import('./web').then((m) => new m.CapacitorXprinter()),
});

export * from './definitions';
export { CapacitorXprinter };
