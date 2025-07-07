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
  IonList,
  IonButtons,
  IonButton,
  IonIcon
} from '@ionic/angular/standalone';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { addIcons } from 'ionicons';
import { link, grid } from 'ionicons/icons';
import { XprinterService } from '../services/xprinter.service';
import { Router } from '@angular/router';

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
    IonList,
    IonButtons,
    IonButton,
    IonIcon
  ],
})
export class HomePage implements OnInit {
  connectTypes: string[] = ['USB', 'BLUETOOTH', 'ETHERNET', 'SERIAL'];
  printLanguages: string[] = ['POS', 'CPCL', 'TSPL', 'ZPL'];
  selectedConnectType: string = this.connectTypes[2];
  selectedLanguage: string = this.printLanguages[0];

  serialPorts: string[] = [];
  selectedSerialPort: string = '';

  bluetoothDevices: string[] = [];
  selectedBluetoothMac: string = '';

  ipAddress: string = '172.16.32.170';
  macAddress: string = '';

  comPorts: string[] = [];
  selectedComPort: string = '';
  baudrateList: number[] = [110, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200];
  selectedBaudrate: number = this.baudrateList[6];

  connectStatus: string = '';
  isConnected: boolean = false;

  deviceInfo: any = null;

  constructor(private xprinter: XprinterService, private router: Router) {
    addIcons({link, grid});
  }

  ngOnInit() {}

  async onConnectPrinter() {
    if (this.isConnected) {
      this.connectStatus = 'Đang ngắt kết nối...';
      try {
        const res = await this.xprinter.disconnectPrinter?.();
        this.isConnected = false;
        this.deviceInfo = null;
        this.connectStatus = res?.msg || 'Đã ngắt kết nối';
      } catch (err: any) {
        this.connectStatus = 'Lỗi khi ngắt kết nối: ' + (err?.msg || err?.message || err);
      }
      return;
    }
    this.connectStatus = 'Đang kết nối...';
    try {
      const options: any = {
        type: this.selectedConnectType,
        language: this.selectedLanguage,
      };
      if (this.selectedConnectType === 'BLUETOOTH') {
        options.macAddress = this.selectedBluetoothMac || this.macAddress;
      } else if (this.selectedConnectType === 'USB') {
        options.name = this.selectedSerialPort;
      } else if (this.selectedConnectType === 'ETHERNET') {
        options.ip = this.ipAddress;
      } else if (this.selectedConnectType === 'SERIAL') {
        options.serialPort = this.selectedComPort;
        options.baudRate = this.selectedBaudrate;
      }
      const res = await this.xprinter.connectPrinter(options);
      this.isConnected = res.code === 1 || res.code === 200;
      this.deviceInfo = res.data;
      this.connectStatus = this.isConnected ? 'Kết nối thành công!' : 'Kết nối thất bại: ' + res.msg;
    } catch (err: any) {
      this.isConnected = false;
      this.connectStatus = 'Lỗi: ' + (err?.msg || err?.message || err);
    }
  }

  async onConnectTypeChange() {
    if (this.selectedConnectType === 'USB') {
      this.serialPorts = await this.xprinter.listUsbPorts();
      if (this.serialPorts.length > 0) {
        this.selectedSerialPort = this.serialPorts[0];
      }
    } else if (this.selectedConnectType === 'BLUETOOTH') {
      this.bluetoothDevices = await this.xprinter.listBluetoothDevices();
      if (this.bluetoothDevices.length > 0) {
        this.selectedBluetoothMac = this.bluetoothDevices[0];
      }
    } else if (this.selectedConnectType === 'SERIAL') {
      this.comPorts = await this.xprinter.listSerialPorts();
      if (this.comPorts.length > 0) {
        this.selectedComPort = this.comPorts[0];
      }
    }
  }

  openPrintTestPage() {
    this.router.navigate(['/print-test']);
  }
}
