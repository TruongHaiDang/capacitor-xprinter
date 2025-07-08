package com.haidang.xprinter;

import android.util.Log;
import net.posprinter.POSConnect;
import net.posprinter.IDeviceConnection;
import com.haidang.xprinter.HandshakeResponse;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.Set;
import java.util.Collections;
import java.util.List;
import android.content.Context;
import net.posprinter.IConnectListener;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.haidang.xprinter.PrinterBase;
import com.haidang.xprinter.PrinterFactory;

public class CapacitorXprinter {
    private IDeviceConnection currentDevice = null;
    /**
     * Đối tượng Printer hiện tại (POS/CPCL/TSPL/ZPL) được khởi tạo sau khi kết nối thành công.
     */
    private PrinterBase currentPrinter = null;
    
    // ===== HANDSHAKE =====
    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    /**
     * Đảm bảo POSConnect đã được khởi tạo trước khi sử dụng.
     * @param context Context ứng dụng để khởi tạo
     */
    private void ensureInit(Context context) {
        if (POSConnect.getAppCtx() == null && context != null) {
            POSConnect.init(context);
        }
    }

    /**
     * Kết nối tới thiết bị máy in dựa trên loại thiết bị và các tham số truyền vào.
     * Sử dụng IConnectListener để nhận trạng thái kết nối bất đồng bộ.
     *
     * @param options  Đối tượng JSObject chứa các tham số cấu hình kết nối (type, name, macAddress, ip, port, serialPort, baudRate, ...)
     * @param context  Context của ứng dụng Android, dùng để khởi tạo hoặc truy cập các tài nguyên hệ thống
     * @param call     Đối tượng PluginCall để trả về kết quả cho phía JS/TS
     */
    public void connect(JSObject options, Context context, PluginCall call) {
        ensureInit(context);

        // Lấy loại thiết bị từ options (1: USB, 2: Bluetooth, 3: Ethernet, 4: Serial)
        int deviceType = options.getInteger("type");
        IDeviceConnection device = POSConnect.createDevice(deviceType);

        // Tạo listener để nhận trạng thái kết nối bất đồng bộ
        IConnectListener listener = new IConnectListener() {
            @Override
            public void onStatus(int status, String info, String msg) {
                com.getcapacitor.JSObject result = new com.getcapacitor.JSObject();
                result.put("code", status);
                result.put("msg", msg);
                result.put("data", info);
                if (status == POSConnect.CONNECT_SUCCESS) {
                    currentDevice = device;
                    // Khởi tạo printer tương ứng với language
                    String language = options.getString("language");
                    if (language == null) language = "POS";
                    try {
                        currentPrinter = PrinterFactory.createPrinter(language, device);
                    } catch (IllegalArgumentException ex) {
                        // Không hỗ trợ ngôn ngữ -> reject và đóng kết nối
                        call.reject(ex.getMessage(), (Exception)null, result);
                        return;
                    }
                    call.resolve(result);
                } else {
                    call.reject(msg, (Exception)null, result);
                }
            }
        };

        try {
            // Xử lý kết nối dựa trên loại thiết bị
            switch (deviceType) {
                case POSConnect.DEVICE_TYPE_USB:
                    // Kết nối qua USB với tên thiết bị
                    String usbName = options.getString("name");
                    device.connect(usbName, listener);
                    break;
                case POSConnect.DEVICE_TYPE_BLUETOOTH:
                    // Kết nối qua Bluetooth với địa chỉ MAC
                    String mac = options.getString("macAddress");
                    device.connect(mac, listener);
                    break;
                case POSConnect.DEVICE_TYPE_ETHERNET:
                    // Kết nối qua Ethernet chỉ truyền IP, port mặc định 9100
                    String ip = options.getString("ip");
                    device.connect(ip, listener);
                    break;
                case POSConnect.DEVICE_TYPE_SERIAL:
                    // Kết nối qua Serial với tên cổng và baudrate
                    String serialPort = options.getString("serialPort");
                    int baudRate = options.getInteger("baudRate");
                    device.connect(serialPort + ":" + baudRate, listener);
                    break;
                default:
                    // Loại thiết bị không hỗ trợ
                    com.getcapacitor.JSObject result = new com.getcapacitor.JSObject();
                    result.put("code", 400);
                    result.put("msg", "Loại thiết bị không hỗ trợ");
                    result.put("data", null);
                    call.reject("Loại thiết bị không hỗ trợ", (Exception)null, result);
            }
        } catch (Exception e) {
            // Xử lý lỗi khi kết nối thất bại (lỗi ngoại lệ)
            com.getcapacitor.JSObject result = new com.getcapacitor.JSObject();
            result.put("code", 500);
            result.put("msg", "Kết nối thất bại: " + e.getMessage());
            result.put("data", null);
            call.reject("Kết nối thất bại: " + e.getMessage(), (Exception)null, result);
        }
    }

