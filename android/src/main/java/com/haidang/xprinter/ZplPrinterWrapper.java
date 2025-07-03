package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.ZPLPrinter;
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

    // Có thể bổ sung thêm các hàm đặc thù ZPLPrinter ở đây
} 