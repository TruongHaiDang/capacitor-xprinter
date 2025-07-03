package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;

public class PrinterFactory {
    /**
     * Tạo instance PrinterBase phù hợp với ngôn ngữ máy in
     * @param language 'POS' | 'CPCL' | 'TSPL' | 'ZPL'
     * @param connection IDeviceConnection đã kết nối
     * @return PrinterBase phù hợp
     */
    public static PrinterBase createPrinter(String language, IDeviceConnection connection) {
        switch (language.toUpperCase()) {
            case "POS":
                return new PosPrinterWrapper(connection);
            case "CPCL":
                return new CpclPrinterWrapper(connection);
            case "TSPL":
                return new TsplPrinterWrapper(connection);
            case "ZPL":
                return new ZplPrinterWrapper(connection);
            default:
                throw new IllegalArgumentException("Ngôn ngữ máy in không hỗ trợ: " + language);
        }
    }
} 