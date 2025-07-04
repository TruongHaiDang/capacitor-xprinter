package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.CPCLPrinter;
import net.posprinter.CPCLConst;
import net.posprinter.posprinterface.IStatusCallback;

public class CpclPrinterWrapper implements PrinterBase {
    private final CPCLPrinter printer;

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

    @Override
    public void printQRCode(String data, int module, int ecLevel, int alignment) {
        printer.initializePrinter(400)
                .addQRCode(20, 20, module, ecLevel, data)
                .addPrint();
    }

    @Override
    public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        printer.initializePrinter(400)
                .addBarcode(20, 20, codeType, height, data)
                .addPrint();
    }

    @Override
    public void printImage(String path, int width, int alignment) {
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(path);
        printer.initializePrinter(400)
                .addEGraphics(20, 20, width, bmp)
                .addPrint();
    }

    @Override
    public void printImageBase64(String base64, int width, int alignment) {
        byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        printer.initializePrinter(400)
                .addEGraphics(20, 20, width, bmp)
                .addPrint();
    }

    // Có thể bổ sung thêm các hàm đặc thù CPCLPrinter ở đây
} 