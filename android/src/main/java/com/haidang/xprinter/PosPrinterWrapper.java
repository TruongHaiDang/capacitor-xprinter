package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.POSPrinter;
import net.posprinter.posprinterface.IStatusCallback;
import android.util.Base64;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class PosPrinterWrapper implements PrinterBase {
    private final POSPrinter printer;

    public PosPrinterWrapper(IDeviceConnection connection) {
        this.printer = new POSPrinter(connection);
    }

    @Override
    public void sendData(byte[] data) {
        printer.sendData(data);
    }

    @Override
    public void printerStatus(IStatusCallback callback) {
        printer.printerStatus(callback);
    }

    @Override
    public void printText(String text, int alignment, int textSize, int attribute) {
        // alignment: 0-left,1-center,2-right; textSize: 0-3 (theo SDK); attribute: bold/underline etc.
        printer.printText(text, alignment, textSize, attribute);
        printer.feedLine(); // xuống dòng để in
        printer.sendData(new byte[0]); // gửi dữ liệu
    }

    @Override
    public void printQRCode(String data, int moduleSize, int ecLevel, int alignment) {
        if (moduleSize <= 0) moduleSize = 8; // default
        if (ecLevel < 0 || ecLevel > 3) ecLevel = 3;
        printer.printQRCode(data, moduleSize, ecLevel, alignment);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    @Override
    public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        if (width <= 0) width = 2;
        if (height <= 0) height = 80;
        if (textPosition < 0) textPosition = 2; // below barcode
        printer.printBarCode(data, codeType, width, height, alignment, textPosition);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    @Override
    public void printImageFromPath(String imagePath, int width, int alignment) {
        if (width <= 0) width = 384; // default print width pixel
        printer.printBitmap(imagePath, width, alignment);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    @Override
    public void printImageBase64(String base64, int width, int alignment) {
        if (width <= 0) width = 384;
        try {
            byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            printer.printBitmap(bmp, width, alignment);
            printer.feedLine();
            printer.sendData(new byte[0]);
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi giải mã base64 hình ảnh: " + ex.getMessage());
        }
    }

    // Có thể bổ sung thêm các hàm đặc thù POSPrinter ở đây
} 