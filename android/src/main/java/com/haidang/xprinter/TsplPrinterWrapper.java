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
        printer.text(x, y, font, rotation, xScale, yScale, text);
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
        printer.qrcode(x, y, ecLevel, cellWidth, mode, rotation, data);
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
        printer.barcode(x, y, codeType, height, readable, rotation, narrow, wide, data);
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
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(imagePath);
        if (bmp != null) {
            printer.bitmap(x, y, 0, bmp.getWidth(), bmp);
            printer.sendData(new byte[0]);
        } else {
            throw new RuntimeException("Không đọc được file hình ảnh: " + imagePath);
        }
    }

    /**
     * In hình ảnh TSPL từ base64
     * @param base64 Chuỗi base64 của hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param mode Mode in
     */
    public void printImageBase64(String base64, int x, int y, String mode) {
        try {
            byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            printer.bitmap(x, y, 0, bmp.getWidth(), bmp);
            printer.sendData(new byte[0]);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã base64 hình ảnh", e);
        }
    }

    /**
     * Thiết lập kích thước label
     * @param width Độ rộng (mm)
     * @param height Chiều cao (mm)
     */
    public void setSize(double width, double height) {
        printer.sizeMm(width, height);
    }

    /**
     * Thiết lập gap
     * @param gap Khoảng cách gap (mm)
     * @param offset Offset (mm)
     */
    public void setGap(double gap, double offset) {
        printer.gapMm(gap, offset);
    }

    /**
     * Thiết lập hướng in
     * @param direction Hướng (0-normal, 1-reverse)
     */
    public void setDirection(int direction) {
        printer.direction(direction);
    }

    /**
     * Clear buffer
     */
    public void clearBuffer() {
        printer.cls();
    }

    /**
     * In label
     * @param sets Số bộ
     * @param copies Số bản sao
     */
    public void printLabel(int sets, int copies) {
        printer.print(sets);
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

