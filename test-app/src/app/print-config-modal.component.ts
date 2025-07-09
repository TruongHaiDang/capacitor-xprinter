import { Component, Input } from '@angular/core';
import {
  ModalController,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonContent,
  IonItem,
  IonLabel,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonToggle
} from '@ionic/angular/standalone';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-print-config-modal',
  standalone: true,
  imports: [
    IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonContent, IonItem, IonLabel, IonInput, IonSelect, IonSelectOption, FormsModule, CommonModule, IonToggle
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Cấu hình {{ typeLabel }}</ion-title>
        <ion-buttons slot="end">
          <ion-button (click)="dismiss()">Đóng</ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      <form>
        <!-- TEXT -->
        <ng-container *ngIf="type === 'text'">
          <ion-item>
            <ion-label position="floating">Nội dung</ion-label>
            <ion-input [(ngModel)]="config.text" name="text"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Căn lề</ion-label>
            <ion-select [(ngModel)]="config.alignment" name="alignment">
              <ion-select-option value="left">Trái</ion-select-option>
              <ion-select-option value="center">Giữa</ion-select-option>
              <ion-select-option value="right">Phải</ion-select-option>
            </ion-select>
          </ion-item>
          <ng-container *ngIf="protocol === 'POS'">
            <ion-item>
              <ion-label position="floating">Attribute</ion-label>
              <ion-input type="number" [(ngModel)]="config.attribute" name="attribute"></ion-input>
            </ion-item>
            <ion-item>
              <ion-label position="floating">Text Size</ion-label>
              <ion-input type="number" [(ngModel)]="config.textSize" name="textSize"></ion-input>
            </ion-item>
            <ion-item>
              <ion-label position="floating">Font</ion-label>
              <ion-input type="number" [(ngModel)]="config.font" name="font"></ion-input>
            </ion-item>
            <ion-item>
              <ion-label position="floating">Line Spacing</ion-label>
              <ion-input type="number" [(ngModel)]="config.lineSpacing" name="lineSpacing"></ion-input>
            </ion-item>
            <ion-item>
              <ion-label position="floating">Code Page</ion-label>
              <ion-input type="number" [(ngModel)]="config.codePage" name="codePage"></ion-input>
            </ion-item>
            <ion-item>
              <ion-label position="floating">Char Right Space</ion-label>
              <ion-input type="number" [(ngModel)]="config.charRightSpace" name="charRightSpace"></ion-input>
            </ion-item>
            <ion-item>
              <ion-label>Upside Down</ion-label>
              <ion-toggle [(ngModel)]="config.upsideDown" name="upsideDown"></ion-toggle>
            </ion-item>
          </ng-container>
          <!-- Các trường cho CPCL, TSPL, ZPL giữ nguyên như cũ -->
          <ion-item *ngIf="protocol === 'CPCL' || protocol === 'TSPL' || protocol === 'ZPL'">
            <ion-label position="floating">X</ion-label>
            <ion-input type="number" [(ngModel)]="config.x" name="x"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'CPCL' || protocol === 'TSPL' || protocol === 'ZPL'">
            <ion-label position="floating">Y</ion-label>
            <ion-input type="number" [(ngModel)]="config.y" name="y"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'CPCL'">
            <ion-label position="floating">Font</ion-label>
            <ion-input type="number" [(ngModel)]="config.font" name="font"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'TSPL'">
            <ion-label position="floating">Font</ion-label>
            <ion-input [(ngModel)]="config.font" name="font"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'ZPL'">
            <ion-label position="floating">Font</ion-label>
            <ion-input [(ngModel)]="config.font" name="font"></ion-input>
          </ion-item>
        </ng-container>

        <!-- BARCODE -->
        <ng-container *ngIf="type === 'barcode'">
          <ion-item>
            <ion-label position="floating">Dữ liệu</ion-label>
            <ion-input [(ngModel)]="config.data" name="data"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Loại mã vạch</ion-label>
            <ion-select [(ngModel)]="config.codeType" name="codeType">
              <ion-select-option [value]="65">UPC-A</ion-select-option>
              <ion-select-option [value]="67">EAN13</ion-select-option>
              <ion-select-option [value]="73">Code128</ion-select-option>
              <ion-select-option [value]="69">CODE39</ion-select-option>
              <ion-select-option [value]="71">ITF</ion-select-option>
              <ion-select-option [value]="72">CODABAR</ion-select-option>
              <!-- Thêm các loại khác nếu cần -->
            </ion-select>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Độ rộng thanh (width)</ion-label>
            <ion-input type="number" [(ngModel)]="config.width" name="width"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Chiều cao (height)</ion-label>
            <ion-input type="number" [(ngModel)]="config.height" name="height"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Căn lề</ion-label>
            <ion-select [(ngModel)]="config.alignment" name="alignment">
              <ion-select-option value="left">Trái</ion-select-option>
              <ion-select-option value="center">Giữa</ion-select-option>
              <ion-select-option value="right">Phải</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Vị trí hiển thị text</ion-label>
            <ion-select [(ngModel)]="config.textPosition" name="textPosition">
              <ion-select-option [value]="0">Không hiển thị</ion-select-option>
              <ion-select-option [value]="1">Trên</ion-select-option>
              <ion-select-option [value]="2">Dưới</ion-select-option>
              <ion-select-option [value]="3">Cả hai</ion-select-option>
            </ion-select>
          </ion-item>
          <!-- Các trường cho CPCL, TSPL, ZPL tương tự, tuỳ theo options từng loại -->
        </ng-container>

        <!-- QRCODE -->
        <ng-container *ngIf="type === 'qr'">
          <ion-item>
            <ion-label position="floating">Dữ liệu</ion-label>
            <ion-input [(ngModel)]="config.data" name="data"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Căn lề</ion-label>
            <ion-select [(ngModel)]="config.alignment" name="alignment">
              <ion-select-option value="left">Trái</ion-select-option>
              <ion-select-option value="center">Giữa</ion-select-option>
              <ion-select-option value="right">Phải</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Kích thước module</ion-label>
            <ion-input type="number" [(ngModel)]="config.moduleSize" name="moduleSize"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Mức sửa lỗi (EC Level)</ion-label>
            <ion-select [(ngModel)]="config.errorCorrectionLevel" name="errorCorrectionLevel">
              <ion-select-option [value]="0">L (7%)</ion-select-option>
              <ion-select-option [value]="1">M (15%)</ion-select-option>
              <ion-select-option [value]="2">Q (25%)</ion-select-option>
              <ion-select-option [value]="3">H (30%)</ion-select-option>
            </ion-select>
          </ion-item>
        </ng-container>

        <!-- IMAGE -->
        <ng-container *ngIf="type === 'image'">
          <ion-item>
            <ion-label position="floating">Đường dẫn/Base64</ion-label>
            <ion-input [(ngModel)]="config.bitmap" name="bitmap"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Căn lề</ion-label>
            <ion-select [(ngModel)]="config.alignment" name="alignment">
              <ion-select-option value="left">Trái</ion-select-option>
              <ion-select-option value="center">Giữa</ion-select-option>
              <ion-select-option value="right">Phải</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Độ rộng (px)</ion-label>
            <ion-input type="number" [(ngModel)]="config.width" name="width"></ion-input>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Chế độ in ảnh</ion-label>
            <ion-select [(ngModel)]="config.mode" name="mode">
              <ion-select-option value="0">Bình thường</ion-select-option>
              <ion-select-option value="1">Double Width</ion-select-option>
              <ion-select-option value="2">Double Height</ion-select-option>
              <ion-select-option value="3">Double Width & Height</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item *ngIf="protocol === 'POS'">
            <ion-label position="floating">Độ đậm nhạt (density)</ion-label>
            <ion-input type="number" [(ngModel)]="config.density" name="density"></ion-input>
          </ion-item>
        </ng-container>

        <ion-button expand="block" (click)="onPrint()">In</ion-button>
      </form>
    </ion-content>
  `
})
export class PrintConfigModalComponent {
  @Input() type: string = 'text';
  @Input() protocol: string = 'POS';
  @Input() config: any = {};

  get typeLabel() {
    switch (this.type) {
      case 'text': return 'Text';
      case 'barcode': return 'Barcode';
      case 'qr': return 'QR Code';
      case 'image': return 'Image';
      default: return '';
    }
  }
  constructor(private modalCtrl: ModalController) {}
  dismiss() { this.modalCtrl.dismiss(); }
  onPrint() { this.modalCtrl.dismiss(this.config); }
}
