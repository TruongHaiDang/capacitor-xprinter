# capacitor-xprinter

A Capacitor plugin for integrating Xprinter SDKs on Android and iOS.

## Install

```bash
npm install capacitor-xprinter
npx cap sync
```

## API

<docgen-index>

* [`connect(...)`](#connect)
* [`disconnect()`](#disconnect)
* [`isConnected()`](#isconnected)
* [`listAvailablePorts(...)`](#listavailableports)
* [`printText(...)`](#printtext)
* [`printEncodedText(...)`](#printencodedtext)
* [`printQRCode(...)`](#printqrcode)
* [`printBarcode(...)`](#printbarcode)
* [`printImageFromPath(...)`](#printimagefrompath)
* [`printImageBase64(...)`](#printimagebase64)
* [`printLabel(...)`](#printlabel)
* [`cutPaper()`](#cutpaper)
* [`openCashDrawer(...)`](#opencashdrawer)
* [`resetPrinter()`](#resetprinter)
* [`selfTest()`](#selftest)
* [`setProtocol(...)`](#setprotocol)
* [`getPrinterStatus()`](#getprinterstatus)
* [`readData()`](#readdata)
* [`sendRawData(...)`](#sendrawdata)
* [`sendBatchCommands(...)`](#sendbatchcommands)
* [`configText(...)`](#configtext)
* [`configBarcode(...)`](#configbarcode)
* [`configQRCode(...)`](#configqrcode)
* [`configImage(...)`](#configimage)
* [`configLabel(...)`](#configlabel)
* [`echo(...)`](#echo)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### connect(...)

```typescript
connect(options: ConnectOptions) => any
```

Kết nối đến máy in

| Param         | Type                                                      | Description                                                  |
| ------------- | --------------------------------------------------------- | ------------------------------------------------------------ |
| **`options`** | <code><a href="#connectoptions">ConnectOptions</a></code> | Chọn loại kết nối và các thông số tương ứng với loại kết kết |

**Returns:** <code>any</code>

--------------------


### disconnect()

```typescript
disconnect() => any
```

Ngắt kết nối với máy in hiện tại

**Returns:** <code>any</code>

--------------------


### isConnected()

```typescript
isConnected() => any
```

Kiểm tra trạng thái kết nối máy in (đã kết nối hay chưa)

**Returns:** <code>any</code>

--------------------


### listAvailablePorts(...)

```typescript
listAvailablePorts(options: { type: 'USB' | 'BLUETOOTH' | 'SERIAL'; }) => any
```

Danh sách cổng khả dụng (USB/Bluetooth/Serial)

| Param         | Type                                                     |
| ------------- | -------------------------------------------------------- |
| **`options`** | <code>{ type: 'USB' \| 'BLUETOOTH' \| 'SERIAL'; }</code> |

**Returns:** <code>any</code>

--------------------


### printText(...)

```typescript
printText(options: { text: string; alignment?: 'left' | 'center' | 'right'; textSize?: number; attribute?: number; }) => any
```

In văn bản đơn giản (chỉ hỗ trợ POSPrinter)

| Param         | Type                                                                                                             |
| ------------- | ---------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ text: string; alignment?: 'left' \| 'center' \| 'right'; textSize?: number; attribute?: number; }</code> |

**Returns:** <code>any</code>

--------------------


### printEncodedText(...)

```typescript
printEncodedText(options: { text: string; encoding: 'gbk' | 'utf-8' | 'shift-jis'; }) => any
```

In văn bản với encoding cụ thể (GBK, UTF-8, Shift-JIS,...)

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code>{ text: string; encoding: 'gbk' \| 'utf-8' \| 'shift-jis'; }</code> |

**Returns:** <code>any</code>

--------------------


### printQRCode(...)

```typescript
printQRCode(options: { data: string; moduleSize?: number; ecLevel?: number; alignment?: 'left' | 'center' | 'right'; }) => any
```

In mã QR

| Param         | Type                                                                                                             |
| ------------- | ---------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ data: string; moduleSize?: number; ecLevel?: number; alignment?: 'left' \| 'center' \| 'right'; }</code> |

**Returns:** <code>any</code>

--------------------


### printBarcode(...)

```typescript
printBarcode(options: { data: string; codeType: number; width?: number; height?: number; alignment?: 'left' | 'center' | 'right'; textPosition?: number; }) => any
```

In mã vạch 1D

| Param         | Type                                                                                                                                                |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ data: string; codeType: number; width?: number; height?: number; alignment?: 'left' \| 'center' \| 'right'; textPosition?: number; }</code> |

**Returns:** <code>any</code>

--------------------


### printImageFromPath(...)

```typescript
printImageFromPath(options: { imagePath: string; width?: number; alignment?: 'left' | 'center' | 'right'; }) => any
```

In hình ảnh từ đường dẫn

| Param         | Type                                                                                           |
| ------------- | ---------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ imagePath: string; width?: number; alignment?: 'left' \| 'center' \| 'right'; }</code> |

**Returns:** <code>any</code>

--------------------


### printImageBase64(...)

```typescript
printImageBase64(options: { base64: string; width?: number; alignment?: 'left' | 'center' | 'right'; }) => any
```

In hình ảnh base64 – phù hợp khi không có file path

| Param         | Type                                                                                        |
| ------------- | ------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ base64: string; width?: number; alignment?: 'left' \| 'center' \| 'right'; }</code> |

**Returns:** <code>any</code>

--------------------


### printLabel(...)

```typescript
printLabel(options: { command: string; }) => any
```

In nội dung dạng label cho CPCL / TSPL / ZPL

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ command: string; }</code> |

**Returns:** <code>any</code>

--------------------


### cutPaper()

```typescript
cutPaper() => any
```

Cắt giấy (POSPrinter)

**Returns:** <code>any</code>

--------------------


### openCashDrawer(...)

```typescript
openCashDrawer(options?: { pinNum?: number | undefined; onTime?: number | undefined; offTime?: number | undefined; } | undefined) => any
```

Mở két tiền (POSPrinter)

| Param         | Type                                                                 |
| ------------- | -------------------------------------------------------------------- |
| **`options`** | <code>{ pinNum?: number; onTime?: number; offTime?: number; }</code> |

**Returns:** <code>any</code>

--------------------


### resetPrinter()

```typescript
resetPrinter() => any
```

Thiết lập lại máy in

**Returns:** <code>any</code>

--------------------


### selfTest()

```typescript
selfTest() => any
```

Thực hiện in tự test của máy in (self-test)

**Returns:** <code>any</code>

--------------------


### setProtocol(...)

```typescript
setProtocol(options: { protocol: 'POS' | 'CPCL' | 'TSPL' | 'ZPL'; }) => any
```

Thiết lập lại protocol (POS / CPCL / TSPL / ZPL) mà không cần reconnect lại

| Param         | Type                                                           |
| ------------- | -------------------------------------------------------------- |
| **`options`** | <code>{ protocol: 'POS' \| 'CPCL' \| 'TSPL' \| 'ZPL'; }</code> |

**Returns:** <code>any</code>

--------------------


### getPrinterStatus()

```typescript
getPrinterStatus() => any
```

Kiểm tra trạng thái máy in

**Returns:** <code>any</code>

--------------------


### readData()

```typescript
readData() => any
```

Đọc dữ liệu phản hồi từ máy in (nếu có)

**Returns:** <code>any</code>

--------------------


### sendRawData(...)

```typescript
sendRawData(options: { hex: string; }) => any
```

Gửi dữ liệu tùy ý (raw byte) – nâng cao

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ hex: string; }</code> |

**Returns:** <code>any</code>

--------------------


### sendBatchCommands(...)

```typescript
sendBatchCommands(options: { commands: string[]; delayBetween?: number; }) => any
```

Gửi nhiều lệnh liên tiếp (batch command mode)

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code>{ commands: {}; delayBetween?: number; }</code> |

**Returns:** <code>any</code>

--------------------


### configText(...)

```typescript
configText(options: any) => any
```

Cấu hình cho in text

| Param         | Type             |
| ------------- | ---------------- |
| **`options`** | <code>any</code> |

**Returns:** <code>any</code>

--------------------


### configBarcode(...)

```typescript
configBarcode(options: any) => any
```

Cấu hình cho in barcode

| Param         | Type             |
| ------------- | ---------------- |
| **`options`** | <code>any</code> |

**Returns:** <code>any</code>

--------------------


### configQRCode(...)

```typescript
configQRCode(options: any) => any
```

Cấu hình cho in QRCode

| Param         | Type             |
| ------------- | ---------------- |
| **`options`** | <code>any</code> |

**Returns:** <code>any</code>

--------------------


### configImage(...)

```typescript
configImage(options: any) => any
```

Cấu hình cho in hình ảnh

| Param         | Type             |
| ------------- | ---------------- |
| **`options`** | <code>any</code> |

**Returns:** <code>any</code>

--------------------


### configLabel(...)

```typescript
configLabel(options: any) => any
```

Cấu hình cho in label (CPCL/TSPL/ZPL)

| Param         | Type             |
| ------------- | ---------------- |
| **`options`** | <code>any</code> |

**Returns:** <code>any</code>

--------------------


### echo(...)

```typescript
echo(options: { value: string; }) => any
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### ConnectOptions

| Prop             | Type                                                        | Description                                                  |
| ---------------- | ----------------------------------------------------------- | ------------------------------------------------------------ |
| **`type`**       | <code><a href="#connecttype">ConnectType</a></code>         | Loại thiết bị cần kết nối                                    |
| **`language`**   | <code><a href="#printerlanguage">PrinterLanguage</a></code> | Loại ngôn ngữ máy in                                         |
| **`name`**       | <code>string</code>                                         | Tên thiết bị Bluetooth hoặc USB (ví dụ: 'XP-58')             |
| **`macAddress`** | <code>string</code>                                         | Địa chỉ MAC cho thiết bị Bluetooth                           |
| **`ip`**         | <code>string</code>                                         | Địa chỉ IP cho kết nối Ethernet                              |
| **`port`**       | <code>number</code>                                         | Cổng kết nối TCP/IP hoặc serial                              |
| **`baudRate`**   | <code>number</code>                                         | Tốc độ baudrate cho kết nối serial                           |
| **`serialPort`** | <code>string</code>                                         | Tên cổng serial (COMx trên Windows, /dev/ttyUSBx trên Linux) |


#### HandshakeResponse

| Prop       | Type                |
| ---------- | ------------------- |
| **`code`** | <code>number</code> |
| **`msg`**  | <code>string</code> |
| **`data`** | <code>any</code>    |


### Type Aliases


#### PrinterLanguage

<code>'POS' | 'CPCL' | 'TSPL' | 'ZPL'</code>


### Enums


#### ConnectType

| Members         | Value          |
| --------------- | -------------- |
| **`USB`**       | <code>1</code> |
| **`BLUETOOTH`** | <code>2</code> |
| **`ETHERNET`**  | <code>3</code> |
| **`SERIAL`**    | <code>4</code> |

</docgen-api>
