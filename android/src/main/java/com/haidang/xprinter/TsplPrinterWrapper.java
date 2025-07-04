package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.TSPLPrinter;
import net.posprinter.TSPLConst;
import net.posprinter.model.AlgorithmType;
import net.posprinter.posprinterface.IStatusCallback;

public class TsplPrinterWrapper implements PrinterBase {
    private final TSPLPrinter printer;

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

    @Override
    public void printQRCode(String data, int module, int ecLevel, int alignment) {
        printer.sizeMm(60.0, 30.0)
                .cls()
                .qrcode(20, 20, TSPLConst.EC_LEVEL_M, module, TSPLConst.QRCODE_MODE_MANUAL, TSPLConst.ROTATION_0, data)
                .print();
    }

    @Override
    public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        printer.sizeMm(60.0, 30.0)
                .cls()
                .barcode(20, 20, String.valueOf(codeType), height, data)
                .print();
    }

    @Override
    public void printImage(String path, int width, int alignment) {
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(path);
        printer.sizeMm(76.0, 30.0)
                .cls()
                .bitmap(0, 0, TSPLConst.BMP_MODE_OVERWRITE, width, bmp, AlgorithmType.Threshold)
                .print();
    }

    @Override
    public void printImageBase64(String base64, int width, int alignment) {
        byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        printer.sizeMm(76.0, 30.0)
                .cls()
                .bitmap(0, 0, TSPLConst.BMP_MODE_OVERWRITE, width, bmp, AlgorithmType.Threshold)
                .print();
    }

    // Có thể bổ sung thêm các hàm đặc thù TSPLPrinter ở đây
} 