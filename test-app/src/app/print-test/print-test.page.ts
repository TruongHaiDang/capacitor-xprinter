import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonButtons,
  IonBackButton,
  IonGrid,
  IonRow,
  IonCol,
} from '@ionic/angular/standalone';
import { XprinterService } from '../services/xprinter.service';

@Component({
  selector: 'app-print-test',
  templateUrl: './print-test.page.html',
  styleUrls: ['./print-test.page.scss'],
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    CommonModule,
    FormsModule,
    IonButtons,
    IonBackButton,
    IonGrid,
    IonRow,
    IonCol,
  ],
})
export class PrintTestPage implements OnInit {
  constructor(private xprinter: XprinterService) {}

  ngOnInit() {}

  async onPrintText() {
    const text = prompt('Nhập nội dung cần in', 'Hello XPrinter');
    if (!text) return;
    try {
      const res = await this.xprinter.printText(text);
      alert(res.msg || 'In thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in');
    }
  }

  async onPrintQRCode() {
    const data = prompt('Nhập nội dung QR', 'https://xprinter.net');
    if (!data) return;
    try {
      const res = await this.xprinter.printQRCode(data);
      alert(res.msg || 'In QR thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in');
    }
  }

  async onPrintBarcode() {
    const data = prompt('Nhập nội dung Barcode', '1234567890');
    if (!data) return;
    try {
      const res = await this.xprinter.printBarcode(data, 73); // CODE128 mặc định
      alert(res.msg || 'In barcode thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in');
    }
  }

  async onPrintImage() {
    const path = prompt('Nhập đường dẫn ảnh trên thiết bị', '/sdcard/Download/logo.png');
    if (!path) return;
    try {
      const res = await this.xprinter.printImageFromPath(path);
      alert(res.msg || 'In hình thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in');
    }
  }
}
