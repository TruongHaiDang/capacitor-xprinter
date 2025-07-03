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

    // Có thể bổ sung thêm các hàm đặc thù POSPrinter ở đây
} 