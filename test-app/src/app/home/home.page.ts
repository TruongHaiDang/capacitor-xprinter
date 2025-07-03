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
import { link } from 'ionicons/icons';
import { XprinterService } from '../services/xprinter.service';

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
  selectedConnectType: string = '';
  selectedLanguage: string = '';

  serialPorts: string[] = [];
  selectedSerialPort: string = '';

  bluetoothDevices: string[] = [];
  selectedBluetoothMac: string = '';

  ipAddress: string = '';
  macAddress: string = '';

  comPorts: string[] = [];
  selectedComPort: string = '';
  baudrateList: number[] = [110, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200];
  selectedBaudrate: number = 9600;

  connectStatus: string = '';
  isConnected: boolean = false;

  deviceInfo: any = null;

  constructor(private xprinter: XprinterService) {
    addIcons({link});
  }

  ngOnInit() {}

  async onConnectPrinter() {
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
        options.port = 9100;
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
}
