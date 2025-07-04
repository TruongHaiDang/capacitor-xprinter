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
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonSegment,
  IonSegmentButton,
  IonLabel,
  IonIcon,
} from '@ionic/angular/standalone';
import { XprinterService } from '../services/xprinter.service';
import { addIcons } from 'ionicons';
import {
  textOutline,
  qrCodeOutline,
  barcodeOutline,
  imageOutline,
  imagesOutline,
  codeOutline,
  cutOutline,
  cashOutline,
  checkmarkCircleOutline,
  documentOutline,
  informationCircleOutline,
  refreshOutline,
} from 'ionicons/icons';

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
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardContent,
    IonSegment,
    IonSegmentButton,
    IonLabel,
    IonIcon,
  ],
})
export class PrintTestPage implements OnInit {
  selectedProtocol: string = 'POS';

  constructor(private xprinter: XprinterService) {
    addIcons({
      textOutline,
      qrCodeOutline,
      barcodeOutline,
      imageOutline,
      imagesOutline,
      codeOutline,
      cutOutline,
      cashOutline,
      checkmarkCircleOutline,
      documentOutline,
      informationCircleOutline,
      refreshOutline,
    });
  }

  ngOnInit() {}

  async onProtocolChange() {
    try {
      const res = await this.xprinter.setProtocol(this.selectedProtocol as any);
      console.log('Protocol changed to:', this.selectedProtocol, res);
    } catch (err: any) {
      console.error('Failed to change protocol:', err);
      alert('Lỗi thay đổi protocol: ' + (err?.msg || err?.message || 'Unknown error'));
    }
  }

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
    const data = prompt('Nhập dữ liệu QR code', 'https://xprinter.net');
    if (!data) return;
    try {
      const options: any = { data };
      
      if (this.selectedProtocol === 'POS') {
        options.moduleSize = 4;
        options.ecLevel = 0;
        options.alignment = 'center';
      } else if (this.selectedProtocol === 'CPCL') {
        options.x = 50;
        options.y = 50;
        options.model = 2;
        options.unitWidth = 6;
      } else if (this.selectedProtocol === 'TSPL') {
        options.x = 50;
        options.y = 50;
        options.ecLevel = 'M';
        options.cellWidth = 4;
        options.mode = 'A';
        options.rotation = 0;
        options.model = 'M2';
      } else if (this.selectedProtocol === 'ZPL') {
        options.x = 50;
        options.y = 50;
        options.model = 2;
        options.magnification = 3;
        options.errorCorrection = 'M';
        options.maskValue = 0;
      }
      
      const res = await this.xprinter.printQRCode(options);
      alert(res.msg || 'In QR code thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in QR code');
    }
  }

  async onPrintBarcode() {
    const data = prompt('Nhập dữ liệu barcode', '1234567890');
    if (!data) return;
    try {
      const options: any = { data };
      
      if (this.selectedProtocol === 'POS') {
        options.codeType = 73; // CODE128
        options.width = 2;
        options.height = 162;
        options.alignment = 'center';
        options.textPosition = 2;
      } else if (this.selectedProtocol === 'CPCL') {
        options.x = 50;
        options.y = 50;
        options.codeType = '128';
        options.height = 32;
        options.readable = 1;
      } else if (this.selectedProtocol === 'TSPL') {
        options.x = 50;
        options.y = 50;
        options.codeType = '128';
        options.height = 40;
        options.readable = 1;
        options.rotation = 0;
        options.narrow = 2;
        options.wide = 2;
      } else if (this.selectedProtocol === 'ZPL') {
        options.x = 50;
        options.y = 50;
        options.codeType = '^BC';
        options.orientation = 'N';
        options.height = 100;
        options.printInterpretationLine = 'Y';
        options.printInterpretationLineAbove = 'N';
        options.checkDigit = 'N';
      }
      
      const res = await this.xprinter.printBarcode(options);
      alert(res.msg || 'In barcode thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in barcode');
    }
  }

  async onPrintImagePath() {
    const imagePath = prompt('Nhập đường dẫn hình ảnh', '/storage/emulated/0/Download/test.png');
    if (!imagePath) return;
    try {
      const options: any = { imagePath };
      
      if (this.selectedProtocol === 'POS') {
        options.width = 200;
        options.alignment = 'center';
      } else if (this.selectedProtocol === 'CPCL') {
        options.x = 50;
        options.y = 50;
        options.mode = 0;
      } else if (this.selectedProtocol === 'TSPL') {
        options.x = 50;
        options.y = 50;
        options.mode = 'OVERWRITE';
      } else if (this.selectedProtocol === 'ZPL') {
        options.x = 50;
        options.y = 50;
        options.width = 200;
        options.height = 200;
      }
      
      const res = await this.xprinter.printImageFromPath(options);
      alert(res.msg || 'In hình ảnh thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in hình ảnh');
    }
  }

  async onPrintImageBase64() {
    const base64 = prompt('Nhập chuỗi base64 của hình ảnh', 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==');
    if (!base64) return;
    try {
      const options: any = { base64 };
      
      if (this.selectedProtocol === 'POS') {
        options.width = 200;
        options.alignment = 'center';
      } else if (this.selectedProtocol === 'CPCL') {
        options.x = 50;
        options.y = 50;
        options.mode = 0;
      } else if (this.selectedProtocol === 'TSPL') {
        options.x = 50;
        options.y = 50;
        options.mode = 'OVERWRITE';
      } else if (this.selectedProtocol === 'ZPL') {
        options.x = 50;
        options.y = 50;
        options.width = 200;
        options.height = 200;
      }
      
      const res = await this.xprinter.printImageBase64(options);
      alert(res.msg || 'In hình ảnh base64 thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in hình ảnh base64');
    }
  }

  async onSendRawData() {
    const hex = prompt('Nhập dữ liệu hex', '1B4000');
    if (!hex) return;
    try {
      const res = await this.xprinter.sendRawData({ hex });
      alert(res.msg || 'Gửi dữ liệu thô thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi gửi dữ liệu thô');
    }
  }

  async onCutPaper() {
    try {
      const res = await this.xprinter.cutPaper();
      alert(res.msg || 'Cắt giấy thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi cắt giấy');
    }
  }

  async onOpenCashDrawer() {
    try {
      const res = await this.xprinter.openCashDrawer();
      alert(res.msg || 'Mở két tiền thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi mở két tiền');
    }
  }

  async onSelfTest() {
    try {
      const res = await this.xprinter.selfTest();
      alert(res.msg || 'Self test thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi self test');
    }
  }

  async onPrintLabel() {
    let command = '';
    
    if (this.selectedProtocol === 'CPCL') {
      command = prompt('Nhập lệnh CPCL', '! 0 200 200 210 1\nTEXT 4 0 30 40 Hello CPCL\nPRINT\n') || '';
    } else if (this.selectedProtocol === 'TSPL') {
      command = prompt('Nhập lệnh TSPL', 'SIZE 40 mm, 30 mm\nGAP 3 mm, 0 mm\nDIRECTION 1\nCLS\nTEXT 100,100,"3",0,1,1,"Hello TSPL"\nPRINT 1,1\n') || '';
    } else if (this.selectedProtocol === 'ZPL') {
      command = prompt('Nhập lệnh ZPL', '^XA\n^FO50,50^A0N,50,50^FDHello ZPL^FS\n^XZ\n') || '';
    }
    
    if (!command) return;
    
    try {
      const res = await this.xprinter.printLabel({ command });
      alert(res.msg || 'In label thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in label');
    }
  }

  async onGetPrinterStatus() {
    try {
      const res = await this.xprinter.getPrinterStatus();
      alert('Trạng thái máy in: ' + (res.data || res.msg || 'OK'));
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi lấy trạng thái máy in');
    }
  }

  async onResetPrinter() {
    try {
      const res = await this.xprinter.resetPrinter();
      alert(res.msg || 'Reset máy in thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi reset máy in');
    }
  }
}
