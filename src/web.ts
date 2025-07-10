import { WebPlugin } from '@capacitor/core';

import type { CapacitorXprinterPlugin } from './definitions';

export class CapacitorXprinter extends WebPlugin implements CapacitorXprinterPlugin {
  // ===== HANDSHAKE =====
  async connect(_options: import('./models').ConnectOptions): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async disconnect(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  /**
   * Kiểm tra trạng thái kết nối máy in (đã kết nối hay chưa) - luôn trả về false trên web
   */
  async isConnected(): Promise<{ connected: boolean }> {
    return Promise.resolve({ connected: false });
  }

  async listAvailablePorts(_options: { type: 'USB' | 'BLUETOOTH' | 'SERIAL' }): Promise<{ ports: string[] }> {
    return Promise.resolve({ ports: [] });
  }

  // ===== PRINT =====
  async printText(_options: {
    text: string;
    alignment?: 'left' | 'center' | 'right';
    textSize?: number;
    attribute?: number;
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printEncodedText(_options: {
    text: string;
    encoding: 'gbk' | 'utf-8' | 'shift-jis';
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printQRCode(_options: {
    data: string;
    moduleSize?: number;
    ecLevel?: number;
    alignment?: 'left' | 'center' | 'right';
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printBarcode(_options: {
    data: string;
    codeType: number;
    width?: number;
    height?: number;
    alignment?: 'left' | 'center' | 'right';
    textPosition?: number;
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printImageFromPath(_options: {
    imagePath: string;
    width?: number;
    alignment?: 'left' | 'center' | 'right';
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printImageBase64(_options: {
    base64: string;
    width?: number;
    alignment?: 'left' | 'center' | 'right';
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printLabel(_options: { command: string }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  // ===== PRINTER CONTROL =====
  async cutPaper(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async openCashDrawer(_options?: {
    pinNum?: number;
    onTime?: number;
    offTime?: number;
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async resetPrinter(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async selfTest(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async setProtocol(_options: {
    protocol: 'POS' | 'CPCL' | 'TSPL' | 'ZPL';
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  // ===== STATUS & DATA =====
  async getPrinterStatus(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async readData(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async sendRawData(_options: { hex: string }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async sendBatchCommands(_options: {
    commands: string[];
    delayBetween?: number;
  }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  // ===== CONFIG =====
  // Hàm cấu hình cho in text - không hỗ trợ trên web
  async configText(_options: Record<string, any>): Promise<any> {
    return Promise.reject({ code: -1, msg: 'Không hỗ trợ trên web', data: null });
  }

  // Hàm cấu hình cho in barcode - không hỗ trợ trên web
  async configBarcode(_options: Record<string, any>): Promise<any> {
    return Promise.reject({ code: -1, msg: 'Không hỗ trợ trên web', data: null });
  }

  // Hàm cấu hình cho in QRCode - không hỗ trợ trên web
  async configQRCode(_options: Record<string, any>): Promise<any> {
    return Promise.reject({ code: -1, msg: 'Không hỗ trợ trên web', data: null });
  }

  // Hàm cấu hình cho in hình ảnh - không hỗ trợ trên web
  async configImage(_options: Record<string, any>): Promise<any> {
    return Promise.reject({ code: -1, msg: 'Không hỗ trợ trên web', data: null });
  }

  // Hàm cấu hình cho in label (CPCL/TSPL/ZPL) - không hỗ trợ trên web
  async configLabel(_options: Record<string, any>): Promise<any> {
    return Promise.reject({ code: -1, msg: 'Không hỗ trợ trên web', data: null });
  }

  // ===== UTILS =====
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
