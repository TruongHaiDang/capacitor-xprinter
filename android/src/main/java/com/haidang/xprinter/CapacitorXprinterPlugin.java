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

    /**
     * In mã QR
     */
    @PluginMethod
    public void printQRCode(PluginCall call) {
        JSObject options = call.getData();
        implementation.printQRCode(options, call);
    }

    /**
     * In mã vạch 1D
     */
    @PluginMethod
    public void printBarcode(PluginCall call) {
        JSObject options = call.getData();
        implementation.printBarcode(options, call);
    }

    /**
     * In hình ảnh từ đường dẫn
     */
    @PluginMethod
    public void printImageFromPath(PluginCall call) {
        JSObject options = call.getData();
        implementation.printImageFromPath(options, getContext(), call);
    }

    /**
     * In hình ảnh base64
     */
    @PluginMethod
    public void printImageBase64(PluginCall call) {
        JSObject options = call.getData();
        implementation.printImageBase64(options, call);
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
        JSObject options = call.getData();
        implementation.openCashDrawer(options, call);
    }

    /**
     * Kiểm tra trạng thái máy in
     */
    @PluginMethod
    public void getPrinterStatus(PluginCall call) {
        implementation.getPrinterStatus(call);
    }

    /**
     * Đọc dữ liệu phản hồi từ máy in (nếu có)
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
        JSObject options = call.getData();
        implementation.sendRawData(options, call);
    }

    /**
     * In nội dung dạng label cho CPCL / TSPL / ZPL
     */
    @PluginMethod
    public void printLabel(PluginCall call) {
        JSObject options = call.getData();
        implementation.printLabel(options, call);
    }

    /**
     * Thiết lập lại máy in
     */
    @PluginMethod
    public void resetPrinter(PluginCall call) {
        implementation.resetPrinter(call);
    }

    /**
     * Thực hiện in tự test của máy in (self-test)
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
        JSObject options = call.getData();
        implementation.printEncodedText(options, call);
    }

    /**
     * Gửi nhiều lệnh liên tiếp (batch command mode)
     */
    @PluginMethod
    public void sendBatchCommands(PluginCall call) {
        JSObject options = call.getData();
        implementation.sendBatchCommands(options, call);
    }

    /**
     * Thiết lập lại protocol (POS / CPCL / TSPL / ZPL)
     */
    @PluginMethod
    public void setProtocol(PluginCall call) {
        JSObject options = call.getData();
        implementation.setProtocol(options, call);
    }

    /**
     * Cấu hình cho in text
     */
    @PluginMethod
    public void configText(PluginCall call) {
        JSObject options = call.getData();
        implementation.configText(options, call);
    }

    /**
     * Cấu hình cho in barcode
     */
    @PluginMethod
    public void configBarcode(PluginCall call) {
        JSObject options = call.getData();
        implementation.configBarcode(options, call);
    }

    /**
     * Cấu hình cho in QRCode
     */
    @PluginMethod
    public void configQRCode(PluginCall call) {
        JSObject options = call.getData();
        implementation.configQRCode(options, call);
    }

    /**
     * Cấu hình cho in hình ảnh
     */
    @PluginMethod
    public void configImage(PluginCall call) {
        JSObject options = call.getData();
        implementation.configImage(options, call);
    }

    /**
     * Cấu hình cho in label (CPCL/TSPL/ZPL)
     */
    @PluginMethod
    public void configLabel(PluginCall call) {
        JSObject options = call.getData();
        implementation.configLabel(options, call);
    }

}

