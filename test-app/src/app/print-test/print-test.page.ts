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

  async onPrintQr() {
    const text = prompt('Nội dung QR', 'https://example.com');
    if (!text) return;
    try {
      const res = await this.xprinter.printQRCode(text);
      alert(res.msg || 'In thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in');
    }
  }

  async onPrintBarcode() {
    const text = prompt('Barcode content', '123456789');
    if (!text) return;
    try {
      const res = await this.xprinter.printBarcode(text, 73);
      alert(res.msg || 'In thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in');
    }
  }

  async onPrintImage() {
    const path = prompt('Đường dẫn hình ảnh', '/sdcard/test.jpg');
    if (!path) return;
    try {
      const res = await this.xprinter.printImagePath(path);
      alert(res.msg || 'In thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in');
    }
  }
}
