export type DeviceType = 'USB' | 'BLUETOOTH' | 'ETHERNET' | 'SERIAL';

export type PrinterProtocol = 'POS' | 'CPCL' | 'TSPL' | 'ZPL';

export interface ConnectOptions {
  /** Loại thiết bị cần kết nối */
  type: DeviceType;

  /** Loại giao thức máy in */
  protocal: PrinterProtocol;

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
