package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.POSPrinter;
import net.posprinter.posprinterface.IStatusCallback;

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
    public void printQRCode(String data, int module, int ecLevel, int alignment) {
        printer.printQRCode(data, module, ecLevel, alignment);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    @Override
    public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        printer.printBarCode(data, codeType, width, height, alignment, textPosition);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    @Override
    public void printImage(String path, int width, int alignment) {
        printer.printBitmap(path, alignment, width);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    @Override
    public void printImageBase64(String base64, int width, int alignment) {
        byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        printer.printBitmap(bmp, alignment, width);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    // Có thể bổ sung thêm các hàm đặc thù POSPrinter ở đây
} 