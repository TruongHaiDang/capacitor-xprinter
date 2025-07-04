import { Injectable } from '@angular/core';
import { CapacitorXprinter } from 'capacitor-xprinter';

@Injectable({
  providedIn: 'root'
})
export class XprinterService {

  constructor() {}

  /**
   * Kết nối máy in tổng quát cho mọi loại kết nối/ngôn ngữ
   * @param options Tham số cấu hình kết nối
   */
  async connectPrinter(options: {
    type: string;
    language: string;
    macAddress?: string;
    name?: string;
    ip?: string;
    port?: number;
    serialPort?: string;
    baudRate?: number;
  }): Promise<{ code: number; msg: string; data: any }> {
    // Chuyển type string sang code số
    const typeCode = this.getConnectTypeCode(options.type);
    const connectOptions: any = {
      type: typeCode,
      language: options.language,
    };
    if (options.macAddress) connectOptions.macAddress = options.macAddress;
    if (options.name) connectOptions.name = options.name;
    if (options.ip) connectOptions.ip = options.ip;
    if (options.port) connectOptions.port = options.port;
    if (options.serialPort) connectOptions.serialPort = options.serialPort;
    if (options.baudRate) connectOptions.baudRate = options.baudRate;
    return CapacitorXprinter.connect(connectOptions);
  }

  private getConnectTypeCode(type: string): number {
    switch (type) {
      case 'USB': return 1;
      case 'BLUETOOTH': return 2;
      case 'ETHERNET': return 3;
      case 'SERIAL': return 4;
      default: return 0;
    }
  }

  async listUsbPorts(): Promise<string[]> {
    const res = await CapacitorXprinter.listAvailablePorts({ type: 'USB' });
    return res.ports || [];
  }

  async listBluetoothDevices(): Promise<string[]> {
    const res = await CapacitorXprinter.listAvailablePorts({ type: 'BLUETOOTH' });
    return res.ports || [];
  }

  async listSerialPorts(): Promise<string[]> {
    const res = await CapacitorXprinter.listAvailablePorts({ type: 'SERIAL' });
    return res.ports || [];
  }

  async disconnectPrinter(): Promise<{ code: number; msg: string; data: any }> {
    return CapacitorXprinter.disconnect();
  }

  async printText(text: string, alignment: 'left' | 'center' | 'right' = 'left', textSize = 0, attribute = 0) {
    return CapacitorXprinter.printText({ text, alignment, textSize, attribute });
  }

  async printQRCode(data: string, moduleSize = 6, ecLevel = 3, alignment: 'left' | 'center' | 'right' = 'left') {
    return CapacitorXprinter.printQRCode({ data, moduleSize, ecLevel, alignment });
  }

  async printBarcode(data: string, codeType: number, width = 2, height = 80, alignment: 'left' | 'center' | 'right' = 'left', textPosition = 2) {
    return CapacitorXprinter.printBarcode({ data, codeType, width, height, alignment, textPosition });
  }

  async printImageFromPath(path: string, width = 384, alignment: 'left' | 'center' | 'right' = 'left') {
    return CapacitorXprinter.printImageFromPath({ imagePath: path, width, alignment });
  }

  async printImageBase64(base64: string, width = 384, alignment: 'left' | 'center' | 'right' = 'left') {
    return CapacitorXprinter.printImageBase64({ base64, width, alignment });
  }
}
