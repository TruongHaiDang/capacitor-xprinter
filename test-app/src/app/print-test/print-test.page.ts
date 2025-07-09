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
  ModalController,
  IonItem,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonToggle,
  IonButton
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
import { Camera, CameraResultType, CameraSource, Photo } from '@capacitor/camera';
import { PrintConfigModalComponent } from '../print-config-modal.component';

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
  // providers: [ModalController] // Không cần thiết vì ModalController là service gốc của Ionic
})
export class PrintTestPage implements OnInit {
  selectedProtocol: string = 'POS';
  labelConfig: { width: number; height: number; gap?: number; offset?: number; direction?: number; quantity?: number; length?: number; speed?: number } = { width: 60, height: 40, gap: 2 };
  barcodeConfig: { height?: number; width?: number; codeType?: string; readable?: number; rotation?: number; narrow?: number; wide?: number } = { height: 40, width: 2 };
  qrcodeConfig: { size?: number; model?: number; unitWidth?: number; ecLevel?: number } = { size: 6 };
  imageConfig: { mode?: number; width?: number; height?: number } = { mode: 0 };
  textConfig: {
    alignment: 'left' | 'center' | 'right';
    attribute: number;
    textSize: number;
    font: number;
    lineSpacing: number;
    codePage: number;
    charRightSpace: number;
    upsideDown: boolean;
  } = {
    alignment: 'left',
    attribute: 0,
    textSize: 0,
    font: 0,
    lineSpacing: 30,
    codePage: 0,
    charRightSpace: 0,
    upsideDown: false
  };
  customCommand: string = '';
  // customPosCommand: string = '';

