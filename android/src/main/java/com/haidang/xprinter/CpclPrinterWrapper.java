package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.CPCLPrinter;
import net.posprinter.posprinterface.IStatusCallback;
import android.util.Base64;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class CpclPrinterWrapper implements PrinterBase {
    private final CPCLPrinter printer;

    // Hằng số mặc định
    private static final int DEFAULT_WIDTH = 384; // pixel/điểm

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

    /**
     * Hàm tiện ích gửi lệnh in và flush
     */
    private void flush() {
        // Gửi dữ liệu đã xây dựng (lệnh) tới máy in.
        printer.sendData(new byte[0]);
    }

    @Override
    public void printText(String text, int alignment, int textSize, int attribute) {
        // CPCL không hỗ trợ alignment giống POS, chúng ta sẽ dùng addAlign.
        printer.initializePrinter(DEFAULT_WIDTH);
        if (alignment == 1) {
            printer.addAlign("CENTER");
        } else if (alignment == 2) {
            printer.addAlign("RIGHT");
        } else {
            printer.addAlign("LEFT");
        }
        printer.addText(0, 0, text);
        printer.addForm();
        printer.addPrint();
        flush();
    }

    @Override
    public void printQRCode(String data, int moduleSize, int ecLevel, int alignment) {
        printer.initializePrinter(DEFAULT_WIDTH);
        if (alignment == 1) printer.addAlign("CENTER");
        if (alignment == 2) printer.addAlign("RIGHT");
        // x, y = 0,0 mặc định
        printer.addQRCode(0, 0, moduleSize, ecLevel, data);
        printer.addForm();
        printer.addPrint();
        flush();
    }

    @Override
    public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        printer.initializePrinter(DEFAULT_WIDTH);
        if (alignment == 1) printer.addAlign("CENTER");
        if (alignment == 2) printer.addAlign("RIGHT");
        String type = String.valueOf(codeType);
        if (height <= 0) height = 80;
        printer.addBarcode(0, 0, type, height, data);
        printer.addForm();
        printer.addPrint();
        flush();
    }

    @Override
    public void printImageFromPath(String imagePath, int width, int alignment) {
        if (width <= 0) width = DEFAULT_WIDTH;
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(imagePath);
        if (bmp == null) throw new RuntimeException("Không đọc được hình ảnh tại " + imagePath);
        printer.initializePrinter(width);
        if (alignment == 1) printer.addAlign("CENTER");
        if (alignment == 2) printer.addAlign("RIGHT");
        printer.addEGraphics(0, 0, width, bmp);
        printer.addForm();
        printer.addPrint();
        flush();
    }

    @Override
    public void printImageBase64(String base64, int width, int alignment) {
        if (width <= 0) width = DEFAULT_WIDTH;
        byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        printer.initializePrinter(width);
        if (alignment == 1) printer.addAlign("CENTER");
        if (alignment == 2) printer.addAlign("RIGHT");
        printer.addEGraphics(0, 0, width, bmp);
        printer.addForm();
        printer.addPrint();
        flush();
    }

    // Có thể bổ sung thêm các hàm đặc thù CPCLPrinter ở đây
} 