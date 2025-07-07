package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.TSPLPrinter;
import net.posprinter.posprinterface.IStatusCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.InputStream;

public class TsplPrinterWrapper implements PrinterBase {
    private final TSPLPrinter printer;
    // Giá trị mặc định dựa trên selftest máy in
    private double labelWidthMm = 72.0;  // mm
    private double labelHeightMm = 30.0; // mm
    private int printSpeed = 3;          // tốc độ in
    private int printDensity = 7;        // độ đậm
    private StringBuilder labelBuffer = new StringBuilder();

    public TsplPrinterWrapper(IDeviceConnection connection) {
        this.printer = new TSPLPrinter(connection);
        // Thiết lập mặc định theo selftest
        printer.sizeMm(labelWidthMm, labelHeightMm);
        printer.speed(printSpeed);
        printer.density(printDensity);
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
     * Xóa buffer (clear label, bắt buộc trước khi vẽ mới)
     */
    public void clearBuffer() {
        printer.cls();
    }

    /**
     * Vẽ text lên buffer máy in (chưa in)
     */
    public void drawText(String text, int x, int y, String font, int rotation, int xScale, int yScale) {
        printer.text(x, y, font, rotation, xScale, yScale, text);
    }

    /**
     * Vẽ barcode lên buffer máy in (chưa in) - luôn truyền đủ tham số chuẩn TSPL
     */
    public void drawBarcode(String data, int x, int y, String codeType, int height, int readable, int rotation, int narrow, int wide) {
        printer.cls();
        String type = (codeType != null) ? codeType : "128";
        int h = (height > 0) ? height : 80;
        int r = (readable == 0 || readable == 1) ? readable : 1;
        int rot = (rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270) ? rotation : 0;
        int n = (narrow > 0) ? narrow : 2;
        int w = (wide > 0) ? wide : 2;
        printer.barcode(x, y, type, h, r, rot, n, w, data);
        printer.print(1);
    }

    /**
     * Vẽ QRCode lên buffer máy in (chưa in) - luôn truyền model và mask cho chắc chắn
     */
    public void drawQRCode(String data, int x, int y, String ecLevel, int cellWidth, String mode, int rotation, String model) {
        printer.cls();
        String qrModel = (model != null) ? model : "M2";
        String mask = "0";
        printer.qrcode(x, y, ecLevel, cellWidth, mode, rotation, qrModel, mask, data);
        printer.print(1);
    }

    /**
     * Vẽ hình ảnh từ path lên buffer máy in (chưa in) - đảm bảo ảnh hợp lệ
     */
    public void drawImageFromPath(String imagePath, int x, int y, int mode, Context context) {
        printer.cls();
        Bitmap bmp = null;
        try {
            if (imagePath != null && imagePath.startsWith("content://")) {
                InputStream input = context.getContentResolver().openInputStream(Uri.parse(imagePath));
                bmp = BitmapFactory.decodeStream(input);
            } else {
                bmp = BitmapFactory.decodeFile(imagePath);
            }
        } catch (Exception e) {
            bmp = null;
        }
        if (bmp != null && bmp.getWidth() > 0 && bmp.getHeight() > 0) {
            printer.bitmap(x, y, mode, bmp.getWidth(), bmp);
        }
        printer.print(1);
    }

    /**
     * Vẽ hình ảnh từ base64 lên buffer máy in (chưa in) - đảm bảo ảnh hợp lệ
     */
    public void drawImageBase64(String base64, int x, int y, int mode) {
        printer.cls();
        try {
            byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            if (bmp != null && bmp.getWidth() > 0 && bmp.getHeight() > 0) {
                printer.bitmap(x, y, mode, bmp.getWidth(), bmp);
            }
        } catch (Exception e) {
            // Bỏ qua lỗi ảnh
        }
        printer.print(1);
    }

    /**
     * In label: gửi lệnh in xuống máy in
     * @param sets Số bộ
     * @param copies Số bản sao (không dùng với TSPL)
     */
    public void printLabel(int sets, int copies) {
        printer.print(sets);
    }

    /**
     * Thiết lập kích thước label
     * @param width Độ rộng (mm)
     * @param height Chiều cao (mm)
     */
    public void setSize(double width, double height) {
        this.labelWidthMm = width;
        this.labelHeightMm = height;
        printer.sizeMm(width, height);
    }

    public double getLabelWidthMm() { return labelWidthMm; }
    public double getLabelHeightMm() { return labelHeightMm; }

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
     * Gửi lệnh TSPL tùy chỉnh
     * @param command Lệnh TSPL
     */
    public void sendCommand(String command) {
        printer.sendData(command.getBytes());
    }

    /**
     * Hàm tiện lợi: In text TSPL (tự động clear, vẽ text, in 1 bản)
     * Nếu x/y/font null thì dùng mặc định & căn giữa sơ bộ.
     */
    public void printText(String text, Integer x, Integer y, String font) {
        int labelWidthDot  = (int) (labelWidthMm * 8);
        int labelHeightDot = (int) (labelHeightMm * 8);
        int posX = (x != null) ? x : Math.max(0, (labelWidthDot - text.length() * 8) / 2);
        int posY = (y != null) ? y : 30;
        String fontName = (font != null) ? font : "0";
        printer.cls();
        printer.text(posX, posY, fontName, 0, 1, 1, text);
        printer.print(1);
    }
}

