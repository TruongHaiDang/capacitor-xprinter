package com.haidang.xprinter;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CapacitorXprinter")
public class CapacitorXprinterPlugin extends Plugin {

    private CapacitorXprinter implementation = new CapacitorXprinter();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    /**
     * Kết nối tới thiết bị máy in dựa trên loại thiết bị và các tham số truyền vào.
     * Trả về kết quả bất đồng bộ qua IConnectListener.
     */
    @PluginMethod
    public void connect(PluginCall call) {
        JSObject options = call.getData();
        android.content.Context context = getContext();
        implementation.connect(options, context, call);
    }

    /**
     * Ngắt kết nối với thiết bị máy in hiện tại.
     * Gọi implementation.disconnect() và trả về kết quả cho phía JS/TS.
     */
    @PluginMethod
    public void disconnect(PluginCall call) {
        HandshakeResponse response = implementation.disconnect();

        JSObject ret = new JSObject();
        ret.put("code", response.code);
        ret.put("msg", response.msg);
        ret.put("data", response.data);

        call.resolve(ret);
    }

    /**
     * Lấy danh sách cổng/thiết bị khả dụng (USB, BLUETOOTH, SERIAL).
     * Trả về mảng string 'ports'.
     */
    @PluginMethod
    public void listAvailablePorts(PluginCall call) {
        String type = call.getString("type");
        java.util.List<String> ports = implementation.listAvailablePorts(type, getContext());

        com.getcapacitor.JSArray arr = new com.getcapacitor.JSArray(ports);
        JSObject ret = new JSObject();
        ret.put("ports", arr);
        call.resolve(ret);
    }

    /**
     * Trả về các hằng số trạng thái của POSConnect.
     */
    @PluginMethod
    public void getStatusConstants(PluginCall call) {
        JSObject constants = implementation.getStatusConstants();
        call.resolve(constants);
    }

    /**
     * Lấy danh sách thiết bị USB chi tiết (UsbDevice).
     * Trả về mảng object 'devices' với thông tin cơ bản của từng thiết bị.
     */
    @PluginMethod
    public void listUsbDevices(PluginCall call) {
        java.util.List<android.hardware.usb.UsbDevice> devices = implementation.listUsbDevices(getContext());
        com.getcapacitor.JSArray arr = new com.getcapacitor.JSArray();
        for (android.hardware.usb.UsbDevice dev : devices) {
            JSObject obj = new JSObject();
            obj.put("deviceId", dev.getDeviceId());
            obj.put("vendorId", dev.getVendorId());
            obj.put("productId", dev.getProductId());
            obj.put("deviceName", dev.getDeviceName());
            arr.put(obj);
        }
        JSObject ret = new JSObject();
        ret.put("devices", arr);
        call.resolve(ret);
    }

    /**
     * Lấy danh sách cổng Serial (COM).
     * Trả về mảng string 'ports'.
     */
    @PluginMethod
    public void listSerialPorts(PluginCall call) {
        java.util.List<String> ports = implementation.listSerialPorts(getContext());
        com.getcapacitor.JSArray arr = new com.getcapacitor.JSArray(ports);
        JSObject ret = new JSObject();
        ret.put("ports", arr);
        call.resolve(ret);
    }

    /**
     * Kết nối tới máy in qua địa chỉ MAC (LAN/Ethernet).
     * Truyền vào chuỗi 'mac'.
     */
    @PluginMethod
    public void connectByMac(PluginCall call) {
        String mac = call.getString("mac");
        implementation.connectByMac(mac, getContext(), call);
    }

    /**
     * In văn bản (POS Printer)
     */
    @PluginMethod
    public void printText(PluginCall call) {
        JSObject options = call.getData();
        implementation.printText(options, call);
    }

}

    /**
     * In mã QR
     */
    @PluginMethod
    public void printQRCode(PluginCall call) {
        implementation.printQRCode(call.getData(), call);
    }

    /**
     * In mã vạch
     */
    @PluginMethod
    public void printBarcode(PluginCall call) {
        implementation.printBarcode(call.getData(), call);
    }

    /**
     * In hình ảnh từ đường dẫn
     */
    @PluginMethod
    public void printImageFromPath(PluginCall call) {
        implementation.printImageFromPath(call.getData(), call);
    }

    /**
     * In hình ảnh từ base64
     */
    @PluginMethod
    public void printImageBase64(PluginCall call) {
        implementation.printImageBase64(call.getData(), call);
    }

    /**
     * Cắt giấy (POSPrinter)
     */
    @PluginMethod
    public void cutPaper(PluginCall call) {
        implementation.cutPaper(call);
    }

    /**
     * Mở két tiền (POSPrinter)
     */
    @PluginMethod
    public void openCashDrawer(PluginCall call) {
        implementation.openCashDrawer(call.getData(), call);
    }

    /**
     * Kiểm tra trạng thái máy in
     */
    @PluginMethod
    public void getPrinterStatus(PluginCall call) {
        implementation.getPrinterStatus(call);
    }

    /**
     * Đọc dữ liệu phản hồi từ máy in
     */
    @PluginMethod
    public void readData(PluginCall call) {
        implementation.readData(call);
    }

    /**
     * Gửi dữ liệu tùy ý (raw byte)
     */
    @PluginMethod
    public void sendRawData(PluginCall call) {
        implementation.sendRawData(call.getData(), call);
    }

    /**
     * In nội dung dạng label cho CPCL / TSPL / ZPL
     */
    @PluginMethod
    public void printLabel(PluginCall call) {
        implementation.printLabel(call.getData(), call);
    }

    /**
     * Thiết lập lại máy in
     */
    @PluginMethod
    public void resetPrinter(PluginCall call) {
        implementation.resetPrinter(call);
    }

    /**
     * Thực hiện in tự test của máy in
     */
    @PluginMethod
    public void selfTest(PluginCall call) {
        implementation.selfTest(call);
    }

    /**
     * In văn bản với encoding cụ thể
     */
    @PluginMethod
    public void printEncodedText(PluginCall call) {
        implementation.printEncodedText(call.getData(), call);
    }

    /**
     * Gửi nhiều lệnh liên tiếp
     */
    @PluginMethod
    public void sendBatchCommands(PluginCall call) {
        implementation.sendBatchCommands(call.getData(), call);
    }

    /**
     * Kiểm tra kết nối hiện tại có đang hoạt động không
     */
    @PluginMethod
    public void isConnected(PluginCall call) {
        boolean connected = implementation.isConnected();
        JSObject ret = new JSObject();
        ret.put("connected", connected);
        call.resolve(ret);
    }

    /**
     * Thiết lập lại protocol
     */
    @PluginMethod
    public void setProtocol(PluginCall call) {
        implementation.setProtocol(call.getData(), call);
    }

    /**
     * Cấu hình cho in text
     */
    @PluginMethod
    public void configText(PluginCall call) {
        implementation.configText(call.getData(), call);
    }

    /**
     * Cấu hình cho in barcode
     */
    @PluginMethod
    public void configBarcode(PluginCall call) {
        implementation.configBarcode(call.getData(), call);
    }

    /**
     * Cấu hình cho in QRCode
     */
    @PluginMethod
    public void configQRCode(PluginCall call) {
        implementation.configQRCode(call.getData(), call);
    }

    /**
     * Cấu hình cho in hình ảnh
     */
    @PluginMethod
    public void configImage(PluginCall call) {
        implementation.configImage(call.getData(), call);
    }

    /**
     * Cấu hình cho in label
     */
    @PluginMethod
    public void configLabel(PluginCall call) {
        implementation.configLabel(call.getData(), call);
    }

