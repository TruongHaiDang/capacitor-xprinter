package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.TSPLPrinter;
import net.posprinter.posprinterface.IStatusCallback;

public class TsplPrinterWrapper implements PrinterBase {
    private final TSPLPrinter printer;

    public TsplPrinterWrapper(IDeviceConnection connection) {
        this.printer = new TSPLPrinter(connection);
    }

    @Override
    public void sendData(byte[] data) {
        printer.sendData(data);
    }

    @Override
    public void printerStatus(IStatusCallback callback) {
        printer.printerStatus(callback);
    }

    // Có thể bổ sung thêm các hàm đặc thù TSPLPrinter ở đây
} 
package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.TSPLPrinter;
import net.posprinter.posprinterface.IStatusCallback;

public class TsplPrinterWrapper implements PrinterBase {
    private final TSPLPrinter printer;

    public TsplPrinterWrapper(IDeviceConnection connection) {
        this.printer = new TSPLPrinter(connection);
    }

    @Override
    public void sendData(byte[] data) {
        printer.sendData(data);
    }

    @Override
    public void printerStatus(IStatusCallback callback) {
        printer.printerStatus(callback);
    }

    /**
     * In văn bản TSPL
     * @param text Nội dung văn bản
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param font Font
     * @param rotation Góc xoay (0, 90, 180, 270)
     * @param xScale Tỷ lệ X
     * @param yScale Tỷ lệ Y
     */
    public void printText(String text, int x, int y, String font, int rotation, int xScale, int yScale) {
        printer.printText(text, x, y, font, rotation, xScale, yScale);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã QR TSPL
     * @param data Dữ liệu mã QR
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param ecLevel Mức độ sửa lỗi
     * @param cellWidth Độ rộng cell
     * @param mode Mode (A-Auto, M-Manual)
     * @param rotation Góc xoay
     * @param model Model (M1, M2)
     */
    public void printQRCode(String data, int x, int y, String ecLevel, int cellWidth, String mode, int rotation, String model) {
        printer.printQRCode(data, x, y, ecLevel, cellWidth, mode, rotation, model);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã vạch TSPL
     * @param data Dữ liệu mã vạch
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param codeType Loại mã vạch
     * @param height Chiều cao
     * @param readable Hiển thị text (0-không, 1-có)
     * @param rotation Góc xoay
     * @param narrow Độ rộng narrow
     * @param wide Độ rộng wide
     */
    public void printBarcode(String data, int x, int y, String codeType, int height, int readable, int rotation, int narrow, int wide) {
        printer.printBarcode(data, x, y, codeType, height, readable, rotation, narrow, wide);
        printer.sendData(new byte[0]);
    }

    /**
     * In hình ảnh TSPL từ đường dẫn
     * @param imagePath Đường dẫn hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param mode Mode in
     */
    public void printImageFromPath(String imagePath, int x, int y, String mode) {
        printer.printBitmap(imagePath, x, y, mode);
        printer.sendData(new byte[0]);
    }

    /**
     * In hình ảnh TSPL từ base64
     * @param base64 Chuỗi base64 của hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param mode Mode in
     */
    public void printImageBase64(String base64, int x, int y, String mode) {
        printer.printBitmapBase64(base64, x, y, mode);
        printer.sendData(new byte[0]);
    }

    /**
     * Thiết lập kích thước label
     * @param width Độ rộng (mm)
     * @param height Chiều cao (mm)
     */
    public void setSize(double width, double height) {
        printer.setSize(width, height);
    }

    /**
     * Thiết lập gap
     * @param gap Khoảng cách gap (mm)
     * @param offset Offset (mm)
     */
    public void setGap(double gap, double offset) {
        printer.setGap(gap, offset);
    }

    /**
     * Thiết lập hướng in
     * @param direction Hướng (0-normal, 1-reverse)
     */
    public void setDirection(int direction) {
        printer.setDirection(direction);
    }

    /**
     * Clear buffer
     */
    public void clearBuffer() {
        printer.clearBuffer();
    }

    /**
     * In label
     * @param sets Số bộ
     * @param copies Số bản sao
     */
    public void printLabel(int sets, int copies) {
        printer.print(sets, copies);
        printer.sendData(new byte[0]);
    }

    /**
     * Gửi lệnh TSPL tùy chỉnh
     * @param command Lệnh TSPL
     */
    public void sendCommand(String command) {
        printer.sendData(command.getBytes());
    }
}

