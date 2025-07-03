export enum ConnectType {
  USB = 1,
  BLUETOOTH = 2,
  ETHERNET = 3,
  SERIAL = 4,
}

export type PrinterLanguage = 'POS' | 'CPCL' | 'TSPL' | 'ZPL';

export interface ConnectOptions {
  /** Loại thiết bị cần kết nối */
  type: ConnectType;

  /** Loại ngôn ngữ máy in */
  language: PrinterLanguage;

  /** Tên thiết bị Bluetooth hoặc USB (ví dụ: 'XP-58') */
  name?: string;

  /** Địa chỉ MAC cho thiết bị Bluetooth */
  macAddress?: string;

  /** Địa chỉ IP cho kết nối Ethernet */
  ip?: string;

  /** Cổng kết nối TCP/IP hoặc serial */
  port?: number;

  /** Tốc độ baudrate cho kết nối serial */
  baudRate?: number;

  /** Tên cổng serial (COMx trên Windows, /dev/ttyUSBx trên Linux) */
  serialPort?: string;
}

export interface HandshakeResponse {
  code: number;
  msg: string;
  data: any;
}
