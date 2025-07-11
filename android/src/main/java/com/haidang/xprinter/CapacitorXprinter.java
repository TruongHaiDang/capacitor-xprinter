package com.haidang.xprinter;

import android.util.Log;
import net.posprinter.POSConst;
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

    // ===== CONFIG =====
    // Biến instance lưu config POS
    private String posTextAlignment = "left";
    private int posTextAttribute = 0;
    private int posTextSize = 0;
    // Thêm các biến cấu hình POS nhận từ JS
    private Integer posTextFont = null;
    private Integer posTextLineSpacing = null;
    private Integer posTextCodePage = null;
    private Integer posTextCharRightSpace = null;
    private Boolean posTextUpsideDown = null;
    // Barcode config POS
    private Integer posBarcodeType = null;
    private Integer posBarcodeWidth = null;
    private Integer posBarcodeHeight = null;
    private String posBarcodeAlignment = null;
    private Integer posBarcodeTextPosition = null;
    // QRCode config POS
    private String posQrData = null;
    private String posQrAlignment = null;
    private Integer posQrModuleSize = null;
    private Integer posQrEcLevel = null;
    // Image config POS
    private String posImageBitmap = null;
    private String posImageAlignment = null;
    private Integer posImageWidth = null;
    private Integer posImageMode = null;
    private Integer posImageDensity = null;

    public void configPosText(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("[configPosText] Chưa kết nối POSPrinter");
            return;
        }

        try {
            if (options.has("alignment")) {
                posTextAlignment = options.getString("alignment", "left");
            }
            if (options.has("attribute")) {
                posTextAttribute = options.getInteger("attribute");
            }
            if (options.has("textSize")) {
                posTextSize = options.getInteger("textSize");
            }
            // Nhận các giá trị mới từ JS
            if (options.has("font")) {
                Object fontObj = options.get("font");
                if (fontObj instanceof Number) {
                    posTextFont = ((Number) fontObj).intValue();
                } else if (fontObj instanceof String) {
                    try {
                        posTextFont = Integer.parseInt((String) fontObj);
                    } catch (NumberFormatException e) {
                        posTextFont = null;
                    }
                }
            }
            if (options.has("lineSpacing")) {
                posTextLineSpacing = options.getInteger("lineSpacing");
            }
            if (options.has("codePage")) {
                Object codePageObj = options.get("codePage");
                if (codePageObj instanceof Number) {
                    posTextCodePage = ((Number) codePageObj).intValue();
                } else if (codePageObj instanceof String) {
                    try {
                        posTextCodePage = Integer.parseInt((String) codePageObj);
                    } catch (NumberFormatException e) {
                        posTextCodePage = null;
                    }
                }
            }
            if (options.has("charRightSpace")) {
                posTextCharRightSpace = options.getInteger("charRightSpace");
            }
            if (options.has("upsideDown")) {
                posTextUpsideDown = options.getBool("upsideDown");
            }

            PosPrinterWrapper pos = (PosPrinterWrapper) currentPrinter;
            int alignment = POSConst.ALIGNMENT_LEFT;
            switch (posTextAlignment.toLowerCase()) {
                case "center": alignment = POSConst.ALIGNMENT_CENTER;
                case "right": alignment = POSConst.ALIGNMENT_RIGHT;
                default: alignment = POSConst.ALIGNMENT_LEFT;
            }

            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Đã cấu hình text");
            call.resolve(ret);

        } catch (Exception e) {
            call.reject("Lỗi cấu hình: " + e.getMessage());
        }
    }

    public void configCpclText(JSObject options, PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("code", 501);
        ret.put("msg", "Chức năng configCpclText chưa được hỗ trợ");
        ret.put("data", null);
        call.resolve(ret);
    }

    // TSPL config - text
    private Integer tsplTextX = null;
    private Integer tsplTextY = null;
    private String tsplTextFont = null;
    private Integer tsplTextRotation = null;
    private Integer tsplTextXScale = null;
    private Integer tsplTextYScale = null;
    private String tsplTextContent = null;
    // TSPL config - barcode
    private Integer tsplBarcodeX = null;
    private Integer tsplBarcodeY = null;
    private String tsplBarcodeType = null;
    private Integer tsplBarcodeHeight = null;
    private Integer tsplBarcodeReadable = null;
    private Integer tsplBarcodeRotation = null;
    private Integer tsplBarcodeNarrow = null;
    private Integer tsplBarcodeWide = null;
    private String tsplBarcodeContent = null;
    // TSPL config - qrcode
    private Integer tsplQrX = null;
    private Integer tsplQrY = null;
    private String tsplQrEcLevel = null;
    private Integer tsplQrCellWidth = null;
    private String tsplQrMode = null;
    private Integer tsplQrRotation = null;
    private String tsplQrModel = null;
    private String tsplQrMask = null;
    private String tsplQrContent = null;
    // TSPL config - image
    private Integer tsplImageX = null;
    private Integer tsplImageY = null;
    private Integer tsplImageMode = null;
    private Integer tsplImageWidth = null;
    private String tsplImageBitmap = null;
    private String tsplImageAlgorithm = null;
    // BỔ SUNG ĐẦY ĐỦ:
    private Integer tsplImageDensity = null;
    private Double tsplImageSpeed = null;
    private Integer tsplImageDirection = null;
    private Boolean tsplImageMirror = null;
    private Integer tsplImageReferenceX = null, tsplImageReferenceY = null;
    private Double tsplImageSizeWidthMm = null, tsplImageSizeHeightMm = null;
    private Double tsplImageGapMmM = null, tsplImageGapMmN = null;
    private Boolean tsplImageCls = null;
    private Integer tsplImageEraseX = null, tsplImageEraseY = null, tsplImageEraseWidth = null, tsplImageEraseHeight = null;
    private Boolean tsplImageBitmapCompression = null;

    public void configTsplText(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof TsplPrinterWrapper)) {
            call.reject("Chưa kết nối TSPLPrinter");
            return;
        }
        if (options.has("x")) tsplTextX = options.getInteger("x");
        if (options.has("y")) tsplTextY = options.getInteger("y");
        if (options.has("font")) tsplTextFont = options.getString("font");
        if (options.has("rotation")) tsplTextRotation = options.getInteger("rotation");
        if (options.has("xScale")) tsplTextXScale = options.getInteger("xScale");
        if (options.has("yScale")) tsplTextYScale = options.getInteger("yScale");
        if (options.has("content")) tsplTextContent = options.getString("content");
        // Bổ sung các trường nâng cao, bọc trong try-catch để tránh lỗi JSONException
        try {
            if (options.has("sizeWidthMm")) {
                Object wObj = options.get("sizeWidthMm");
                if (wObj instanceof Number) tsplImageSizeWidthMm = ((Number) wObj).doubleValue();
                else if (wObj instanceof String) {
                    try { tsplImageSizeWidthMm = Double.parseDouble((String) wObj); } catch (Exception e) { tsplImageSizeWidthMm = null; }
                }
            }
            if (options.has("sizeHeightMm")) {
                Object hObj = options.get("sizeHeightMm");
                if (hObj instanceof Number) tsplImageSizeHeightMm = ((Number) hObj).doubleValue();
                else if (hObj instanceof String) {
                    try { tsplImageSizeHeightMm = Double.parseDouble((String) hObj); } catch (Exception e) { tsplImageSizeHeightMm = null; }
                }
            }
            if (options.has("gapMmM")) {
                Object mObj = options.get("gapMmM");
                if (mObj instanceof Number) tsplImageGapMmM = ((Number) mObj).doubleValue();
                else if (mObj instanceof String) {
                    try { tsplImageGapMmM = Double.parseDouble((String) mObj); } catch (Exception e) { tsplImageGapMmM = null; }
                }
            }
            if (options.has("gapMmN")) {
                Object nObj = options.get("gapMmN");
                if (nObj instanceof Number) tsplImageGapMmN = ((Number) nObj).doubleValue();
                else if (nObj instanceof String) {
                    try { tsplImageGapMmN = Double.parseDouble((String) nObj); } catch (Exception e) { tsplImageGapMmN = null; }
                }
            }
            if (options.has("speed")) {
                Object speedObj = options.get("speed");
                if (speedObj instanceof Number) tsplImageSpeed = ((Number) speedObj).doubleValue();
                else if (speedObj instanceof String) {
                    try { tsplImageSpeed = Double.parseDouble((String) speedObj); } catch (Exception e) { tsplImageSpeed = null; }
                }
            }
            if (options.has("direction")) {
                Object dirObj = options.get("direction");
                if (dirObj instanceof Number) tsplImageDirection = ((Number) dirObj).intValue();
                else if (dirObj instanceof String) {
                    String dirStr = (String) dirObj;
                    if (dirStr.equalsIgnoreCase("FORWARD")) tsplImageDirection = 0;
                    else if (dirStr.equalsIgnoreCase("REVERSE")) tsplImageDirection = 1;
                }
            }
        } catch (Exception e) {
            // Nếu lỗi, các biến sẽ là null
        }
        if (options.has("referenceX")) tsplImageReferenceX = options.getInteger("referenceX");
        if (options.has("referenceY")) tsplImageReferenceY = options.getInteger("referenceY");
        if (options.has("density")) tsplImageDensity = options.getInteger("density");
        if (options.has("mirror")) tsplImageMirror = options.getBool("mirror");
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình text TSPL thành công");
        call.resolve(ret);
    }

    public void configTsplBarcode(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof TsplPrinterWrapper)) {
            call.reject("Chưa kết nối TSPLPrinter");
            return;
        }
        if (options.has("x")) tsplBarcodeX = options.getInteger("x");
        if (options.has("y")) tsplBarcodeY = options.getInteger("y");
        if (options.has("type")) tsplBarcodeType = options.getString("type");
        if (options.has("height")) tsplBarcodeHeight = options.getInteger("height");
        if (options.has("readable")) tsplBarcodeReadable = options.getInteger("readable");
        if (options.has("rotation")) tsplBarcodeRotation = options.getInteger("rotation");
        if (options.has("narrow")) tsplBarcodeNarrow = options.getInteger("narrow");
        if (options.has("wide")) tsplBarcodeWide = options.getInteger("wide");
        if (options.has("content")) tsplBarcodeContent = options.getString("content");
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình barcode TSPL thành công");
        call.resolve(ret);
    }

    public void configTsplQRCode(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof TsplPrinterWrapper)) {
            call.reject("Chưa kết nối TSPLPrinter");
            return;
        }
        if (options.has("x")) tsplQrX = options.getInteger("x");
        if (options.has("y")) tsplQrY = options.getInteger("y");
        if (options.has("ecLevel")) tsplQrEcLevel = options.getString("ecLevel");
        if (options.has("cellWidth")) tsplQrCellWidth = options.getInteger("cellWidth");
        if (options.has("mode")) tsplQrMode = options.getString("mode");
        if (options.has("rotation")) tsplQrRotation = options.getInteger("rotation");
        if (options.has("model")) tsplQrModel = options.getString("model");
        if (options.has("mask")) tsplQrMask = options.getString("mask");
        if (options.has("content")) tsplQrContent = options.getString("content");
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình QRCode TSPL thành công");
        call.resolve(ret);
    }

    public void configZplText(JSObject options, PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("code", 501);
        ret.put("msg", "Chức năng configZplText chưa được hỗ trợ");
        ret.put("data", null);
        call.resolve(ret);
    }

    public void configPosBarcode(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("[configPosBarcode] Chưa kết nối POSPrinter");
            return;
        }
        // Lưu lại config barcode POS nếu có
        if (options.has("codeType")) posBarcodeType = options.getInteger("codeType");
        if (options.has("width")) posBarcodeWidth = options.getInteger("width");
        if (options.has("height")) posBarcodeHeight = options.getInteger("height");
        if (options.has("alignment")) posBarcodeAlignment = options.getString("alignment");
        if (options.has("textPosition")) posBarcodeTextPosition = options.getInteger("textPosition");
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình barcode POS thành công");
        call.resolve(ret);
    }

    public void configCpclBarcode(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof CpclPrinterWrapper)) {
            call.reject("Chưa kết nối CPCLPrinter");
            return;
        }
        // Có thể lưu lại config barcode cho CPCL nếu cần
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình barcode CPCL thành công");
        call.resolve(ret);
    }

    public void configZplBarcode(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof ZplPrinterWrapper)) {
            call.reject("Chưa kết nối ZPLPrinter");
            return;
        }
        // Có thể lưu lại config barcode cho ZPL nếu cần
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình barcode ZPL thành công");
        call.resolve(ret);
    }

    public void configPosQRCode(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("[configPosQRCode] Chưa kết nối POSPrinter");
            return;
        }
        if (options.has("data")) posQrData = options.getString("data");
        if (options.has("alignment")) posQrAlignment = options.getString("alignment");
        if (options.has("moduleSize")) posQrModuleSize = options.getInteger("moduleSize");
        if (options.has("errorCorrectionLevel")) posQrEcLevel = options.getInteger("errorCorrectionLevel");
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình QRCode POS thành công");
        call.resolve(ret);
    }

    public void configCpclQRCode(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof CpclPrinterWrapper)) {
            call.reject("Chưa kết nối CPCLPrinter");
            return;
        }
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình QRCode CPCL thành công");
        call.resolve(ret);
    }

    public void configZplQRCode(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof ZplPrinterWrapper)) {
            call.reject("Chưa kết nối ZPLPrinter");
            return;
        }
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình QRCode ZPL thành công");
        call.resolve(ret);
    }

    public void configPosImage(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("[configPosImage] Chưa kết nối POSPrinter");
            return;
        }
        if (options.has("bitmap")) posImageBitmap = options.getString("bitmap");
        if (options.has("alignment")) posImageAlignment = options.getString("alignment");
        if (options.has("width")) posImageWidth = options.getInteger("width");
        if (options.has("mode")) posImageMode = options.getInteger("mode");
        if (options.has("density")) posImageDensity = options.getInteger("density");
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình image POS thành công");
        call.resolve(ret);
    }

    // Image config CPCL
    private String cpclImagePath = null;
    private Integer cpclImageX = null;
    private Integer cpclImageY = null;
    private Integer cpclImageMode = null;
    private String cpclImageBase64 = null;

    public void configCpclImage(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof CpclPrinterWrapper)) {
            call.reject("Chưa kết nối CPCLPrinter");
            return;
        }
        if (options.has("imagePath")) cpclImagePath = options.getString("imagePath");
        if (options.has("x")) cpclImageX = options.getInteger("x");
        if (options.has("y")) cpclImageY = options.getInteger("y");
        if (options.has("mode")) cpclImageMode = options.getInteger("mode");
        if (options.has("base64")) cpclImageBase64 = options.getString("base64");
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình image CPCL thành công");
        call.resolve(ret);
    }

    public void configZplImage(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof ZplPrinterWrapper)) {
            call.reject("Chưa kết nối ZPLPrinter");
            return;
        }
        JSObject ret = new JSObject();
        ret.put("code", 200);
        ret.put("msg", "Cấu hình image ZPL thành công");
        call.resolve(ret);
    }

    // ===== PRINT =====
    public void printText(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("[printText] Chưa kết nối máy in", (Exception) null, null);
            return;
        }
    
        String text = options.getString("text");
        if (text == null) {
            call.reject("Thiếu text", (Exception) null, null);
            return;
        }
    
        try {
            if (currentPrinter instanceof TsplPrinterWrapper) {
                TsplPrinterWrapper tspl = (TsplPrinterWrapper) currentPrinter;
                // Áp dụng các cấu hình nâng cao nếu có
                if (tsplImageSizeWidthMm != null && tsplImageSizeHeightMm != null) {
                    tspl.sizeMm(tsplImageSizeWidthMm, tsplImageSizeHeightMm);
                }
                if (tsplImageGapMmM != null && tsplImageGapMmN != null) {
                    tspl.gapMm(tsplImageGapMmM, tsplImageGapMmN);
                }
                if (tsplImageReferenceX != null && tsplImageReferenceY != null) {
                    tspl.reference(tsplImageReferenceX, tsplImageReferenceY);
                }
                if (tsplImageDensity != null) {
                    tspl.density(tsplImageDensity);
                }
                if (tsplImageSpeed != null) {
                    tspl.speed(tsplImageSpeed);
                }
                if (tsplImageDirection != null && tsplImageMirror != null) {
                    tspl.direction(tsplImageDirection, tsplImageMirror);
                } else if (tsplImageDirection != null) {
                    tspl.direction(tsplImageDirection);
                }
                int x = tsplTextX != null ? tsplTextX : 0;
                int y = tsplTextY != null ? tsplTextY : 0;
                String font = tsplTextFont != null ? tsplTextFont : "0";
                int rotation = tsplTextRotation != null ? tsplTextRotation : 0;
                int xScale = tsplTextXScale != null ? tsplTextXScale : 1;
                int yScale = tsplTextYScale != null ? tsplTextYScale : 1;
                String content = tsplTextContent != null ? tsplTextContent : text;
                tspl.cls();
                tspl.printText(content, x, y, font, rotation, xScale, yScale);
                // Reset các biến cấu hình nâng cao sau khi in
                tsplImageSizeWidthMm = null;
                tsplImageSizeHeightMm = null;
                tsplImageGapMmM = null;
                tsplImageGapMmN = null;
                tsplImageReferenceX = null;
                tsplImageReferenceY = null;
                tsplImageDensity = null;
                tsplImageSpeed = null;
                tsplImageDirection = null;
                tsplImageMirror = null;
            } else if (currentPrinter instanceof PosPrinterWrapper) {
                PosPrinterWrapper pos = (PosPrinterWrapper) currentPrinter;
                // Lấy config từ options nếu có, không thì lấy từ biến instance
                String alignmentStr = options.has("alignment") ? options.getString("alignment") : posTextAlignment;
                int alignment;
                switch (alignmentStr.toLowerCase()) {
                    case "center": alignment = POSConst.ALIGNMENT_CENTER; break;
                    case "right": alignment = POSConst.ALIGNMENT_RIGHT; break;
                    default: alignment = POSConst.ALIGNMENT_LEFT; break;
                }
    
                Integer attribute = options.has("attribute") ? options.getInteger("attribute") : posTextAttribute;
                Integer textSize = options.has("textSize") ? options.getInteger("textSize") : posTextSize;
                Integer font = null;
                if (options.has("font")) {
                    Object fontObj = options.get("font");
                    if (fontObj instanceof Number) font = ((Number) fontObj).intValue();
                    else if (fontObj instanceof String) {
                        try { font = Integer.parseInt((String) fontObj); } catch (Exception e) { font = null; }
                    }
                } else {
                    font = posTextFont;
                }
                Integer lineSpacing = options.has("lineSpacing") ? options.getInteger("lineSpacing") : posTextLineSpacing;
                Integer codePage = null;
                if (options.has("codePage")) {
                    Object codePageObj = options.get("codePage");
                    if (codePageObj instanceof Number) codePage = ((Number) codePageObj).intValue();
                    else if (codePageObj instanceof String) {
                        try { codePage = Integer.parseInt((String) codePageObj); } catch (Exception e) { codePage = null; }
                    }
                } else {
                    codePage = posTextCodePage;
                }
                Integer charRightSpace = options.has("charRightSpace") ? options.getInteger("charRightSpace") : posTextCharRightSpace;
                Boolean upsideDown = options.has("upsideDown") ? options.getBool("upsideDown") : posTextUpsideDown;

                // Áp dụng cấu hình bổ sung trước khi in
                if (font != null) {
                    pos.selectCharacterFont(font);
                }
                if (lineSpacing != null) {
                    pos.setLineSpacing(lineSpacing);
                }
                if (codePage != null) {
                    pos.selectCodePage(codePage);
                }
                if (charRightSpace != null) {
                    pos.setCharRightSpace(charRightSpace.byteValue());
                }
                if (upsideDown != null) {
                    pos.setTurnUpsideDownMode(upsideDown);
                }
                pos.printText(text, alignment, attribute, textSize);
    
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                if (!options.has("x") && !options.has("font") && !options.has("rotation")) {
                    ((CpclPrinterWrapper) currentPrinter).printText(text, null, null, null);
                } else {
                    int x = options.has("x") ? options.getInteger("x") : 0;
                    int y = options.has("y") ? options.getInteger("y") : 0;
                    int font = options.has("font") ? options.getInteger("font") : 0;
                    int rotation = options.has("rotation") ? options.getInteger("rotation") : 0;
                    ((CpclPrinterWrapper) currentPrinter).printText(text, x, y, font, rotation);
                }
    
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                if (!options.has("x") && !options.has("font")) {
                    ((ZplPrinterWrapper) currentPrinter).printText(text, null, null, null, null, null, null);
                } else {
                    Integer x = options.has("x") ? options.getInteger("x") : null;
                    Integer y = options.has("y") ? options.getInteger("y") : null;
                    String font = options.has("font") ? options.getString("font") : null;
                    String orientation = options.getString("orientation", "N");
                    Integer height = options.has("height") ? options.getInteger("height") : null;
                    Integer width = options.has("width") ? options.getInteger("width") : null;
                    ((ZplPrinterWrapper) currentPrinter).printText(text, x, y, font, orientation, height, width);
                }
    
            } else {
                call.reject("Loại máy in không hỗ trợ in text", (Exception) null, null);
                return;
            }
    
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In text thành công");
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
            call.reject("[printQRCode] Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        String data = options.getString("data");
        if (data == null) {
            call.reject("Thiếu dữ liệu QR code", (Exception) null, null);
            return;
        }
        try {
            if (currentPrinter instanceof TsplPrinterWrapper) {
                int x = tsplQrX != null ? tsplQrX : 0;
                int y = tsplQrY != null ? tsplQrY : 0;
                String ecLevel = tsplQrEcLevel != null ? tsplQrEcLevel : "M";
                int cellWidth = tsplQrCellWidth != null ? tsplQrCellWidth : 4;
                String mode = tsplQrMode != null ? tsplQrMode : "A";
                int rotation = tsplQrRotation != null ? tsplQrRotation : 0;
                String model = tsplQrModel != null ? tsplQrModel : "M2";
                String mask = tsplQrMask != null ? tsplQrMask : null;
                String content = tsplQrContent != null ? tsplQrContent : data;
                ((TsplPrinterWrapper) currentPrinter).printQRCode(content, x, y, ecLevel, cellWidth, mode, rotation);
            } else if (currentPrinter instanceof PosPrinterWrapper) {
                int moduleSize = posQrModuleSize != null ? posQrModuleSize : 4;
                int ecLevel = posQrEcLevel != null ? posQrEcLevel : 0;
                String alignStr = posQrAlignment != null ? posQrAlignment : "left";
                int alignment = 0;
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = net.posprinter.POSConst.ALIGNMENT_CENTER; break;
                    case "right": alignment = net.posprinter.POSConst.ALIGNMENT_RIGHT; break;
                    default: alignment = net.posprinter.POSConst.ALIGNMENT_LEFT; break;
                }
                android.util.Log.d("CapacitorXprinter", "printQRCode POS: data=" + data + ", moduleSize=" + moduleSize + ", ecLevel=" + ecLevel + ", alignment=" + alignment);
                ((PosPrinterWrapper) currentPrinter).printQRCode(data, moduleSize, ecLevel, alignment);
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int model = options.has("model") ? options.getInteger("model") : 2;
                int unitWidth = options.has("unitWidth") ? options.getInteger("unitWidth") : 6;
                ((CpclPrinterWrapper) currentPrinter).printQRCode(data, x, y, model, unitWidth);
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
            call.reject("[printBarcode] Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        String data = options.getString("data");
        if (data == null) {
            call.reject("Thiếu dữ liệu barcode", (Exception) null, null);
            return;
        }
        try {
            if (currentPrinter instanceof TsplPrinterWrapper) {
                int x = tsplBarcodeX != null ? tsplBarcodeX : 0;
                int y = tsplBarcodeY != null ? tsplBarcodeY : 0;
                String codeType = tsplBarcodeType != null ? tsplBarcodeType : "128";
                int height = tsplBarcodeHeight != null ? tsplBarcodeHeight : 40;
                int readable = tsplBarcodeReadable != null ? tsplBarcodeReadable : 1;
                int rotation = tsplBarcodeRotation != null ? tsplBarcodeRotation : 0;
                int narrow = tsplBarcodeNarrow != null ? tsplBarcodeNarrow : 2;
                int wide = tsplBarcodeWide != null ? tsplBarcodeWide : 2;
                String content = tsplBarcodeContent != null ? tsplBarcodeContent : data;
                ((TsplPrinterWrapper) currentPrinter).printBarcode(content, x, y, codeType, height, readable, rotation, narrow, wide);
            } else if (currentPrinter instanceof PosPrinterWrapper) {
                int codeType = posBarcodeType != null ? posBarcodeType : 73; // CODE128
                int width = posBarcodeWidth != null ? posBarcodeWidth : 2;
                int height = posBarcodeHeight != null ? posBarcodeHeight : 162;
                String alignStr = posBarcodeAlignment != null ? posBarcodeAlignment : "left";
                int alignment = 0;
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = net.posprinter.POSConst.ALIGNMENT_CENTER; break;
                    case "right": alignment = net.posprinter.POSConst.ALIGNMENT_RIGHT; break;
                    default: alignment = net.posprinter.POSConst.ALIGNMENT_LEFT; break;
                }
                int textPosition = posBarcodeTextPosition != null ? posBarcodeTextPosition : 2;
                ((PosPrinterWrapper) currentPrinter).printBarcode(data, codeType, width, height, alignment, textPosition);
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                String codeType = options.getString("codeType", "128");
                int height = options.has("height") ? options.getInteger("height") : 32;
                int readable = options.has("readable") ? options.getInteger("readable") : 1;
                ((CpclPrinterWrapper) currentPrinter).printBarcode(data, x, y, codeType, height, readable);
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

    public void configTsplImage(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof TsplPrinterWrapper)) {
            call.reject("Chưa kết nối TSPLPrinter", (Exception) null, null);
            return;
        }
        try {
            // Nhận các tham số cấu hình từ options
            if (options.has("x")) tsplImageX = options.getInteger("x");
            if (options.has("y")) tsplImageY = options.getInteger("y");
            if (options.has("width")) tsplImageWidth = options.getInteger("width");
            if (options.has("mode")) {
                Object modeObj = options.get("mode");
                if (modeObj instanceof Number) tsplImageMode = ((Number) modeObj).intValue();
                else if (modeObj instanceof String) {
                    // Chuyển đổi tên mode sang số nếu cần
                    String modeStr = (String) modeObj;
                    switch (modeStr) {
                        case "OVERWRITE": tsplImageMode = 0; break;
                        case "OR": tsplImageMode = 1; break;
                        case "XOR": tsplImageMode = 2; break;
                        case "OVERWRITE_C": tsplImageMode = 128; break;
                        case "OR_C": tsplImageMode = 129; break;
                        case "XOR_C": tsplImageMode = 130; break;
                        default: tsplImageMode = 0; break;
                    }
                }
            }
            if (options.has("bitmap")) tsplImageBitmap = options.getString("bitmap");
            if (options.has("algorithm")) tsplImageAlgorithm = options.getString("algorithm");
            if (options.has("density")) tsplImageDensity = options.getInteger("density");
            if (options.has("speed")) {
                Object speedObj = options.get("speed");
                if (speedObj instanceof Number) tsplImageSpeed = ((Number) speedObj).doubleValue();
                else if (speedObj instanceof String) {
                    try { tsplImageSpeed = Double.parseDouble((String) speedObj); } catch (Exception e) { tsplImageSpeed = null; }
                }
            }
            if (options.has("direction")) {
                Object dirObj = options.get("direction");
                if (dirObj instanceof Number) tsplImageDirection = ((Number) dirObj).intValue();
                else if (dirObj instanceof String) {
                    String dirStr = (String) dirObj;
                    if (dirStr.equalsIgnoreCase("FORWARD")) tsplImageDirection = 0;
                    else if (dirStr.equalsIgnoreCase("REVERSE")) tsplImageDirection = 1;
                }
            }
            if (options.has("mirror")) tsplImageMirror = options.getBool("mirror");
            if (options.has("referenceX")) tsplImageReferenceX = options.getInteger("referenceX");
            if (options.has("referenceY")) tsplImageReferenceY = options.getInteger("referenceY");
            if (options.has("sizeWidthMm")) {
                Object wObj = options.get("sizeWidthMm");
                if (wObj instanceof Number) tsplImageSizeWidthMm = ((Number) wObj).doubleValue();
                else if (wObj instanceof String) {
                    try { tsplImageSizeWidthMm = Double.parseDouble((String) wObj); } catch (Exception e) { tsplImageSizeWidthMm = null; }
                }
            }
            if (options.has("sizeHeightMm")) {
                Object hObj = options.get("sizeHeightMm");
                if (hObj instanceof Number) tsplImageSizeHeightMm = ((Number) hObj).doubleValue();
                else if (hObj instanceof String) {
                    try { tsplImageSizeHeightMm = Double.parseDouble((String) hObj); } catch (Exception e) { tsplImageSizeHeightMm = null; }
                }
            }
            if (options.has("gapMmM")) {
                Object mObj = options.get("gapMmM");
                if (mObj instanceof Number) tsplImageGapMmM = ((Number) mObj).doubleValue();
                else if (mObj instanceof String) {
                    try { tsplImageGapMmM = Double.parseDouble((String) mObj); } catch (Exception e) { tsplImageGapMmM = null; }
                }
            }
            if (options.has("gapMmN")) {
                Object nObj = options.get("gapMmN");
                if (nObj instanceof Number) tsplImageGapMmN = ((Number) nObj).doubleValue();
                else if (nObj instanceof String) {
                    try { tsplImageGapMmN = Double.parseDouble((String) nObj); } catch (Exception e) { tsplImageGapMmN = null; }
                }
            }
            if (options.has("cls")) tsplImageCls = options.getBool("cls");
            if (options.has("eraseX")) tsplImageEraseX = options.getInteger("eraseX");
            if (options.has("eraseY")) tsplImageEraseY = options.getInteger("eraseY");
            if (options.has("eraseWidth")) tsplImageEraseWidth = options.getInteger("eraseWidth");
            if (options.has("eraseHeight")) tsplImageEraseHeight = options.getInteger("eraseHeight");
            if (options.has("bitmapCompression")) tsplImageBitmapCompression = options.getBool("bitmapCompression");

            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Cấu hình image TSPL thành công");
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
        if (currentPrinter == null) {
            call.reject("[printImageFromPath] Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        String imagePath = options.getString("bitmap");
        if (imagePath == null) {
            call.reject("Thiếu đường dẫn hình ảnh", (Exception) null, null);
            return;
        }
        try {
            if (currentPrinter instanceof TsplPrinterWrapper) {
                int x = tsplImageX != null ? tsplImageX : 0;
                int y = tsplImageY != null ? tsplImageY : 0;
                int mode = tsplImageMode != null ? tsplImageMode : 0;
                String bitmap = tsplImageBitmap != null ? tsplImageBitmap : imagePath;
                ((TsplPrinterWrapper) currentPrinter).printImageFromPath(bitmap, x, y, mode, context);
            } else if (currentPrinter instanceof PosPrinterWrapper) {
                String alignStr = posImageAlignment != null ? posImageAlignment : "left";
                int alignment = 0;
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = net.posprinter.POSConst.ALIGNMENT_CENTER; break;
                    case "right": alignment = net.posprinter.POSConst.ALIGNMENT_RIGHT; break;
                    default: alignment = net.posprinter.POSConst.ALIGNMENT_LEFT; break;
                }
                int width = posImageWidth != null ? posImageWidth : 0;
                // int mode = posImageMode != null ? posImageMode : 0; // Không dùng cho PosPrinterWrapper
                ((PosPrinterWrapper) currentPrinter).printImageFromPath(imagePath, width, alignment, context);
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                String path = cpclImagePath != null ? cpclImagePath : imagePath;
                int x = cpclImageX != null ? cpclImageX : 0;
                int y = cpclImageY != null ? cpclImageY : 0;
                int mode = cpclImageMode != null ? cpclImageMode : 0;
                ((CpclPrinterWrapper) currentPrinter).printImageFromPath(path, x, y, mode, context);
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int width = options.has("width") ? options.getInteger("width") : 200;
                int height = options.has("height") ? options.getInteger("height") : 200;
                ((ZplPrinterWrapper) currentPrinter).printImageFromPath(imagePath, x, y, width, height, context);
            }
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "In hình ảnh thành công");
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

    public void printImageBase64(JSObject options, PluginCall call) {
        if (currentPrinter == null) {
            call.reject("[printImageBase64] Chưa kết nối máy in", (Exception) null, null);
            return;
        }
        String base64 = options.getString("bitmap");
        if (base64 == null) {
            call.reject("Thiếu dữ liệu base64", (Exception) null, null);
            return;
        }
        try {
            if (currentPrinter instanceof TsplPrinterWrapper) {
                TsplPrinterWrapper tspl = (TsplPrinterWrapper) currentPrinter;
                if (Boolean.TRUE.equals(tsplImageCls)) {
                    tspl.cls();
                }
                if (tsplImageReferenceX != null && tsplImageReferenceY != null) {
                    tspl.reference(tsplImageReferenceX, tsplImageReferenceY);
                }
                if (tsplImageSizeWidthMm != null && tsplImageSizeHeightMm != null) {
                    tspl.sizeMm(tsplImageSizeWidthMm, tsplImageSizeHeightMm);
                }
                if (tsplImageGapMmM != null && tsplImageGapMmN != null) {
                    tspl.gapMm(tsplImageGapMmM, tsplImageGapMmN);
                }
                if (tsplImageDensity != null) {
                    tspl.density(tsplImageDensity);
                }
                if (tsplImageSpeed != null) {
                    tspl.speed(tsplImageSpeed);
                }
                if (tsplImageDirection != null && tsplImageMirror != null) {
                    tspl.direction(tsplImageDirection, tsplImageMirror);
                } else if (tsplImageDirection != null) {
                    tspl.direction(tsplImageDirection);
                }
                if (tsplImageEraseX != null && tsplImageEraseY != null && tsplImageEraseWidth != null && tsplImageEraseHeight != null) {
                    tspl.erase(tsplImageEraseX, tsplImageEraseY, tsplImageEraseWidth, tsplImageEraseHeight);
                }
                tspl.printImageBase64(
                    tsplImageBitmap != null ? tsplImageBitmap : base64,
                    tsplImageX != null ? tsplImageX : 0,
                    tsplImageY != null ? tsplImageY : 0,
                    tsplImageMode != null ? tsplImageMode : 0
                );
                // Reset các biến cấu hình sau khi in
                tsplImageX = null;
                tsplImageY = null;
                tsplImageMode = null;
                tsplImageWidth = null;
                tsplImageBitmap = null;
                tsplImageAlgorithm = null;
                tsplImageDensity = null;
                tsplImageSpeed = null;
                tsplImageDirection = null;
                tsplImageMirror = null;
                tsplImageReferenceX = null;
                tsplImageReferenceY = null;
                tsplImageSizeWidthMm = null;
                tsplImageSizeHeightMm = null;
                tsplImageGapMmM = null;
                tsplImageGapMmN = null;
                tsplImageCls = null;
                tsplImageEraseX = null;
                tsplImageEraseY = null;
                tsplImageEraseWidth = null;
                tsplImageEraseHeight = null;
                tsplImageBitmapCompression = null;
                JSObject ret = new JSObject();
                ret.put("code", 200);
                ret.put("msg", "In hình ảnh base64 TSPL thành công");
                ret.put("data", null);
                call.resolve(ret);
            } else if (currentPrinter instanceof PosPrinterWrapper) {
                String alignStr = posImageAlignment != null ? posImageAlignment : "left";
                int alignment = 0;
                switch (alignStr.toLowerCase()) {
                    case "center": alignment = net.posprinter.POSConst.ALIGNMENT_CENTER; break;
                    case "right": alignment = net.posprinter.POSConst.ALIGNMENT_RIGHT; break;
                    default: alignment = net.posprinter.POSConst.ALIGNMENT_LEFT; break;
                }
                int width = posImageWidth != null ? posImageWidth : 0;
                // int mode = posImageMode != null ? posImageMode : 0; // Không dùng cho PosPrinterWrapper
                ((PosPrinterWrapper) currentPrinter).printImageBase64(base64, width, alignment);
                JSObject ret = new JSObject();
                ret.put("code", 200);
                ret.put("msg", "In hình ảnh base64 thành công");
                ret.put("data", null);
                call.resolve(ret);
            } else if (currentPrinter instanceof CpclPrinterWrapper) {
                String b64 = cpclImageBase64 != null ? cpclImageBase64 : base64;
                int x = cpclImageX != null ? cpclImageX : 0;
                int y = cpclImageY != null ? cpclImageY : 0;
                int mode = cpclImageMode != null ? cpclImageMode : 0;
                ((CpclPrinterWrapper) currentPrinter).printImageBase64(b64, x, y, mode);
                JSObject ret = new JSObject();
                ret.put("code", 200);
                ret.put("msg", "In hình ảnh base64 thành công");
                ret.put("data", null);
                call.resolve(ret);
            } else if (currentPrinter instanceof ZplPrinterWrapper) {
                int x = options.has("x") ? options.getInteger("x") : 0;
                int y = options.has("y") ? options.getInteger("y") : 0;
                int width = options.has("width") ? options.getInteger("width") : 200;
                int height = options.has("height") ? options.getInteger("height") : 200;
                ((ZplPrinterWrapper) currentPrinter).printImageBase64(base64, x, y, width, height);
                JSObject ret = new JSObject();
                ret.put("code", 200);
                ret.put("msg", "In hình ảnh base64 thành công");
                ret.put("data", null);
                call.resolve(ret);
            }
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
            call.reject("[cutPaper] Chưa kết nối máy in", (Exception) null, null);
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
            call.reject("[openCashDrawer] Chưa kết nối máy in", (Exception) null, null);
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
            call.reject("[resetPrinter] Chưa kết nối máy in", (Exception) null, null);
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
            call.reject("[selfTest] Chưa kết nối máy in", (Exception) null, null);
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
            call.reject("[getPrinterStatus] Chưa kết nối máy in", (Exception) null, null);
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

    public void sendPosCommand(JSObject options, PluginCall call) {
        if (currentPrinter == null || !(currentPrinter instanceof PosPrinterWrapper)) {
            call.reject("[sendPosCommand] Chưa kết nối POSPrinter");
            return;
        }
        String command = options.getString("command");
        if (command == null || command.trim().isEmpty()) {
            call.reject("Thiếu lệnh POS");
            return;
        }
        try {
            byte[] data;
            if (command.matches("^[0-9A-Fa-f ]+$")) {
                String hex = command.replaceAll("\\s+", "");
                data = hexStringToByteArray(hex);
            } else {
                data = command.getBytes("GBK");
            }
            ((PosPrinterWrapper) currentPrinter).sendData(data);
            JSObject ret = new JSObject();
            ret.put("code", 200);
            ret.put("msg", "Gửi lệnh POS thành công");
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Lỗi gửi lệnh POS: " + e.getMessage());
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

