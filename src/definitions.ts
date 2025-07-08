import { ConnectOptions, HandshakeResponse } from './models';

export interface CapacitorXprinterPlugin {
  // ===== HANDSHAKE =====
  /**
   * Kết nối đến máy in
   * @param options Chọn loại kết nối và các thông số tương ứng với loại kết kết
   */
  connect(options: ConnectOptions): Promise<HandshakeResponse>;

  /**
   * Ngắt kết nối với máy in hiện tại
   */
  disconnect(): Promise<HandshakeResponse>;

  /**
   * Danh sách cổng khả dụng (USB/Bluetooth/Serial)
   */
  listAvailablePorts(options: { type: 'USB' | 'BLUETOOTH' | 'SERIAL' }): Promise<{ ports: string[] }>;

  // ===== PRINT =====
  /**
   * In văn bản đơn giản (chỉ hỗ trợ POSPrinter)
   */
  printText(options: {
    text: string;
    alignment?: 'left' | 'center' | 'right';
    textSize?: number;
    attribute?: number;
  }): Promise<HandshakeResponse>;

  /**
   * In văn bản với encoding cụ thể (GBK, UTF-8, Shift-JIS,...)
   */
  printEncodedText(options: { text: string; encoding: 'gbk' | 'utf-8' | 'shift-jis' }): Promise<HandshakeResponse>;

  /**
   * In mã QR
   */
  printQRCode(options: {
    data: string;
    moduleSize?: number;
    ecLevel?: number;
    alignment?: 'left' | 'center' | 'right';
  }): Promise<HandshakeResponse>;

  /**
   * In mã vạch 1D
   */
  printBarcode(options: {
    data: string;
    codeType: number;
    width?: number;
    height?: number;
    alignment?: 'left' | 'center' | 'right';
    textPosition?: number;
  }): Promise<HandshakeResponse>;

  /**
   * In hình ảnh từ đường dẫn
   */
  printImageFromPath(options: {
    imagePath: string;
    width?: number;
    alignment?: 'left' | 'center' | 'right';
  }): Promise<HandshakeResponse>;

  /**
   * In hình ảnh base64 – phù hợp khi không có file path
   */
  printImageBase64(options: {
    base64: string;
    width?: number;
    alignment?: 'left' | 'center' | 'right';
  }): Promise<HandshakeResponse>;

  /**
   * In nội dung dạng label cho CPCL / TSPL / ZPL
   * @param command Chuỗi command tương ứng với ngôn ngữ in
   */
  printLabel(options: {
    command: string; // CPCL/TSPL/ZPL command
  }): Promise<HandshakeResponse>;

  // ===== PRINTER CONTROL =====
  /**
   * Cắt giấy (POSPrinter)
   */
  cutPaper(): Promise<HandshakeResponse>;

  /**
   * Mở két tiền (POSPrinter)
   */
  openCashDrawer(options?: { pinNum?: number; onTime?: number; offTime?: number }): Promise<HandshakeResponse>;

  /**
   * Thiết lập lại máy in
   */
  resetPrinter(): Promise<HandshakeResponse>;

  /**
   * Thực hiện in tự test của máy in (self-test)
   */
  selfTest(): Promise<HandshakeResponse>;

  /**
   * Thiết lập lại protocol (POS / CPCL / TSPL / ZPL) mà không cần reconnect lại
   */
  setProtocol(options: { protocol: 'POS' | 'CPCL' | 'TSPL' | 'ZPL' }): Promise<HandshakeResponse>;

  // ===== STATUS & DATA =====
  /**
   * Kiểm tra trạng thái máy in
   */
  getPrinterStatus(): Promise<HandshakeResponse>;

  /**
   * Đọc dữ liệu phản hồi từ máy in (nếu có)
   */
  readData(): Promise<HandshakeResponse>;

  /**
   * Gửi dữ liệu tùy ý (raw byte) – nâng cao
   */
  sendRawData(options: { hex: string }): Promise<HandshakeResponse>;

  /**
   * Gửi nhiều lệnh liên tiếp (batch command mode)
   */
  sendBatchCommands(options: {
    commands: string[]; // dạng text hoặc hex, tuỳ theo ngữ cảnh
    delayBetween?: number; // delay giữa các lệnh (ms)
  }): Promise<HandshakeResponse>;

  // ===== CONFIG =====
  /**
   * Cấu hình cho in text
   */
  configText(options: Record<string, any>): Promise<any>;

  /**
   * Cấu hình cho in barcode
   */
  configBarcode(options: Record<string, any>): Promise<any>;

  /**
   * Cấu hình cho in QRCode
   */
  configQRCode(options: Record<string, any>): Promise<any>;

  /**
   * Cấu hình cho in hình ảnh
   */
  configImage(options: Record<string, any>): Promise<any>;

  /**
   * Cấu hình cho in label (CPCL/TSPL/ZPL)
   */
  configLabel(options: Record<string, any>): Promise<any>;

  // ===== UTILS =====
  echo(options: { value: string }): Promise<{ value: string }>;
}
