package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.ZPLPrinter;
import net.posprinter.ZPLConst;
import net.posprinter.model.AlgorithmType;
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

    @Override
    public void printQRCode(String data, int module, int ecLevel, int alignment) {
        printer.addStart()
                .addQRCode(30, 30, module, data)
                .addEnd();
    }

    @Override
    public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        printer.addStart()
                .addBarcode(20, 20, String.valueOf(codeType), data)
                .addEnd();
    }

    @Override
    public void printImage(String path, int width, int alignment) {
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(path);
        printer.addStart()
                .printBitmap(0, 0, bmp, width, AlgorithmType.Threshold)
                .addEnd();
    }

    @Override
    public void printImageBase64(String base64, int width, int alignment) {
        byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        printer.addStart()
                .printBitmap(0, 0, bmp, width, AlgorithmType.Threshold)
                .addEnd();
    }

    // Có thể bổ sung thêm các hàm đặc thù ZPLPrinter ở đây
} 