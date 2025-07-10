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
     * Vẽ text lên buffer máy in (chưa in)
     */
    public void printText(String text, int x, int y, String font, int rotation, int xScale, int yScale) {
        printer.cls();
        printer.text(x, y, font, rotation, xScale, yScale, text);
        printer.sendData(new byte[0]);
        printer.print(1);
    }

    /**
     * Vẽ barcode lên buffer máy in (chưa in) - luôn truyền đủ tham số chuẩn TSPL
     */
    public void printBarcode(String data, int x, int y, String codeType, int height, int readable, int rotation, int narrow, int wide) {
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
     * Vẽ QRCode lên buffer máy in (chưa in) - đảm bảo thiết lập đầy đủ trước khi in
     */
    public void printQRCode(String data,
                        int x, int y,
                        String ecLevel,    // L | M | Q | H
                        int cellWidth,     // 1–10
                        String mode,       // A | M
                        int rotation) {    // 0 | 90 | 180 | 270

        /* 1. Bảo đảm khổ giấy & gap (gửi mỗi lần để không phụ thuộc trạng thái cũ) */
        printer.sizeMm(labelWidthMm, labelHeightMm);
        printer.gapMm(2.0, 0.0);
        printer.speed(printSpeed);
        printer.density(printDensity);

        /* 2. Xoá buffer và gửi đúng cú pháp 7 tham số */
        printer.cls();
        printer.qrcode(x, y, ecLevel, cellWidth, mode, rotation, data);  // <-- 7 tham số
        printer.print(1);
    }

    /**
     * Vẽ hình ảnh từ path lên buffer máy in (chưa in) - đảm bảo ảnh hợp lệ
     */
    public void printImageFromPath(String imagePath, int x, int y, int mode, Context context) {
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
    public void printImageBase64(String base64, int x, int y, int mode) {
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

