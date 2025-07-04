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

    /**
     * In mã QR theo ngôn ngữ của máy in hiện tại.
     */
    public void printQRCode(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", null, null);
            return;
        }
        String data = options.getString("data");
        if (data == null) {
            call.reject("Thiếu dữ liệu QR", null, null);
            return;
        }
        int module = options.has("moduleSize") ? options.getInteger("moduleSize") : 4;
        int ecLevel = options.has("ecLevel") ? options.getInteger("ecLevel") : 0;
        String alignStr = options.getString("alignment", "left");
        int alignment = 0;
        switch (alignStr.toLowerCase()) {
            case "center": alignment = 1; break;
            case "right": alignment = 2; break;
            default: alignment = 0; break;
        }
        try {
            currentPrinter.printQRCode(data, module, ecLevel, alignment);
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

    /**
     * In mã vạch 1D.
     */
    public void printBarcode(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", null, null);
            return;
        }
        String data = options.getString("data");
        if (data == null) {
            call.reject("Thiếu dữ liệu", null, null);
            return;
        }
        int codeType = options.getInteger("codeType");
        int width = options.has("width") ? options.getInteger("width") : 2;
        int height = options.has("height") ? options.getInteger("height") : 80;
        int textPosition = options.has("textPosition") ? options.getInteger("textPosition") : 0;
        String alignStr = options.getString("alignment", "left");
        int alignment = 0;
        switch (alignStr.toLowerCase()) {
            case "center": alignment = 1; break;
            case "right": alignment = 2; break;
            default: alignment = 0; break;
        }
        try {
            currentPrinter.printBarcode(data, codeType, width, height, alignment, textPosition);
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

    /**
     * In hình ảnh từ đường dẫn.
     */
    public void printImageFromPath(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", null, null);
            return;
        }
        String path = options.getString("imagePath");
        if (path == null) {
            call.reject("Thiếu đường dẫn", null, null);
            return;
        }
        int width = options.has("width") ? options.getInteger("width") : 200;
        String alignStr = options.getString("alignment", "left");
        int alignment = 0;
        switch (alignStr.toLowerCase()) {
            case "center": alignment = 1; break;
            case "right": alignment = 2; break;
            default: alignment = 0; break;
        }
        try {
            currentPrinter.printImage(path, width, alignment);
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

    /**
     * In hình ảnh base64.
     */
    public void printImageBase64(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("Chưa kết nối máy in", null, null);
            return;
        }
        String base64 = options.getString("base64");
        if (base64 == null) {
            call.reject("Thiếu dữ liệu ảnh", null, null);
            return;
        }
        int width = options.has("width") ? options.getInteger("width") : 200;
        String alignStr = options.getString("alignment", "left");
        int alignment = 0;
        switch (alignStr.toLowerCase()) {
            case "center": alignment = 1; break;
            case "right": alignment = 2; break;
            default: alignment = 0; break;
        }
        try {
            currentPrinter.printImageBase64(base64, width, alignment);
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

}
