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

  async printQRCode(options: any) {
    return CapacitorXprinter.printQRCode(options);
  }

  async printBarcode(options: any) {
    return CapacitorXprinter.printBarcode(options);
  }

  async printImageFromPath(options: any) {
    return CapacitorXprinter.printImageFromPath(options);
  }

  async printImageBase64(options: any) {
    return CapacitorXprinter.printImageBase64(options);
  }

  async cutPaper() {
    return CapacitorXprinter.cutPaper();
  }

  async openCashDrawer(options?: { pinNum?: number; onTime?: number; offTime?: number }) {
    return CapacitorXprinter.openCashDrawer(options || {});
  }

  async getPrinterStatus() {
    return CapacitorXprinter.getPrinterStatus();
  }

  async readData() {
    return CapacitorXprinter.readData();
  }

  async sendRawData(options: { hex: string }) {
    return CapacitorXprinter.sendRawData(options);
  }

  async printLabel(options: { command: string }) {
    return CapacitorXprinter.printLabel(options);
  }

  async resetPrinter() {
    return CapacitorXprinter.resetPrinter();
  }

  async selfTest() {
    return CapacitorXprinter.selfTest();
  }

  async printEncodedText(options: { text: string; encoding: 'gbk' | 'utf-8' | 'shift-jis' }) {
    return CapacitorXprinter.printEncodedText(options);
  }

  async sendBatchCommands(options: { commands: string[]; delayBetween?: number }) {
    return CapacitorXprinter.sendBatchCommands(options);
  }

  async isConnected(): Promise<{ connected: boolean }> {
    return CapacitorXprinter.isConnected();
  }

  async setProtocol(protocol: 'POS' | 'CPCL' | 'TSPL' | 'ZPL') {
    return CapacitorXprinter.setProtocol({ protocol });
  }

  async configText(options: Record<string, any>) {
    return CapacitorXprinter.configText(options);
  }

  async configBarcode(options: Record<string, any>) {
    return CapacitorXprinter.configBarcode(options);
  }

  async configQRCode(options: Record<string, any>) {
    return CapacitorXprinter.configQRCode(options);
  }

  async configImage(options: Record<string, any>) {
    return CapacitorXprinter.configImage(options);
  }

  async configLabel(options: Record<string, any>) {
    return CapacitorXprinter.configLabel(options);
  }
}
