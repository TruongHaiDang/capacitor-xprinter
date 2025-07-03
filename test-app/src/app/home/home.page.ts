import { Component, OnInit } from '@angular/core';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonItem,
  IonSelect,
  IonSelectOption,
  IonInput,
  IonList
} from '@ionic/angular/standalone';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
  imports: [
    FormsModule,
    CommonModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonItem,
    IonSelect,
    IonSelectOption,
    IonInput,
    IonList
  ],
})
export class HomePage implements OnInit {
  connectTypes: string[] = ['USB', 'BLUETOOTH', 'ETHERNET', 'SERIAL'];
  printProtocols: string[] = ['POS', 'CPCL', 'TSPL', 'ZPL'];
  selectedConnectType: string = '';

  serialPorts: string[] = [];
  selectedSerialPort: string = '';

  ipAddress: string = '';
  macAddress: string = '';

  comPorts: string[] = [];
  selectedComPort: string = '';
  baudrateList: number[] = [110, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200];
  selectedBaudrate: number = 9600;

  constructor() {}

  ngOnInit() {}
}
