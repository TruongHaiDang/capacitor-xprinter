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

    /**
     * Lấy danh sách cổng/thiết bị khả dụng tuỳ theo type.
     * Hỗ trợ USB, BLUETOOTH, SERIAL. Nếu type không hợp lệ trả về danh sách rỗng.
     *
     * @param type    Chuỗi 'USB' | 'BLUETOOTH' | 'SERIAL'
     * @param context Context ứng dụng để truy cập hệ thống (cần cho USB)
     * @return        Danh sách tên cổng/thiết bị
     */
    public List<String> listAvailablePorts(String type, Context context) {
        ensureInit(context);
        if (type == null) return Collections.emptyList();

        switch (type.toUpperCase()) {
            case "USB":
                return POSConnect.getUsbDevices(context);
            case "SERIAL":
                return POSConnect.getSerialPort();
            case "BLUETOOTH":
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) return Collections.emptyList();
                Set<BluetoothDevice> bonded = adapter.getBondedDevices();
                List<String> btDevices = new java.util.ArrayList<>();
                for (BluetoothDevice dev : bonded) {
                    btDevices.add(dev.getName());
                }
                return btDevices;
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Expose các hằng số trạng thái của POSConnect cho phía JS/TS.
     * @return JSObject chứa mapping { CONST_NAME: value }
     */
    public com.getcapacitor.JSObject getStatusConstants() {
        com.getcapacitor.JSObject constants = new com.getcapacitor.JSObject();
        constants.put("CONNECT_SUCCESS", POSConnect.CONNECT_SUCCESS);
        constants.put("CONNECT_FAIL", POSConnect.CONNECT_FAIL);
        constants.put("SEND_FAIL", POSConnect.SEND_FAIL);
        constants.put("CONNECT_INTERRUPT", POSConnect.CONNECT_INTERRUPT);
        constants.put("USB_ATTACHED", POSConnect.USB_ATTACHED);
        constants.put("USB_DETACHED", POSConnect.USB_DETACHED);
        constants.put("BLUETOOTH_INTERRUPT", POSConnect.BLUETOOTH_INTERRUPT);
        return constants;
    }

    /**
     * Lấy danh sách thiết bị USB chi tiết (UsbDevice).
     * @param context Context ứng dụng Android
     * @return Danh sách UsbDevice
     */
    public List<android.hardware.usb.UsbDevice> listUsbDevices(Context context) {
        ensureInit(context);
        return POSConnect.getUsbDevice(context);
    }

    /**
     * Lấy danh sách cổng Serial (COM).
     * @return Danh sách tên cổng Serial
     */
    public List<String> listSerialPorts(Context context) {
        ensureInit(context);
        return POSConnect.getSerialPort();
    }

    /**
     * Kết nối tới máy in qua địa chỉ MAC (LAN/Ethernet).
     * @param mac Địa chỉ MAC của thiết bị
     * @param context Context ứng dụng Android
     * @param call Đối tượng PluginCall để trả về kết quả
     */
    public void connectByMac(String mac, Context context, PluginCall call) {
        ensureInit(context);
        IDeviceConnection device = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET);
        IConnectListener listener = new IConnectListener() {
            @Override
            public void onStatus(int status, String info, String msg) {
                JSObject result = new JSObject();
                result.put("code", status);
                result.put("msg", msg);
                result.put("data", info);
                if (status == POSConnect.CONNECT_SUCCESS) {
                    currentDevice = device;
                    // Mặc định dùng POS nếu không chỉ định
                    currentPrinter = PrinterFactory.createPrinter("POS", currentDevice);
                    call.resolve(result);
                } else {
                    call.reject(msg, (Exception)null, result);
                }
            }
        };
        // POSConnect.connectMac đã tự gọi createDevice ở trong, nhưng ta vẫn gọi để lấy reference device ở trên.
        POSConnect.connectMac(mac, listener);
    }

    public void printText(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        if (!(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("Chức năng in text chỉ hỗ trợ POSPrinter", (Exception) null, null);
            return;
        }
        String text = options.getString("text");
        if (text == null) {
            call.reject("Thiếu text", (Exception) null, null);
            return;
        }
        String alignStr = options.getString("alignment", "left");
        int alignment = 0;
        switch (alignStr.toLowerCase()) {
            case "center": alignment = 1; break;
            case "right": alignment = 2; break;
            default: alignment = 0; break;
        }
        int textSize = options.has("textSize") ? options.getInteger("textSize") : 0;
        int attribute = options.has("attribute") ? options.getInteger("attribute") : 0;
        try {
            ((PosPrinterWrapper) currentPrinter).printText(text, alignment, textSize, attribute);
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In thành công");
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
                ((TsplPrinterWrapper) currentPrinter).printQRCode(data, x, y, ecLevel, cellWidth, mode, rotation, model);
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
                ((TsplPrinterWrapper) currentPrinter).printBarcode(data, x, y, codeType, height, readable, rotation, narrow, wide);
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
                String mode = options.getString("mode", "OVERWRITE");
                ((TsplPrinterWrapper) currentPrinter).printImageFromPath(imagePath, x, y, mode, context);

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
                String mode = options.getString("mode", "OVERWRITE");
                ((TsplPrinterWrapper) currentPrinter).printImageBase64(base64, x, y, mode);
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

    public void printEncodedText(JSObject options, PluginCall call) {
        // Chức năng in text với encoding - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng in text với encoding chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void sendBatchCommands(JSObject options, PluginCall call) {
        // Chức năng gửi nhiều lệnh liên tiếp - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng gửi batch commands chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    public boolean isConnected() {
        return currentDevice != null && currentPrinter != null;
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

    public void configText(JSObject options, PluginCall call) {
        // Chức năng cấu hình text - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng config text chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void configBarcode(JSObject options, PluginCall call) {
        // Chức năng cấu hình barcode - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng config barcode chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void configQRCode(JSObject options, PluginCall call) {
        // Chức năng cấu hình QR code - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng config QR code chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void configImage(JSObject options, PluginCall call) {
        // Chức năng cấu hình image - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng config image chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void configLabel(JSObject options, PluginCall call) {
        // Chức năng cấu hình label - cần implement chi tiết
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Chức năng config label chưa được implement");
        ret.put("data", null);
        call.resolve(ret);
    }

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

