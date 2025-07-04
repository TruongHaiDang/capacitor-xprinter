package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.TSPLPrinter;
import net.posprinter.posprinterface.IStatusCallback;
import android.util.Base64;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class TsplPrinterWrapper implements PrinterBase {
    private final TSPLPrinter printer;
    private static final int DEFAULT_WIDTH = 384;

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

    private void flush() {
        // Đối với TSPL, gọi print(1) đã gửi dữ liệu
        // Không cần sendData riêng.
    }

    @Override
    public void printText(String text, int alignment, int textSize, int attribute) {
        printer.cls();
        String font = "TSS24.BF2"; // font mặc định
        printer.text(0, 0, font, text);
        printer.print(1);
    }

    @Override
    public void printQRCode(String data, int moduleSize, int ecLevel, int alignment) {
        printer.cls();
        printer.qrcode(0, 0, data);
        printer.print(1);
    }

    @Override
    public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        printer.cls();
        if (height <= 0) height = 80;
        printer.barcode(0, 0, String.valueOf(codeType), height, data);
        printer.print(1);
    }

    @Override
    public void printImageFromPath(String imagePath, int width, int alignment) {
        if (width <= 0) width = DEFAULT_WIDTH;
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(imagePath);
        if (bmp == null) throw new RuntimeException("Không đọc được hình ảnh");
        printer.cls();
        printer.bitmap(0, 0, width, bmp.getHeight(), bmp);
        printer.print(1);
    }

    @Override
    public void printImageBase64(String base64, int width, int alignment) {
        if (width <= 0) width = DEFAULT_WIDTH;
        byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        printer.cls();
        printer.bitmap(0, 0, width, bmp.getHeight(), bmp);
        printer.print(1);
    }

    // Có thể bổ sung thêm các hàm đặc thù TSPLPrinter ở đây
} 