    /**
     * Ngắt kết nối với thiết bị máy in hiện tại nếu đang kết nối.
     * Sử dụng phương thức close() của IDeviceConnection để đóng kết nối.
     *
     * @return Đối tượng HandshakeResponse phản hồi trạng thái ngắt kết nối (thành công/thất bại và thông điệp tương ứng)
     */
    public HandshakeResponse disconnect() {
        // Kiểm tra xem có thiết bị nào đang kết nối không
        if (currentDevice != null) {
            try {
                // Đóng kết nối với thiết bị
                currentDevice.close();
                currentDevice = null;
                currentPrinter = null;
                // Dọn dẹp tài nguyên
                POSConnect.exit();
                // Ngắt kết nối thành công
                return new HandshakeResponse(200, "Ngắt kết nối thành công", null);
            } catch (Exception e) {
                // Xử lý lỗi khi ngắt kết nối thất bại
                return new HandshakeResponse(500, "Ngắt kết nối thất bại: " + e.getMessage(), null);
            }
        } else {
            // Không có thiết bị nào đang kết nối
            return new HandshakeResponse(400, "Không có thiết bị nào đang kết nối", null);
        }
    }

    public HandshakeResponse isConnected() {
        boolean connected = currentDevice != null && currentPrinter != null;
        if (connected) {
            return new HandshakeResponse(200, "Đã kết nối", true);
        } else {
            return new HandshakeResponse(400, "Chưa kết nối", false);
        }
    }

