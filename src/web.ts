import { WebPlugin } from '@capacitor/core';

import type { CapacitorXprinterPlugin } from './definitions';

export class CapacitorXprinter extends WebPlugin implements CapacitorXprinterPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async initialize(): Promise<void> {
    return Promise.reject('Not supported on web');
  }

  async connect(_options: import('./models').ConnectOptions): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async disconnect(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printText(_options: { text: string; alignment?: 'left' | 'center' | 'right'; textSize?: number; attribute?: number; }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printQRCode(_options: { data: string; moduleSize?: number; ecLevel?: number; alignment?: 'left' | 'center' | 'right'; }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printBarcode(_options: { data: string; codeType: number; width?: number; height?: number; alignment?: 'left' | 'center' | 'right'; textPosition?: number; }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printImageFromPath(_options: { imagePath: string; width?: number; alignment?: 'left' | 'center' | 'right'; }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async cutPaper(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async openCashDrawer(_options?: { pinNum?: number; onTime?: number; offTime?: number }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async getPrinterStatus(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async readData(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async sendRawData(_options: { hex: string }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printImageBase64(_options: { base64: string; width?: number; alignment?: 'left' | 'center' | 'right'; }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async printLabel(_options: { command: string }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async resetPrinter(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async selfTest(): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async listAvailablePorts(_options: { type: 'USB' | 'BLUETOOTH' | 'SERIAL' }): Promise<{ ports: string[] }> {
    return Promise.resolve({ ports: [] });
  }

  async printEncodedText(_options: { text: string; encoding: 'gbk' | 'utf-8' | 'shift-jis' }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async sendBatchCommands(_options: { commands: string[]; delayBetween?: number }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }

  async isConnected(): Promise<{ connected: boolean }> {
    return Promise.resolve({ connected: false });
  }

  async setProtocol(_options: { protocol: 'POS' | 'CPCL' | 'TSPL' | 'ZPL' }): Promise<import('./models').HandshakeResponse> {
    return Promise.reject({ code: -1, msg: 'Not supported on web', data: null });
  }
}