  constructor(private xprinter: XprinterService, private modalCtrl: ModalController) {
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

  async openPrintConfigModal(type: string, defaultConfig: any) {
    const modal = await this.modalCtrl.create({
      component: PrintConfigModalComponent,
      componentProps: { type, protocol: this.selectedProtocol, config: { ...defaultConfig } }
    });
    await modal.present();
    const { data } = await modal.onDidDismiss();
    return data;
  }

  getDefaultConfig(type: string, protocol: string) {
    if (type === 'text') {
      if (protocol === 'POS') return { text: 'Hello XPrinter', alignment: 'left', textSize: 0 };
      if (protocol === 'CPCL') return { text: 'Hello XPrinter', x: 0, y: 0, font: 0, rotation: 0 };
      if (protocol === 'TSPL') return { text: 'Hello XPrinter', x: 0, y: 0, font: '0', rotation: 0, xScale: 1, yScale: 1 };
      if (protocol === 'ZPL') return { text: 'Hello XPrinter', x: 0, y: 0, font: 'A', orientation: 'N', height: 30, width: 30 };
    }
    if (type === 'barcode') {
      if (protocol === 'POS') return { data: '0123456789012', codeType: 67, width: 3, height: 120, alignment: 'center', textPosition: 2 };
      if (protocol === 'CPCL') return { data: '1234567890', x: 50, y: 50, codeType: '128', height: 32, readable: 1 };
      if (protocol === 'TSPL') return { data: '1234567890', x: 50, y: 50, codeType: '128', height: 40, readable: 1, rotation: 0, narrow: 2, wide: 2 };
      if (protocol === 'ZPL') return { data: '1234567890', x: 50, y: 50, codeType: '^BC', orientation: 'N', height: 100, printInterpretationLine: 'Y', printInterpretationLineAbove: 'N', checkDigit: 'N' };
    }
    if (type === 'qr') {
      if (protocol === 'POS') return { data: 'https://xprinter.net', alignment: 'center', moduleSize: 4, errorCorrectionLevel: 0 };
      if (protocol === 'CPCL') return { data: 'https://xprinter.net', x: 50, y: 50, model: 2, unitWidth: 6 };
      if (protocol === 'TSPL') return { data: 'https://xprinter.net', x: 50, y: 50, ecLevel: 'M', cellWidth: 4, mode: 'A', rotation: 0, model: 'M2' };
      if (protocol === 'ZPL') return { data: 'https://xprinter.net', x: 50, y: 50, model: 2, magnification: 3, errorCorrection: 'M', maskValue: 0 };
    }
    if (type === 'image') {
      if (protocol === 'POS') return { bitmap: '', alignment: 'center', width: 200, mode: 0, density: 0 };
      if (protocol === 'CPCL') return { imagePath: '', x: 50, y: 50, mode: 0, base64: '' };
      if (protocol === 'TSPL') return { imagePath: '', x: 50, y: 50, mode: 'OVERWRITE', base64: '' };
      if (protocol === 'ZPL') return { imagePath: '', x: 50, y: 50, width: 200, height: 200, base64: '' };
    }
    return {};
  }

  async onPrintText() {
    const defaultConfig = this.getDefaultConfig('text', this.selectedProtocol);
    const config = await this.openPrintConfigModal('text', defaultConfig);
    if (!config) return;
    try {
      // Gọi configText trước với các trường cấu hình
      await this.xprinter.configText({
        alignment: config.alignment,
        attribute: config.attribute,
        textSize: config.textSize,
        font: config.font,
        lineSpacing: config.lineSpacing,
        codePage: config.codePage,
        charRightSpace: config.charRightSpace,
        upsideDown: config.upsideDown,
        language: this.selectedProtocol
      });
      // Sau đó chỉ truyền text vào printText
      const res = await this.xprinter.printText(config.text);
      alert(res.msg || 'In thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in');
    }
  }

  async onPrintQRCode() {
    const defaultConfig = this.getDefaultConfig('qr', this.selectedProtocol);
    const config = await this.openPrintConfigModal('qr', defaultConfig);
    if (!config) return;
    try {
      let res;
      if (this.selectedProtocol === 'POS') {
        await this.xprinter.configQRCode(config);
        res = await this.xprinter.printQRCode({ data: config.data });
      } else {
        res = await this.xprinter.printQRCode(config);
      }
      alert(res.msg || 'In QR code thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in QR code');
    }
  }

  async onPrintBarcode() {
    const defaultConfig = this.getDefaultConfig('barcode', this.selectedProtocol);
    const config = await this.openPrintConfigModal('barcode', defaultConfig);
    if (!config) return;
    try {
      let res;
      if (this.selectedProtocol === 'POS') {
        await this.xprinter.configBarcode(config);
        res = await this.xprinter.printBarcode({ data: config.data });
      } else {
        res = await this.xprinter.printBarcode(config);
      }
      alert(res.msg || 'In barcode thành công');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi in barcode');
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

  /**
   * Chọn ảnh từ thư viện và in bằng path/URI (chỉ dùng path thực, không dùng webPath)
   */
  async selectImageFromLibraryAndPrintPath() {
    try {
      const photo: Photo = await Camera.getPhoto({
        quality: 90,
        allowEditing: false,
        resultType: CameraResultType.Uri,
        source: CameraSource.Photos,
      });
      const filePath = photo.path; // Chỉ dùng path thực
      if (!filePath) {
        alert('Không lấy được file path thực từ ảnh!\nChỉ có thể in bằng base64.');
        return;
      }
      const defaultConfig = this.getDefaultConfig('image', this.selectedProtocol);
      defaultConfig.bitmap = filePath;
      const config = await this.openPrintConfigModal('image', defaultConfig);
      if (!config) return;
      let res;
      if (this.selectedProtocol === 'POS') {
        await this.xprinter.configImage(config);
        res = await this.xprinter.printImageFromPath({ bitmap: config.bitmap });
      } else {
        res = await this.xprinter.printImageFromPath(config);
      }
      alert(res.msg || 'In ảnh từ thư viện (path) thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi chọn/in ảnh từ thư viện');
    }
  }

  /**
   * Chụp ảnh và in bằng base64
   */
  async captureImageAndPrintBase64() {
    try {
      const photo: Photo = await Camera.getPhoto({
        quality: 90,
        allowEditing: false,
        resultType: CameraResultType.Base64,
        source: CameraSource.Camera,
      });
      const base64 = photo.base64String;
      if (!base64) {
        alert('Không lấy được base64 ảnh!');
        return;
      }
      const defaultConfig = this.getDefaultConfig('image', this.selectedProtocol);
      defaultConfig.bitmap = base64;
      const config = await this.openPrintConfigModal('image', defaultConfig);
      if (!config) return;
      let res;
      if (this.selectedProtocol === 'POS') {
        await this.xprinter.configImage(config);
        res = await this.xprinter.printImageBase64({ bitmap: config.bitmap });
      } else {
        res = await this.xprinter.printImageBase64(config);
      }
      alert(res.msg || 'In ảnh chụp (base64) thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi chụp/in ảnh');
    }
  }

  /*
   * Cấu hình thông số label cho CPCL / TSPL / ZPL
   * Sau khi cấu hình thành công có thể gọi các hàm printText / printBarcode / ... để vẽ nội dung.
   */
  async onConfigLabel() {
    try {
      // Không truyền width/height nếu muốn dùng mặc định của máy in (service sẽ tự gán)
      const res = await this.xprinter.configLabel();
      alert(res.msg || 'Cấu hình label thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi cấu hình label');
    }
  }

  async onConfigBarcode() {
    try {
      const res = await this.xprinter.configBarcode(this.barcodeConfig);
      alert(res.msg || 'Cấu hình barcode thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi cấu hình barcode');
    }
  }

  async onConfigQRCode() {
    try {
      const res = await this.xprinter.configQRCode(this.qrcodeConfig);
      alert(res.msg || 'Cấu hình QRCode thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi cấu hình QRCode');
    }
  }

  async onConfigImage() {
    try {
      const res = await this.xprinter.configImage(this.imageConfig);
      alert(res.msg || 'Cấu hình image thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi cấu hình image');
    }
  }

  async onConfigText() {
    try {
      const config = {
        alignment: this.textConfig.alignment,
        attribute: this.textConfig.attribute,
        textSize: this.textConfig.textSize,
        font: this.textConfig.font,
        lineSpacing: this.textConfig.lineSpacing,
        codePage: this.textConfig.codePage,
        charRightSpace: this.textConfig.charRightSpace,
        upsideDown: this.textConfig.upsideDown,
        language: this.selectedProtocol
      };
      const res = await this.xprinter.configText(config);
      alert(res.msg || 'Cấu hình text thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Lỗi cấu hình text');
    }
  }

  async onSendCommand(): Promise<void> {
    if (!this.customCommand?.trim()) {
      alert('Vui lòng nhập lệnh máy in!');
      return;
    }
    try {
      const res = await this.xprinter.sendCommand(this.customCommand);
      alert(res?.msg || 'Gửi lệnh thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Gửi lệnh thất bại!');
    }
  }

  async onSendPosCommand(): Promise<void> {
    const command = prompt('Nhập lệnh ESC/POS (hex hoặc text)', '1B4000');
    if (!command || !command.trim()) {
      return;
    }
    try {
      const res = await this.xprinter.sendPosCommand(command.trim());
      alert(res?.msg || 'Gửi lệnh POS thành công!');
    } catch (err: any) {
      alert(err?.msg || err?.message || 'Gửi lệnh POS thất bại!');
    }
  }
}
