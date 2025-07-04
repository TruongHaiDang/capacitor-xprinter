package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.CPCLPrinter;
import net.posprinter.posprinterface.IStatusCallback;

public class CpclPrinterWrapper implements PrinterBase {
    private final CPCLPrinter printer;

    public CpclPrinterWrapper(IDeviceConnection connection) {
        this.printer = new CPCLPrinter(connection);
    }

    @Override
    public void sendData(byte[] data) {
        printer.sendData(data);
    }

    @Override
    public void printerStatus(IStatusCallback callback) {
        printer.printerStatus(callback);
    }

    // Có thể bổ sung thêm các hàm đặc thù CPCLPrinter ở đây
} 
    /**
     * In văn bản CPCL
     * @param text Nội dung văn bản
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param font Font (0-7)
     * @param rotation Góc xoay (0, 90, 180, 270)
     */
    public void printText(String text, int x, int y, int font, int rotation) {
        printer.printText(text, x, y, font, rotation);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã QR CPCL
     * @param data Dữ liệu mã QR
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param model Model QR (1 hoặc 2)
     * @param unitWidth Độ rộng đơn vị
     */
    public void printQRCode(String data, int x, int y, int model, int unitWidth) {
        printer.printQRCode(data, x, y, model, unitWidth);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã vạch CPCL
     * @param data Dữ liệu mã vạch
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param codeType Loại mã vạch
     * @param height Chiều cao
     * @param readable Hiển thị text (0-không, 1-có)
     */
    public void printBarcode(String data, int x, int y, String codeType, int height, int readable) {
        printer.printBarcode(data, x, y, codeType, height, readable);
        printer.sendData(new byte[0]);
    }

    /**
     * In hình ảnh CPCL từ đường dẫn
     * @param imagePath Đường dẫn hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param mode Mode in (0-overwrite, 1-or, 2-xor)
     */
    public void printImageFromPath(String imagePath, int x, int y, int mode) {
        printer.printGraphics(imagePath, x, y, mode);
        printer.sendData(new byte[0]);
    }

    /**
     * In hình ảnh CPCL từ base64
     * @param base64 Chuỗi base64 của hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param mode Mode in (0-overwrite, 1-or, 2-xor)
     */
    public void printImageBase64(String base64, int x, int y, int mode) {
        printer.printGraphicsBase64(base64, x, y, mode);
        printer.sendData(new byte[0]);
    }

    /**
     * Thiết lập kích thước label
     * @param width Độ rộng
     * @param height Chiều cao
     * @param quantity Số lượng
     */
    public void setLabel(int width, int height, int quantity) {
        printer.setLabel(width, height, quantity);
    }

    /**
     * In label
     */
    public void printLabel() {
        printer.print();
        printer.sendData(new byte[0]);
    }

    /**
     * Gửi lệnh CPCL tùy chỉnh
     * @param command Lệnh CPCL
     */
    public void sendCommand(String command) {
        printer.sendData(command.getBytes());
    }

