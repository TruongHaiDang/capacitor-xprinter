package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.ZPLPrinter;
import net.posprinter.posprinterface.IStatusCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.InputStream;

public class ZplPrinterWrapper implements PrinterBase {
    private final ZPLPrinter printer;
    private int labelLength = 800; // default, đơn vị dots
    private int labelWidth = 600;  // default, đơn vị dots

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

    /**
     * In văn bản ZPL
     * @param text Nội dung văn bản
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param font Font
     * @param orientation Hướng (N, R, I, B)
     * @param height Chiều cao font
     * @param width Độ rộng font
     */
    public void printText(String text, Integer x, Integer y, String font, String orientation, Integer height, Integer width) {
        // Giá trị mặc định
        String fontVal = (font != null && !font.isEmpty()) ? font : "A";
        String orientVal = (orientation != null && !orientation.isEmpty()) ? orientation : "N";
        int fontHeight = (height != null) ? height : 30;
        int fontWidth = (width != null) ? width : 30;
        int labelW = getLabelWidthOrDefault();
        int labelH = getLabelLengthOrDefault();
        // Ước lượng chiều rộng text (rất đơn giản, có thể tinh chỉnh)
        int textPixelWidth = text.length() * fontWidth;
        int xVal = (x != null) ? x : Math.max(0, (labelW - textPixelWidth) / 2);
        int yVal = (y != null) ? y : Math.max(0, (labelH - fontHeight) / 2);

        // Xây chuỗi lệnh ZPL thủ công và gửi bằng sendData
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\r\n");
        // Tọa độ
        zpl.append("^FO").append(xVal).append(',').append(yVal).append("\r\n");
        // Font và kích thước
        zpl.append("^A").append(fontVal).append(',').append(orientVal).append(',')
           .append(fontHeight).append(',').append(fontWidth).append("\r\n");
        // Nội dung
        zpl.append("^FD").append(text).append("^FS\r\n");
        zpl.append("^XZ");
        sendCommand(zpl.toString());
    }

    /**
     * In mã QR ZPL
     * @param data Dữ liệu mã QR
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param model Model (1 hoặc 2)
     * @param magnification Độ phóng đại (1-10)
     * @param errorCorrection Mức độ sửa lỗi (H, Q, M, L)
     * @param maskValue Mask value (0-7)
     */
    public void printQRCode(String data, int x, int y, int model, int magnification, String errorCorrection, int maskValue) {
        printer.addQRCode(x, y, magnification, data);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã vạch ZPL
     * @param data Dữ liệu mã vạch
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param codeType Loại mã vạch
     * @param orientation Hướng (N, R, I, B)
     * @param height Chiều cao
     * @param printInterpretationLine In text (Y/N)
     * @param printInterpretationLineAbove Text ở trên (Y/N)
     * @param checkDigit Check digit (Y/N)
     */
    public void printBarcode(String data, int x, int y, String codeType, String orientation, int height, String printInterpretationLine, String printInterpretationLineAbove, String checkDigit) {
        printer.addBarcode(x, y, codeType, data, height);
        printer.sendData(new byte[0]);
    }

    /**
     * In hình ảnh ZPL từ đường dẫn hoặc URI
     * @param imagePath Đường dẫn hình ảnh hoặc content URI
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param width Độ rộng
     * @param height Chiều cao
     * @param context Context Android để đọc URI
     */
    public void printImageFromPath(String imagePath, int x, int y, int width, int height, Context context) {
        Bitmap bmp = null;
        try {
            if (imagePath != null && imagePath.startsWith("content://")) {
                InputStream input = context.getContentResolver().openInputStream(Uri.parse(imagePath));
                bmp = BitmapFactory.decodeStream(input);
            } else {
                bmp = BitmapFactory.decodeFile(imagePath);
            }
        } catch (Exception e) {
            bmp = null;
        }
        if (bmp != null) {
            printer.printBitmap(x, y, bmp, width);
            printer.sendData(new byte[0]);
        } else {
            throw new RuntimeException("Không đọc được file hình ảnh: " + imagePath);
        }
    }

    /**
     * In hình ảnh ZPL từ base64
     * @param base64 Chuỗi base64 của hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param width Độ rộng
     * @param height Chiều cao
     */
    public void printImageBase64(String base64, int x, int y, int width, int height) {
        try {
            byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            printer.printBitmap(x, y, bmp, width);
            printer.sendData(new byte[0]);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã base64 hình ảnh", e);
        }
    }

    /**
     * Bắt đầu format label
     */
    public void startFormat() {
        printer.addStart();
    }

    /**
     * Kết thúc format label
     */
    public void endFormat() {
        printer.addEnd();
        printer.sendData(new byte[0]);
    }

    /**
     * Thiết lập độ dài label
     * @param length Độ dài (dots)
     */
    public void setLabelLength(int length) {
        this.labelLength = length;
        printer.setLabelLength(length);
    }

    /**
     * Thiết lập tốc độ in
     * @param speed Tốc độ (2-14)
     */
    public void setPrintSpeed(int speed) {
        printer.setPrintSpeed(speed);
    }

    /**
     * Gửi lệnh ZPL tùy chỉnh
     * @param command Lệnh ZPL
     */
    public void sendCommand(String command) {
        printer.sendData(command.getBytes());
    }

    public void setLabelWidth(int width) {
        this.labelWidth = width;
        printer.setPrinterWidth(width);
    }

    public int getLabelLengthOrDefault() { return labelLength; }
    public int getLabelWidthOrDefault() { return labelWidth; }
}

