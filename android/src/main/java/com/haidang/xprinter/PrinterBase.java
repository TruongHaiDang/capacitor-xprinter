package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.posprinterface.IStatusCallback;

public interface PrinterBase {
    /**
     * Gửi dữ liệu thô tới máy in
     * @param data Dữ liệu dạng byte
     */
    void sendData(byte[] data);

    /**
     * Kiểm tra trạng thái máy in
     * @param callback Callback nhận trạng thái
     */
    void printerStatus(IStatusCallback callback);

    /**
     * In văn bản (chỉ POSPrinter hỗ trợ). Các wrapper khác có thể ném UnsupportedOperationException.
     */
    default void printText(String text, int alignment, int textSize, int attribute) {
        throw new UnsupportedOperationException("Print text không hỗ trợ cho printer này");
    }
} 