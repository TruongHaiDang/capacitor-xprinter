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
          <ion-item *ngIf="type === 'text' && protocol === 'POS'">
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

        <ng-container *ngIf="type === 'text' && protocol === 'TSPL'">
          <ion-item>
            <ion-label position="floating">Vị trí ngang (x)</ion-label>
            <ion-input type="number" [(ngModel)]="config.x" name="x"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Vị trí dọc (y)</ion-label>
            <ion-input type="number" [(ngModel)]="config.y" name="y"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Font chữ</ion-label>
            <ion-input [(ngModel)]="config.font" name="font"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Góc xoay</ion-label>
            <ion-select [(ngModel)]="config.rotation" name="rotation">
              <ion-select-option [value]="0">0°</ion-select-option>
              <ion-select-option [value]="90">90°</ion-select-option>
              <ion-select-option [value]="180">180°</ion-select-option>
              <ion-select-option [value]="270">270°</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Tỉ lệ ngang (xScale)</ion-label>
            <ion-input type="number" [(ngModel)]="config.xScale" name="xScale"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Tỉ lệ dọc (yScale)</ion-label>
            <ion-input type="number" [(ngModel)]="config.yScale" name="yScale"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Nội dung</ion-label>
            <ion-input [(ngModel)]="config.content" name="content"></ion-input>
          </ion-item>
          <!-- Bổ sung các trường cấu hình nhãn, reference, density, speed, direction, mirror -->
          <ion-item>
            <ion-label position="floating">Chiều rộng nhãn (sizeWidthMm, mm)</ion-label>
            <ion-input type="number" [(ngModel)]="config.sizeWidthMm" name="sizeWidthMm"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Chiều cao nhãn (sizeHeightMm, mm)</ion-label>
            <ion-input type="number" [(ngModel)]="config.sizeHeightMm" name="sizeHeightMm"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Khoảng cách giữa nhãn (gapMmM, mm)</ion-label>
            <ion-input type="number" [(ngModel)]="config.gapMmM" name="gapMmM"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Khoảng dịch chuyển nhãn (gapMmN, mm)</ion-label>
            <ion-input type="number" [(ngModel)]="config.gapMmN" name="gapMmN"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Toạ độ gốc X (referenceX, dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.referenceX" name="referenceX"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Toạ độ gốc Y (referenceY, dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.referenceY" name="referenceY"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Độ đậm nét (density, 0-15)</ion-label>
            <ion-input type="number" min="0" max="15" [(ngModel)]="config.density" name="density"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Tốc độ in (speed, mm/s)</ion-label>
            <ion-input type="number" step="0.1" [(ngModel)]="config.speed" name="speed"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Hướng in (direction)</ion-label>
            <ion-select [(ngModel)]="config.direction" name="direction">
              <ion-select-option [value]="0">Forward</ion-select-option>
              <ion-select-option [value]="1">Reverse</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label>In mirror (đảo chiều ngang)</ion-label>
            <ion-toggle [(ngModel)]="config.mirror" name="mirror"></ion-toggle>
          </ion-item>
        </ng-container>

        <ng-container *ngIf="type === 'barcode' && protocol === 'TSPL'">
          <ion-item>
            <ion-label position="floating">Vị trí ngang (x)</ion-label>
            <ion-input type="number" [(ngModel)]="config.x" name="x"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Vị trí dọc (y)</ion-label>
            <ion-input type="number" [(ngModel)]="config.y" name="y"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Loại mã vạch</ion-label>
            <ion-input [(ngModel)]="config.type" name="type"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Chiều cao</ion-label>
            <ion-input type="number" [(ngModel)]="config.height" name="height"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Hiển thị text</ion-label>
            <ion-input type="number" [(ngModel)]="config.readable" name="readable"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Góc xoay</ion-label>
            <ion-select [(ngModel)]="config.rotation" name="rotation">
              <ion-select-option [value]="0">0°</ion-select-option>
              <ion-select-option [value]="90">90°</ion-select-option>
              <ion-select-option [value]="180">180°</ion-select-option>
              <ion-select-option [value]="270">270°</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Độ rộng vạch nhỏ (narrow)</ion-label>
            <ion-input type="number" [(ngModel)]="config.narrow" name="narrow"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Độ rộng vạch lớn (wide)</ion-label>
            <ion-input type="number" [(ngModel)]="config.wide" name="wide"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Dữ liệu mã vạch</ion-label>
            <ion-input [(ngModel)]="config.content" name="content"></ion-input>
          </ion-item>
        </ng-container>

        <ng-container *ngIf="type === 'qr' && protocol === 'TSPL'">
          <ion-item>
            <ion-label position="floating">Vị trí ngang (x)</ion-label>
            <ion-input type="number" [(ngModel)]="config.x" name="x"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Vị trí dọc (y)</ion-label>
            <ion-input type="number" [(ngModel)]="config.y" name="y"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Mức sửa lỗi (ecLevel)</ion-label>
            <ion-select [(ngModel)]="config.ecLevel" name="ecLevel">
              <ion-select-option value="L">L</ion-select-option>
              <ion-select-option value="M">M</ion-select-option>
              <ion-select-option value="Q">Q</ion-select-option>
              <ion-select-option value="H">H</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Kích thước ô (cellWidth)</ion-label>
            <ion-input type="number" [(ngModel)]="config.cellWidth" name="cellWidth"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Chế độ mã hóa (mode)</ion-label>
            <ion-select [(ngModel)]="config.mode" name="mode">
              <ion-select-option value="A">Auto</ion-select-option>
              <ion-select-option value="M">Manual</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Góc xoay</ion-label>
            <ion-select [(ngModel)]="config.rotation" name="rotation">
              <ion-select-option [value]="0">0°</ion-select-option>
              <ion-select-option [value]="90">90°</ion-select-option>
              <ion-select-option [value]="180">180°</ion-select-option>
              <ion-select-option [value]="270">270°</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Model</ion-label>
            <ion-select [(ngModel)]="config.model" name="model">
              <ion-select-option value="M1">M1</ion-select-option>
              <ion-select-option value="M2">M2</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Mặt nạ (mask)</ion-label>
            <ion-input [(ngModel)]="config.mask" name="mask"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Nội dung</ion-label>
            <ion-input [(ngModel)]="config.content" name="content"></ion-input>
          </ion-item>
        </ng-container>

        <ng-container *ngIf="type === 'image' && protocol === 'TSPL'">
          <ion-item>
            <ion-label position="floating">Vị trí ngang (x, đơn vị: dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.x" name="x"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Vị trí dọc (y, đơn vị: dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.y" name="y"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Độ rộng ảnh (width, đơn vị: dot, scale ngang)</ion-label>
            <ion-input type="number" [(ngModel)]="config.width" name="width"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Chế độ in (mode)</ion-label>
            <ion-select [(ngModel)]="config.mode" name="mode">
              <ion-select-option value="OVERWRITE">OVERWRITE</ion-select-option>
              <ion-select-option value="OR">OR</ion-select-option>
              <ion-select-option value="XOR">XOR</ion-select-option>
              <ion-select-option value="OVERWRITE_C">OVERWRITE_C</ion-select-option>
              <ion-select-option value="OR_C">OR_C</ion-select-option>
              <ion-select-option value="XOR_C">XOR_C</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Thuật toán chuyển ảnh (algorithm)</ion-label>
            <ion-select [(ngModel)]="config.algorithm" name="algorithm">
              <ion-select-option value="Threshold">Threshold</ion-select-option>
              <ion-select-option value="Dithering">Dithering</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Độ đậm khi in (density, 0-15)</ion-label>
            <ion-input type="number" min="0" max="15" [(ngModel)]="config.density" name="density"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Tốc độ in (speed, ví dụ: 2.0 - 5.0)</ion-label>
            <ion-input type="number" step="0.1" [(ngModel)]="config.speed" name="speed"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Hướng in (direction)</ion-label>
            <ion-select [(ngModel)]="config.direction" name="direction">
              <ion-select-option value="FORWARD">FORWARD</ion-select-option>
              <ion-select-option value="REVERSE">REVERSE</ion-select-option>
            </ion-select>
          </ion-item>
          <ion-item>
            <ion-label>Đảo ảnh ngang (mirror)</ion-label>
            <ion-toggle [(ngModel)]="config.mirror" name="mirror"></ion-toggle>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Gốc toạ độ in (reference x, dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.referenceX" name="referenceX"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Gốc toạ độ in (reference y, dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.referenceY" name="referenceY"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Kích thước nhãn (width, mm)</ion-label>
            <ion-input type="number" [(ngModel)]="config.sizeWidthMm" name="sizeWidthMm"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Kích thước nhãn (height, mm)</ion-label>
            <ion-input type="number" [(ngModel)]="config.sizeHeightMm" name="sizeHeightMm"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Khoảng cách giữa nhãn (gap m, mm)</ion-label>
            <ion-input type="number" [(ngModel)]="config.gapMmM" name="gapMmM"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Khoảng cách giữa nhãn (gap n, mm)</ion-label>
            <ion-input type="number" [(ngModel)]="config.gapMmN" name="gapMmN"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label>Làm sạch vùng in trước khi in (cls)</ion-label>
            <ion-toggle [(ngModel)]="config.cls" name="cls"></ion-toggle>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Xoá vùng ảnh (erase x, dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.eraseX" name="eraseX"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Xoá vùng ảnh (erase y, dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.eraseY" name="eraseY"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Xoá vùng ảnh (erase width, dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.eraseWidth" name="eraseWidth"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Xoá vùng ảnh (erase height, dot)</ion-label>
            <ion-input type="number" [(ngModel)]="config.eraseHeight" name="eraseHeight"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label>In ảnh dạng nén (bitmapCompression)</ion-label>
            <ion-toggle [(ngModel)]="config.bitmapCompression" name="bitmapCompression"></ion-toggle>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Ảnh (bitmap/base64)</ion-label>
            <ion-input [(ngModel)]="config.bitmap" name="bitmap"></ion-input>
          </ion-item>
          <ion-item>
            <ion-label position="floating">Thuật toán chuyển ảnh (algorithm)</ion-label>
            <ion-input [(ngModel)]="config.algorithm" name="algorithm"></ion-input>
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
