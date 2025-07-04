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

    /**
     * In mã QR (QR Code)
     */
    default void printQRCode(String data, int moduleSize, int ecLevel, int alignment) {
        throw new UnsupportedOperationException("Print QRCode không hỗ trợ cho printer này");
    }

    /**
     * In mã vạch 1D (Barcode)
     */
    default void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        throw new UnsupportedOperationException("Print Barcode không hỗ trợ cho printer này");
    }

    /**
     * In hình ảnh từ đường dẫn
     */
    default void printImageFromPath(String imagePath, int width, int alignment) {
        throw new UnsupportedOperationException("Print Image (path) không hỗ trợ cho printer này");
    }

    /**
     * In hình ảnh từ chuỗi base64
     */
    default void printImageBase64(String base64, int width, int alignment) {
        throw new UnsupportedOperationException("Print Image (base64) không hỗ trợ cho printer này");
    }
} 