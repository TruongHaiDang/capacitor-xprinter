package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.CPCLPrinter;
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

    // Có thể bổ sung thêm các hàm đặc thù CPCLPrinter ở đây
} 