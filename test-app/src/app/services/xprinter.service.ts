import { Injectable } from '@angular/core';
import { CapacitorXprinter } from 'capacitor-xprinter';

@Injectable({
  providedIn: 'root'
})
export class XprinterService {

  constructor() {
    CapacitorXprinter.initialize();
  }


}
