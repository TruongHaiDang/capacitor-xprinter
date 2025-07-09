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

  async printText(text: string) {
    return CapacitorXprinter.printText({ text });
  }

  async printQRCode(options: any) {
    if (options && options.protocol === 'POS') {
      return CapacitorXprinter.printQRCode({ data: options.data });
    }
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

  /**
   * Cấu hình label cho TSPL với giá trị mặc định từ selftest nếu không truyền vào
   */
  async configLabel(options: Record<string, any> = {}) {
    // Gán mặc định nếu chưa có
    if (options['width'] === undefined) options['width'] = 72;
    if (options['height'] === undefined) options['height'] = 30;
    if (options['speed'] === undefined) options['speed'] = 3;
    if (options['density'] === undefined) options['density'] = 7;
    return CapacitorXprinter.configLabel(options);
  }

  /**
   * Gửi lệnh gốc cho máy in (ZPL/CPCL/TSPL...)
   */
  async sendCommand(command: string): Promise<any> {
    return CapacitorXprinter.printLabel({ command });
  }

  /**
   * Vẽ text lên buffer TSPL (chưa in)
   */
  async drawTextTSPL(options: { text: string; x: number; y: number; font: string; rotation: number; xScale: number; yScale: number }) {
    return CapacitorXprinter.printText({ ...options });
  }

  /**
   * Vẽ barcode lên buffer TSPL (chưa in)
   */
  async drawBarcodeTSPL(options: { data: string; x: number; y: number; codeType: string; height: number; readable: number; rotation: number; narrow: number; wide: number }) {
    // Gọi plugin riêng cho TSPL, không dùng chung với POS/CPCL/ZPL
    return (window as any).Capacitor.Plugins.CapacitorXprinter.drawBarcodeTSPL(options);
  }

  /**
   * Vẽ QRCode lên buffer TSPL (chưa in)
   */
  async drawQRCodeTSPL(options: { data: string; x: number; y: number; ecLevel: string; cellWidth: number; mode: string; rotation: number; model?: string }) {
    // Gọi plugin riêng cho TSPL, không dùng chung với POS/CPCL/ZPL
    return (window as any).Capacitor.Plugins.CapacitorXprinter.drawQRCodeTSPL(options);
  }

  /**
   * Vẽ hình ảnh từ path lên buffer TSPL (chưa in)
   */
  async drawImageFromPathTSPL(options: { imagePath: string; x: number; y: number; mode: number }) {
    return CapacitorXprinter.printImageFromPath({ ...options });
  }

  /**
   * Vẽ hình ảnh từ base64 lên buffer TSPL (chưa in)
   */
  async drawImageBase64TSPL(options: { base64: string; x: number; y: number; mode: number }) {
    return CapacitorXprinter.printImageBase64({ ...options });
  }

  /**
   * In label TSPL sau khi đã vẽ các thành phần
   */
  async printLabelTSPL(options: { sets?: number; copies?: number } = {}) {
    // Gọi plugin riêng cho TSPL, không dùng chung với POS/CPCL/ZPL
    return (window as any).Capacitor.Plugins.CapacitorXprinter.printLabelTSPL(options);
  }

  /**
   * Gửi lệnh POS (ESC/POS) dạng text hoặc hex
   */
  async sendPosCommand(command: string): Promise<any> {
    return (window as any).Capacitor.Plugins.CapacitorXprinter.sendPosCommand({ command });
  }
}
