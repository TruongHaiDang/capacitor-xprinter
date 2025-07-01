import { WebPlugin } from '@capacitor/core';

import type { CapacitorXprinterPlugin } from './definitions';

export class CapacitorXprinterWeb extends WebPlugin implements CapacitorXprinterPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