    // ===== PRINT =====
    /**
     * In văn bản ra máy in theo loại máy in hiện tại.
     *
     * @param options Đối tượng JSObject chứa các tham số in text. Cấu trúc:
     *   - POSPrinter:
     *       {
     *         text: string,                // Nội dung cần in (bắt buộc)
     *         alignment?: 'left'|'center'|'right', // Căn lề (mặc định: 'left')
     *         textSize?: number,           // Kích thước chữ (tùy máy, mặc định: 0)
     *         attribute?: number           // Thuộc tính in (in đậm, nghiêng, v.v, mặc định: 0)
     *       }
     *   - CPCL:
     *       {
     *         text: string,                // Nội dung cần in (bắt buộc)
     *         x?: number,                  // Tọa độ X (mặc định: 0)
     *         y?: number,                  // Tọa độ Y (mặc định: 0)
     *         font?: number,               // Loại font (mặc định: 0)
     *         rotation?: number            // Góc xoay (mặc định: 0)
     *       }
     *   - TSPL:
     *       {
     *         text: string,                // Nội dung cần in (bắt buộc)
     *         x?: number,                  // Tọa độ X (mặc định: 0)
     *         y?: number,                  // Tọa độ Y (mặc định: 0)
     *         font?: string,               // Loại font (mặc định: '0')
     *         rotation?: number,           // Góc xoay (mặc định: 0)
     *         xScale?: number,             // Tỉ lệ X (mặc định: 1)
     *         yScale?: number              // Tỉ lệ Y (mặc định: 1)
     *       }
     *   - ZPL:
     *       {
     *         text: string,                // Nội dung cần in (bắt buộc)
     *         x?: number,                  // Tọa độ X (mặc định: null)
     *         y?: number,                  // Tọa độ Y (mặc định: null)
     *         font?: string,               // Loại font (mặc định: null)
     *         orientation?: string,        // Hướng in (mặc định: 'N')
     *         height?: number,             // Chiều cao font (mặc định: null)
     *         width?: number               // Chiều rộng font (mặc định: null)
     *       }
     *
     * @param call Đối tượng PluginCall để trả về kết quả cho phía JS/TS
     */
    public void printText(JSObject options, PluginCall call) {
        // Kiểm tra đã kết nối máy in chưa
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        // Lấy nội dung text cần in
        String text = options.getString("text");
        if (text == null) {
            call.reject("Thiếu text", (Exception) null, null);
            return;
        }
        try {
            // Xử lý cho POSPrinter (máy in hóa đơn)
            if (currentPrinter instanceof PosPrinterWrapper) {
                // Lấy căn lề, kích thước, thuộc tính từ options hoặc configPosText
                String alignStr = options.has("alignment") ? options.getString("alignment") : posTextAlignment;
                int alignment = 0; // 0: left, 1: center, 2: right
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = 1; break;
                    case "right": alignment = 2; break;
                    default: alignment = 0; break;
                }
                int textSize = options.has("textSize") ? options.getInteger("textSize") : posTextSize;
                int attribute = options.has("attribute") ? options.getInteger("attribute") : posTextAttribute;
                ((PosPrinterWrapper) currentPrinter).printText(text, alignment, textSize, attribute);
            // Xử lý cho CPCL Printer (máy in nhãn CPCL)
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                // Nếu không có tham số vị trí/font/rotation thì dùng hàm tự động (in ở vị trí mặc định)
                if (!options.has("x") && !options.has("font") && !options.has("rotation")) {
                    ((CpclPrinterWrapper) currentPrinter).printText(text, null, null, null);
                } else {
                    // Lấy các tham số vị trí, font, rotation nếu có
                    int x = options.has("x") ? options.getInteger("x") : 0;
                    int y = options.has("y") ? options.getInteger("y") : 0;
                    int font = options.has("font") ? options.getInteger("font") : 0;
                    int rotation = options.has("rotation") ? options.getInteger("rotation") : 0;
                    ((CpclPrinterWrapper) currentPrinter).printText(text, x, y, font, rotation);
                }
            // Xử lý cho TSPL Printer (máy in nhãn TSPL)
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                // Nếu không có tham số vị trí/font/rotation thì dùng hàm tự động
                if (!options.has("x") && !options.has("font") && !options.has("rotation")) {
                    ((TsplPrinterWrapper) currentPrinter).printText(text, null, null, null);
                } else {
                    // Lấy các tham số vị trí, font, rotation, tỉ lệ X/Y nếu có
                    int x = options.has("x") ? options.getInteger("x") : 0;
                    int y = options.has("y") ? options.getInteger("y") : 0;
                    String font = options.getString("font", "0"); // font TSPL là string
                    int rotation = options.has("rotation") ? options.getInteger("rotation") : 0;
                    int xScale = options.has("xScale") ? options.getInteger("xScale") : 1;
                    int yScale = options.has("yScale") ? options.getInteger("yScale") : 1;
                    ((TsplPrinterWrapper) currentPrinter).drawText(text, x, y, font, rotation, xScale, yScale);
                }
            // Xử lý cho ZPL Printer (máy in nhãn ZPL)
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                // Nếu không có x/font thì dùng hàm tự động (in ở vị trí mặc định)
                if (!options.has("x") && !options.has("font")) {
                    ((ZplPrinterWrapper) currentPrinter).printText(text, null, null, null, null, null, null);
                } else {
                    // Lấy các tham số vị trí, font, orientation, height, width nếu có
                    Integer x = options.has("x") ? options.getInteger("x") : null;
                    Integer y = options.has("y") ? options.getInteger("y") : null;
                    String font = options.has("font") ? options.getString("font") : null;
                    String orientation = options.getString("orientation", "N"); // N: Normal
                    Integer height = options.has("height") ? options.getInteger("height") : null;
                    Integer width = options.has("width") ? options.getInteger("width") : null;
                    ((ZplPrinterWrapper) currentPrinter).printText(text, x, y, font, orientation, height, width);
                }
            } else {
                // Nếu loại máy in không hỗ trợ in text
                call.reject("Loại máy in không hỗ trợ in text", (Exception) null, null);
                return;
            }
            // Trả về kết quả thành công cho phía JS/TS
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In text thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            // Xử lý lỗi khi in
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void printEncodedText(JSObject options, PluginCall call) {
        // Chức năng in text với encoding - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng in text với encoding chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void printQRCode(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        
        String data = options.getString("data");
        if (data == null) {
            call.reject("Thiếu dữ liệu QR code", (Exception) null, null);
            return;
        }
        
        try {
            if (currentPrinter instanceof PosPrinterWrapper) {
                int moduleSize = options.has("moduleSize") ? options.getInteger("moduleSize") : 4;
                int ecLevel = options.has("ecLevel") ? options.getInteger("ecLevel") : 0;
                String alignStr = options.getString("alignment", "left");
                int alignment = 0;
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = 1; break;
                    case "right": alignment = 2; break;
                    default: alignment = 0; break;
                }
                ((PosPrinterWrapper) currentPrinter).printQRCode(data, moduleSize, ecLevel, alignment);
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int model = options.has("model") ? options.getInteger("model") : 2;
                int unitWidth = options.has("unitWidth") ? options.getInteger("unitWidth") : 6;
                ((CpclPrinterWrapper) currentPrinter).printQRCode(data, x, y, model, unitWidth);
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                String ecLevel = options.getString("ecLevel", "M");
                int cellWidth = options.has("cellWidth") ? options.getInteger("cellWidth") : 4;
                String mode = options.getString("mode", "A");
                int rotation = options.has("rotation") ? options.getInteger("rotation") : 0;
                String model = options.getString("model", "M2");
                ((TsplPrinterWrapper) currentPrinter).drawQRCode(data, x, y, ecLevel, cellWidth, mode, rotation);
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int model = options.has("model") ? options.getInteger("model") : 2;
                int magnification = options.has("magnification") ? options.getInteger("magnification") : 3;
                String errorCorrection = options.getString("errorCorrection", "M");
                int maskValue = options.has("maskValue") ? options.getInteger("maskValue") : 0;
                ((ZplPrinterWrapper) currentPrinter).printQRCode(data, x, y, model, magnification, errorCorrection, maskValue);
            }
            
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In QR code thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void printBarcode(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        
        String data = options.getString("data");
        if (data == null) {
            call.reject("Thiếu dữ liệu barcode", (Exception) null, null);
            return;
        }
        
        try {
            if (currentPrinter instanceof PosPrinterWrapper) {
                int codeType = options.has("codeType") ? options.getInteger("codeType") : 73; // CODE128
                int width = options.has("width") ? options.getInteger("width") : 2;
                int height = options.has("height") ? options.getInteger("height") : 162;
                String alignStr = options.getString("alignment", "left");
                int alignment = 0;
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = 1; break;
                    case "right": alignment = 2; break;
                    default: alignment = 0; break;
                }
                int textPosition = options.has("textPosition") ? options.getInteger("textPosition") : 2;
                ((PosPrinterWrapper) currentPrinter).printBarcode(data, codeType, width, height, alignment, textPosition);
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                String codeType = options.getString("codeType", "128");
                int height = options.has("height") ? options.getInteger("height") : 32;
                int readable = options.has("readable") ? options.getInteger("readable") : 1;
                ((CpclPrinterWrapper) currentPrinter).printBarcode(data, x, y, codeType, height, readable);
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                String codeType = options.getString("codeType", "128");
                int height = options.has("height") ? options.getInteger("height") : 40;
                int readable = options.has("readable") ? options.getInteger("readable") : 1;
                int rotation = options.has("rotation") ? options.getInteger("rotation") : 0;
                int narrow = options.has("narrow") ? options.getInteger("narrow") : 2;
                int wide = options.has("wide") ? options.getInteger("wide") : 2;
                ((TsplPrinterWrapper) currentPrinter).drawBarcode(data, x, y, codeType, height, readable, rotation, narrow, wide);
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                String codeType = options.getString("codeType", "^BC");
                String orientation = options.getString("orientation", "N");
                int height = options.has("height") ? options.getInteger("height") : 100;
                String printInterpretationLine = options.getString("printInterpretationLine", "Y");
                String printInterpretationLineAbove = options.getString("printInterpretationLineAbove", "N");
                String checkDigit = options.getString("checkDigit", "N");
                ((ZplPrinterWrapper) currentPrinter).printBarcode(data, x, y, codeType, orientation, height, printInterpretationLine, printInterpretationLineAbove, checkDigit);
            }
            
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In barcode thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void printImageFromPath(JSObject options, Context context, PluginCall call) {
        // Kiểm tra kết nối máy in
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }

        // Lấy đường dẫn ảnh từ JSObject
        String imagePath = options.getString("imagePath");
        if (imagePath == null) {
            call.reject("Thiếu đường dẫn hình ảnh", (Exception) null, null);
            return;
        }

        // Loại bỏ tiền tố file:// nếu có (để tương thích với BitmapFactory.decodeFile)
        if (imagePath.startsWith("file://")) {
            imagePath = imagePath.replaceFirst("file://", "");
        }

        try {
            // Phân nhánh theo loại máy in
            if (currentPrinter instanceof PosPrinterWrapper) {
                int width = options.has("width") ? options.getInteger("width") : 0;
                String alignStr = options.getString("alignment", "left");
                int alignment;
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = 1; break;
                    case "right": alignment = 2; break;
                    default: alignment = 0; break;
                }
                ((PosPrinterWrapper) currentPrinter).printImageFromPath(imagePath, width, alignment, context);

            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int mode = options.has("mode") ? options.getInteger("mode") : 0;
                ((CpclPrinterWrapper) currentPrinter).printImageFromPath(imagePath, x, y, mode, context);

            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int mode = 0;
                if (options.has("mode")) {
                    Object m = options.get("mode");
                    if (m instanceof Number) {
                        mode = ((Number) m).intValue();
                    } else if (m instanceof String) {
                        String ms = (String) m;
                        if (ms.equalsIgnoreCase("OR")) mode = 1;
                        else if (ms.equalsIgnoreCase("XOR")) mode = 2;
                        else mode = 0;
                    }
                }
                ((TsplPrinterWrapper) currentPrinter).drawImageFromPath(imagePath, x, y, mode, context);

            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int width = options.has("width") ? options.getInteger("width") : 200;
                int height = options.has("height") ? options.getInteger("height") : 200;
                ((ZplPrinterWrapper) currentPrinter).printImageFromPath(imagePath, x, y, width, height, context);
            }

            // Phản hồi thành công
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In hình ảnh thành công");
            ret.put("data", null);
            call.resolve(ret);

        } catch (Exception ex) {
            // Trả lỗi về JS
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void printImageBase64(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        
        String base64 = options.getString("base64");
        if (base64 == null) {
            call.reject("Thiếu dữ liệu base64", (Exception) null, null);
            return;
        }
        
        try {
            if (currentPrinter instanceof PosPrinterWrapper) {
                int width = options.has("width") ? options.getInteger("width") : 0;
                String alignStr = options.getString("alignment", "left");
                int alignment = 0;
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = 1; break;
                    case "right": alignment = 2; break;
                    default: alignment = 0; break;
                }
                ((PosPrinterWrapper) currentPrinter).printImageBase64(base64, width, alignment);
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int mode = options.has("mode") ? options.getInteger("mode") : 0;
                ((CpclPrinterWrapper) currentPrinter).printImageBase64(base64, x, y, mode);
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int mode = 0;
                if (options.has("mode")) {
                    Object m = options.get("mode");
                    if (m instanceof Number) {
                        mode = ((Number) m).intValue();
                    } else if (m instanceof String) {
                        String ms = (String) m;
                        if (ms.equalsIgnoreCase("OR")) mode = 1;
                        else if (ms.equalsIgnoreCase("XOR")) mode = 2;
                        else mode = 0;
                    }
                }
                ((TsplPrinterWrapper) currentPrinter).drawImageBase64(base64, x, y, mode);
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int width = options.has("width") ? options.getInteger("width") : 200;
                int height = options.has("height") ? options.getInteger("height") : 200;
                ((ZplPrinterWrapper) currentPrinter).printImageBase64(base64, x, y, width, height);
            }
            
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In hình ảnh base64 thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void printLabel(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        
        String command = options.getString("command");
        if (command == null) {
            call.reject("Thiếu lệnh command", (Exception) null, null);
            return;
        }
        
        try {
            if (currentPrinter instanceof CpclPrinterWrapper) {
                ((CpclPrinterWrapper) currentPrinter).sendCommand(command);
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                ((TsplPrinterWrapper) currentPrinter).sendCommand(command);
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                ((ZplPrinterWrapper) currentPrinter).sendCommand(command);
            } else {
                call.reject("Chức năng in label không hỗ trợ cho POSPrinter", (Exception) null, null);
                return;
            }
            
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In label thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    // ===== PRINTER CONTROL =====
    public void cutPaper(PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        if (!(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("Chức năng cắt giấy chỉ hỗ trợ POSPrinter", (Exception) null, null);
            return;
        }
        
        try {
            ((PosPrinterWrapper) currentPrinter).cutPaper();
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Cắt giấy thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void openCashDrawer(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        if (!(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("Chức năng mở két tiền chỉ hỗ trợ POSPrinter", (Exception) null, null);
            return;
        }
        
        try {
            int pinNum = options.has("pinNum") ? options.getInteger("pinNum") : 0;
            int onTime = options.has("onTime") ? options.getInteger("onTime") : 100;
            int offTime = options.has("offTime") ? options.getInteger("offTime") : 100;
            ((PosPrinterWrapper) currentPrinter).openCashDrawer(pinNum, onTime, offTime);
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Mở két tiền thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void resetPrinter(PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        if (!(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("Chức năng reset chỉ hỗ trợ POSPrinter", (Exception) null, null);
            return;
        }
        
        try {
            ((PosPrinterWrapper) currentPrinter).resetPrinter();
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Reset máy in thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void selfTest(PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        if (!(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("Chức năng self test chỉ hỗ trợ POSPrinter", (Exception) null, null);
            return;
        }
        
        try {
            // Không hỗ trợ selfTest cho POSPrinter
            JSObject ret = new JSObject();
            ret.put("code", 501);
            ret.put("msg", "Chức năng self test chưa được hỗ trợ cho POSPrinter");
            ret.put("data", null);
            call.reject("Chức năng self test chưa được hỗ trợ cho POSPrinter", (Exception) null, ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void setProtocol(JSObject options, PluginCall call) {
        if (currentDevice == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        
        String protocol = options.getString("protocol");
        if (protocol == null) {
            call.reject("Thiếu thông tin protocol", (Exception) null, null);
            return;
        }
        
        try {
            currentPrinter = PrinterFactory.createPrinter(protocol, currentDevice);
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Thiết lập protocol thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    // ===== STATUS & DATA =====
    public void getPrinterStatus(PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        
        try {
            currentPrinter.printerStatus(new net.posprinter.posprinterface.IStatusCallback() {
                @Override
                public void receive(int status) {
                    JSObject ret = new JSObject();
                    ret.put("code", 200);
                    ret.put("msg", "Lấy trạng thái thành công");
                    ret.put("data", status);
                    call.resolve(ret);
                }
            });
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void readData(PluginCall call) {
        // Chức năng đọc dữ liệu từ máy in - cần implement tùy theo yêu cầu cụ thể
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng đọc dữ liệu chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void sendRawData(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        
        String hex = options.getString("hex");
        if (hex == null) {
            call.reject("Thiếu dữ liệu hex", (Exception) null, null);
            return;
        }
        
        try {
            // Chuyển đổi hex string thành byte array
            byte[] data = hexStringToByteArray(hex);
            currentPrinter.sendData(data);
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Gửi dữ liệu thô thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void sendBatchCommands(JSObject options, PluginCall call) {
        // Chức năng gửi nhiều lệnh liên tiếp - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng gửi batch commands chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    // ===== CONFIG =====
    /**
     * Cấu hình các tham số in text cho POSPrinter.
     *
     * @param options Đối tượng JSObject chứa các tham số cấu hình. Cấu trúc:
     *   {
     *     alignment?: 'left'|'center'|'right', // Căn lề mặc định cho in text
     *     attribute?: number,                  // Thuộc tính in (in đậm, gạch chân, đảo ngược, ...)
     *     textSize?: number,                   // Kích thước chữ mặc định
     *     font?: number,                       // Loại font (standard/compress)
     *     lineSpacing?: number,                // Giãn dòng
     *     codePage?: number,                   // Bảng mã ký tự
     *     charRightSpace?: number,             // Khoảng cách ký tự
     *     upsideDown?: boolean                 // In ngược
     *   }
     *
     * Khi gọi hàm này, các giá trị sẽ được lưu lại và sử dụng làm mặc định cho các lần in text tiếp theo (nếu không truyền options tương ứng khi in).
     *
     * @param call Đối tượng PluginCall để trả về kết quả cho phía JS/TS
     */
    // Biến instance lưu config POS
    private String posTextAlignment = "left";
    private int posTextAttribute = 0;
    private int posTextSize = 0;
    private Integer posTextFont = null;
    private Integer posTextLineSpacing = null;
    private Integer posTextCodePage = null;
    private Integer posTextCharRightSpace = null;
    private Boolean posTextUpsideDown = null;

    public void configPosText(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        if (!(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("Chức năng configPosText chỉ hỗ trợ POSPrinter", (Exception) null, null);
            return;
        }
        try {
            PosPrinterWrapper pos = (PosPrinterWrapper) currentPrinter;
            // Alignment
            if (options.has("alignment")) {
                posTextAlignment = options.getString("alignment", "left");
                int alignment = 0;
                switch (posTextAlignment.toLowerCase()) {
                    case "center": alignment = net.posprinter.POSConst.ALIGNMENT_CENTER; break;
                    case "right": alignment = net.posprinter.POSConst.ALIGNMENT_RIGHT; break;
                    default: alignment = net.posprinter.POSConst.ALIGNMENT_LEFT; break;
                }
                pos.setAlignment(alignment);
            }
            // Attribute (bold, underline, reverse...)
            if (options.has("attribute")) {
                posTextAttribute = options.getInteger("attribute");
            }
            if (options.has("textSize")) {
                posTextSize = options.getInteger("textSize");
            }
            if (options.has("attribute") || options.has("textSize")) {
                pos.setTextStyle(posTextAttribute, posTextSize);
            }
            // Font (standard/compress)
            if (options.has("font")) {
                posTextFont = options.getInteger("font");
                pos.selectCharacterFont(posTextFont);
            }
            // Line spacing
            if (options.has("lineSpacing")) {
                posTextLineSpacing = options.getInteger("lineSpacing");
                pos.setLineSpacing(posTextLineSpacing);
            }
            // Code page
            if (options.has("codePage")) {
                posTextCodePage = options.getInteger("codePage");
                pos.selectCodePage(posTextCodePage);
            }
            // Char right space
            if (options.has("charRightSpace")) {
                posTextCharRightSpace = options.getInteger("charRightSpace");
                pos.setCharRightSpace(posTextCharRightSpace.byteValue());
            }
            // Upside down
            if (options.has("upsideDown")) {
                posTextUpsideDown = options.getBool("upsideDown");
                pos.setTurnUpsideDownMode(posTextUpsideDown);
            }
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Cấu hình text thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void configCpclText(JSObject options, PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("code", 501);
        ret.put("msg", "Chức năng configCpclText chưa được hỗ trợ");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void configTsplText(JSObject options, PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("code", 501);
        ret.put("msg", "Chức năng configTsplText chưa được hỗ trợ");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void configZplText(JSObject options, PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("code", 501);
        ret.put("msg", "Chức năng configZplText chưa được hỗ trợ");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void configBarcode(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        try {
            if (currentPrinter instanceof CpclPrinterWrapper) {
                // CPCL không có config barcode riêng, có thể lưu height/readable nếu cần
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                // TSPL không có config barcode riêng, có thể lưu height/readable nếu cần
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                // ZPL không có config barcode riêng, có thể lưu height/width nếu cần
            } else {
                call.reject("Chức năng configBarcode chỉ hỗ trợ CPCL, TSPL, ZPL", (Exception) null, null);
                return;
            }
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Cấu hình barcode thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void configQRCode(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        try {
            if (currentPrinter instanceof CpclPrinterWrapper) {
                // CPCL không có config QRCode riêng, có thể lưu model/unitWidth nếu cần
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                // TSPL không có config QRCode riêng, có thể lưu cellWidth/model nếu cần
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                // ZPL không có config QRCode riêng, có thể lưu model/magnification nếu cần
            } else {
                call.reject("Chức năng configQRCode chỉ hỗ trợ CPCL, TSPL, ZPL", (Exception) null, null);
                return;
            }
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Cấu hình QRCode thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void configImage(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        try {
            if (currentPrinter instanceof CpclPrinterWrapper) {
                // CPCL không có config image riêng, có thể lưu mode nếu cần
                // int mode = options.has("mode") ? options.getInteger("mode") : 0;
                // Lưu lại nếu muốn dùng cho printImage
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                // TSPL không có config image riêng, có thể lưu mode nếu cần
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                // ZPL không có config image riêng, có thể lưu width/height nếu cần
            } else {
                call.reject("Chức năng configImage chỉ hỗ trợ CPCL, TSPL, ZPL", (Exception) null, null);
                return;
            }
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Cấu hình image thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    public void configLabel(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        try {
            if (currentPrinter instanceof CpclPrinterWrapper) {
                int width = options.has("width") ? options.getInteger("width") : 576;
                int height = options.has("height") ? options.getInteger("height") : 320;
                int quantity = options.has("quantity") ? options.getInteger("quantity") : 1;
                ((CpclPrinterWrapper) currentPrinter).setLabel(width, height, quantity);
            } else if (currentPrinter instanceof TsplPrinterWrapper) {
                double width = options.has("width") ? options.getDouble("width") : 60.0;
                double height = options.has("height") ? options.getDouble("height") : 40.0;
                double gap = options.has("gap") ? options.getDouble("gap") : 2.0;
                double offset = options.has("offset") ? options.getDouble("offset") : 0.0;
                int direction = options.has("direction") ? options.getInteger("direction") : 0;
                TsplPrinterWrapper tspl = (TsplPrinterWrapper) currentPrinter;
                tspl.setSize(width, height);
                tspl.setGap(gap, offset);
                tspl.setDirection(direction);
                tspl.clearBuffer();
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                int length = options.has("length") ? options.getInteger("length") : 800;
                int speed = options.has("speed") ? options.getInteger("speed") : 4;
                ZplPrinterWrapper zpl = (ZplPrinterWrapper) currentPrinter;
                zpl.setLabelLength(length);
                zpl.setPrintSpeed(speed);
                zpl.startFormat();
            } else {
                call.reject("Chức năng configLabel chỉ hỗ trợ CPCL, TSPL, ZPL", (Exception) null, null);
                return;
            }
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Cấu hình label thành công");
            ret.put("data", null);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("code", 500);
            ret.put("msg", ex.getMessage());
            ret.put("data", null);
            call.reject(ex.getMessage(), ex, ret);
        }
    }

    // ===== UTILS =====
    /**
     * Chuyển đổi hex string thành byte array
     */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

