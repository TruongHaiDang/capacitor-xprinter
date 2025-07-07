package com.haidang.xprinter;

import net.posprinter.IDeviceConnection;
import net.posprinter.CPCLPrinter;
import net.posprinter.posprinterface.IStatusCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.InputStream;

public class CpclPrinterWrapper implements PrinterBase {
    private final CPCLPrinter printer;
    private int labelWidth = 576;   // mặc định, đơn vị dots
    private int labelHeight = 320;  // mặc định, đơn vị dots
    private int labelQuantity = 1;

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

    /**
     * In văn bản CPCL
     * @param text Nội dung văn bản
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param font Font (0-7)
     * @param rotation Góc xoay (0, 90, 180, 270)
     */
    public void printText(String text, int x, int y, int font, int rotation) {
        printer.addText(x, y, String.valueOf(rotation), String.valueOf(font), text);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã QR CPCL
     * @param data Dữ liệu mã QR
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param model Model QR (1 hoặc 2)
     * @param unitWidth Độ rộng đơn vị
     */
    public void printQRCode(String data, int x, int y, int model, int unitWidth) {
        printer.addQRCode(x, y, model, unitWidth, data);
        printer.sendData(new byte[0]);
    }

    /**
     * In mã vạch CPCL
     * @param data Dữ liệu mã vạch
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param codeType Loại mã vạch
     * @param height Chiều cao
     * @param readable Hiển thị text (0-không, 1-có)
     */
    public void printBarcode(String data, int x, int y, String codeType, int height, int readable) {
        printer.addBarcode(x, y, codeType, height, data);
        printer.sendData(new byte[0]);
    }

    /**
     * In hình ảnh CPCL từ đường dẫn hoặc URI
     * @param imagePath Đường dẫn hình ảnh hoặc content URI
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param mode Mode in (0-overwrite, 1-or, 2-xor)
     * @param context Context Android để đọc URI
     */
    public void printImageFromPath(String imagePath, int x, int y, int mode, Context context) {
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
            printer.addEGraphics(x, y, bmp.getWidth(), bmp);
            printer.sendData(new byte[0]);
        } else {
            throw new RuntimeException("Không đọc được file hình ảnh: " + imagePath);
        }
    }

    /**
     * In hình ảnh CPCL từ base64
     * @param base64 Chuỗi base64 của hình ảnh
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param mode Mode in (0-overwrite, 1-or, 2-xor)
     */
    public void printImageBase64(String base64, int x, int y, int mode) {
        try {
            byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            printer.addEGraphics(x, y, bmp.getWidth(), bmp);
            printer.sendData(new byte[0]);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã base64 hình ảnh", e);
        }
    }

    /**
     * Thiết lập kích thước label
     * @param width Độ rộng
     * @param height Chiều cao
     * @param quantity Số lượng
     */
    public void setLabel(int width, int height, int quantity) {
        this.labelWidth = width;
        this.labelHeight = height;
        this.labelQuantity = quantity;
        printer.initializePrinter(0, height, quantity);
    }

    public int getLabelWidthOrDefault() { return labelWidth; }
    public int getLabelHeightOrDefault() { return labelHeight; }
    public int getLabelQuantityOrDefault() { return labelQuantity; }

    /**
     * In text CPCL: tự động initialize, addText, form, print, sendData
     * Nếu x/y/font không truyền vào, tự căn giữa
     */
    public void printText(String text, Integer x, Integer y, String font) {
        // 1. Khởi tạo label
        printer.initializePrinter(0, labelHeight, labelQuantity);

        // 2. Tính toán vị trí căn giữa nếu x/y null
        int posX = (x != null) ? x : (labelWidth / 2 - (text.length() * 8)); // 8 là width ước lượng 1 ký tự
        int posY = (y != null) ? y : (labelHeight / 2);

        // 3. Chọn font mặc định nếu không truyền
        String fontName = (font != null) ? font : "0";

        // 4. Thêm text
        printer.addText(posX, posY, fontName, text);

        // 5. Kết thúc label
        printer.addForm();
        printer.addPrint();

        // 6. Gửi lệnh xuống máy in (nếu cần flush)
        printer.sendData(new byte[0]);
    }

    /**
     * In label
     */
    public void printLabel() {
        printer.addPrint();
        printer.sendData(new byte[0]);
    }

    /**
     * Gửi lệnh CPCL tùy chỉnh
     * @param command Lệnh CPCL
     */
    public void sendCommand(String command) {
        printer.sendData(command.getBytes());
    }
} 

