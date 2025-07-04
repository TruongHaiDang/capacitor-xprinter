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
     * In mã QR với dữ liệu cho trước.
     * @param data      Chuỗi dữ liệu QR
     * @param module    Kích thước module
     * @param ecLevel   Mức độ sửa lỗi
     * @param alignment Canh lề 0-left,1-center,2-right
     */
    default void printQRCode(String data, int module, int ecLevel, int alignment) {
        throw new UnsupportedOperationException("Print QRCode không hỗ trợ cho printer này");
    }

    /**
     * In mã vạch 1D.
     * @param data         Nội dung mã vạch
     * @param codeType     Loại mã vạch theo SDK
     * @param width        Độ rộng mã vạch
     * @param height       Chiều cao
     * @param alignment    Canh lề
     * @param textPosition Vị trí hiển thị text
     */
    default void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
        throw new UnsupportedOperationException("Print barcode không hỗ trợ cho printer này");
    }

    /**
     * In hình ảnh từ đường dẫn
     */
    default void printImage(String path, int width, int alignment) {
        throw new UnsupportedOperationException("Print image không hỗ trợ cho printer này");
    }

    /**
     * In hình ảnh từ chuỗi base64
     */
    default void printImageBase64(String base64, int width, int alignment) {
        throw new UnsupportedOperationException("Print image base64 không hỗ trợ cho printer này");
    }
}
