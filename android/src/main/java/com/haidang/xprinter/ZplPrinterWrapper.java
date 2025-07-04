package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.ZPLPrinter;
import net.posprinter.posprinterface.IStatusCallback;
import android.util.Base64;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class ZplPrinterWrapper implements PrinterBase {
    private final ZPLPrinter printer;
    private static final int DEFAULT_WIDTH = 384;

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

    private void flush() {
        // Thực hiện sendData để gửi lệnh
        printer.sendData(new byte[0]);
    }

    @Override
    public void printText(String text, int alignment, int textSize, int attribute) {
        printer.addStart();
        printer.addText(0, 0, text);
        printer.addEnd();
        flush();
    }

    @Override
    public void printQRCode(String data, int moduleSize, int ecLevel, int alignment) {
        printer.addStart();
        printer.addQRCode(0, 0, moduleSize, data);
        printer.addEnd();
        flush();
    }

    @Override
    public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        printer.addStart();
        printer.addBarcode(0, 0, String.valueOf(codeType), data);
        printer.addEnd();
        flush();
    }

    @Override
    public void printImageFromPath(String imagePath, int width, int alignment) {
        if (width <= 0) width = DEFAULT_WIDTH;
        printer.addStart();
        printer.addBitmap(0, 0, imagePath);
        printer.addEnd();
        flush();
    }

    @Override
    public void printImageBase64(String base64, int width, int alignment) {
        if (width <= 0) width = DEFAULT_WIDTH;
        byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        printer.addStart();
        printer.printBitmap(0, 0, bmp, width);
        printer.addEnd();
        flush();
    }

    // Có thể bổ sung thêm các hàm đặc thù ZPLPrinter ở đây
} 