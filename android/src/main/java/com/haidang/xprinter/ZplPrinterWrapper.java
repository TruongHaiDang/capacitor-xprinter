package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.ZPLPrinter;
import net.posprinter.posprinterface.IStatusCallback;

public class ZplPrinterWrapper implements PrinterBase {
    private final ZPLPrinter printer;

    public ZplPrinterWrapper(IDeviceConnection connection) {
        this.printer = new ZPLPrinter(connection);
    }

    @Override
    public void sendData(byte[] data) {
        printer.sendData(data);
    }

    @Override
    public void printerStatus(IStatusCallback callback) {
        printer.printerStatus(callback);
    }

    // Có thể bổ sung thêm các hàm đặc thù ZPLPrinter ở đây
} 
package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.ZPLPrinter;
import net.posprinter.posprinterface.IStatusCallback;

public class ZplPrinterWrapper implements PrinterBase {
    private final ZPLPrinter printer;

    public ZplPrinterWrapper(IDeviceConnection connection) {
        this.printer = new ZPLPrinter(connection);
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
     * In văn bản ZPL
     * @param text Nội dung văn bản
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param font Font
     * @param orientation Hướng (N, R, I, B)
     * @param height Chiều cao font
     * @param width Độ rộng font
     */
    public void printText(String text, int x, int y, String font, String orientation, int height, int width) {
        printer.printText(text, x, y, font, orientation, height, width);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã QR ZPL
     * @param data Dữ liệu mã QR
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param model Model (1 hoặc 2)
     * @param magnification Độ phóng đại (1-10)
     * @param errorCorrection Mức độ sửa lỗi (H, Q, M, L)
     * @param maskValue Mask value (0-7)
     */
    public void printQRCode(String data, int x, int y, int model, int magnification, String errorCorrection, int maskValue) {
        printer.printQRCode(data, x, y, model, magnification, errorCorrection, maskValue);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã vạch ZPL
     * @param data Dữ liệu mã vạch
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param codeType Loại mã vạch
     * @param orientation Hướng (N, R, I, B)
     * @param height Chiều cao
     * @param printInterpretationLine In text (Y/N)
     * @param printInterpretationLineAbove Text ở trên (Y/N)
     * @param checkDigit Check digit (Y/N)
     */
    public void printBarcode(String data, int x, int y, String codeType, String orientation, int height, String printInterpretationLine, String printInterpretationLineAbove, String checkDigit) {
        printer.printBarcode(data, x, y, codeType, orientation, height, printInterpretationLine, printInterpretationLineAbove, checkDigit);
        printer.sendData(new byte[0]);
    }

    /**
     * In hình ảnh ZPL từ đường dẫn
     * @param imagePath Đường dẫn hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param width Độ rộng
     * @param height Chiều cao
     */
    public void printImageFromPath(String imagePath, int x, int y, int width, int height) {
        printer.printGraphic(imagePath, x, y, width, height);
        printer.sendData(new byte[0]);
    }

    /**
     * In hình ảnh ZPL từ base64
     * @param base64 Chuỗi base64 của hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param width Độ rộng
     * @param height Chiều cao
     */
    public void printImageBase64(String base64, int x, int y, int width, int height) {
        printer.printGraphicBase64(base64, x, y, width, height);
        printer.sendData(new byte[0]);
    }

    /**
     * Bắt đầu format label
     */
    public void startFormat() {
        printer.startFormat();
    }

    /**
     * Kết thúc format label
     */
    public void endFormat() {
        printer.endFormat();
        printer.sendData(new byte[0]);
    }

    /**
     * Thiết lập độ dài label
     * @param length Độ dài (dots)
     */
    public void setLabelLength(int length) {
        printer.setLabelLength(length);
    }

    /**
     * Thiết lập home position
     * @param x Tọa độ X
     * @param y Tọa độ Y
     */
    public void setLabelHome(int x, int y) {
        printer.setLabelHome(x, y);
    }

    /**
     * Thiết lập độ đậm in
     * @param darkness Độ đậm (0-30)
     */
    public void setPrintDarkness(int darkness) {
        printer.setPrintDarkness(darkness);
    }

    /**
     * Thiết lập tốc độ in
     * @param speed Tốc độ (2-14)
     */
    public void setPrintSpeed(int speed) {
        printer.setPrintSpeed(speed);
    }

    /**
     * In số lượng label
     * @param quantity Số lượng
     */
    public void printQuantity(int quantity) {
        printer.printQuantity(quantity);
        printer.sendData(new byte[0]);
    }

    /**
     * Gửi lệnh ZPL tùy chỉnh
     * @param command Lệnh ZPL
     */
    public void sendCommand(String command) {
        printer.sendData(command.getBytes());
    }
}

