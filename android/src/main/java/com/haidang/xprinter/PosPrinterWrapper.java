package com.haidang.xprinter;

import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import net.posprinter.IDeviceConnection;
import net.posprinter.POSPrinter;
import net.posprinter.posprinterface.IStatusCallback;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
    public void printText(String text, int alignment, int attribute, int textSize) {
        // Gọi đúng method có đủ 4 tham số
        Log.d("printText", "text: " + text + ", alignment: " + alignment + ", attribute: " + attribute + ", textSize: " + textSize);
        // printer.printText(text, alignment, attribute, textSize);
        printer.printTextAlignment(text, alignment);

        // Nếu cần xuống dòng thì gọi thêm
        printer.feedLine();         // Xuống dòng sau khi in
        printer.sendData(new byte[0]); // Gửi lệnh in
    }

    public void printQRCode(String data, int moduleSize, int ecLevel, int alignment) {
        printer.setAlignment(alignment);
        printer.printQRCode(data, moduleSize, ecLevel);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    public void printBarcode(
            final String data,
            final int codeType,
            final int width,
            final int height,
            final int alignment,
            final int textPosition) {

        int w  = Math.max(2, Math.min(width, 6));
        int h  = Math.max(24, Math.min(height, 255));
        int tp = Math.max(0, Math.min(textPosition, 3));
        int al = Math.max(0, Math.min(alignment, 2));

        List<byte[]> cmds = new ArrayList<>();
        cmds.add(new byte[]{0x1B, 0x61, (byte) al});         // ESC a n (căn lề)
        cmds.add(new byte[]{0x1D, 0x48, (byte) tp});         // GS H n (HRI position)
        cmds.add(new byte[]{0x1D, 0x77, (byte) w});          // GS w n (width)
        cmds.add(new byte[]{0x1D, 0x68, (byte) h});          // GS h n (height)

        byte[] payload = data.getBytes(Charset.forName("GBK"));
        byte[] header  = new byte[]{0x1D, 0x6B, (byte) codeType, (byte) payload.length};
        byte[] body    = new byte[header.length + payload.length + 1];
        System.arraycopy(header, 0, body, 0, header.length);
        System.arraycopy(payload, 0, body, header.length, payload.length);
        body[body.length - 1] = 0x00; // NUL terminator

        cmds.add(body);
        cmds.add(new byte[]{0x0A}); // LF

        printer.sendData(cmds);
    }

    public void printImageFromPath(String imagePath, int width, int alignment, Context context) {
        Bitmap bmp = loadBitmapFromPath(imagePath, context);
        if (bmp == null) throw new RuntimeException("Không thể đọc hình ảnh: " + imagePath);
        printer.printBitmap(bmp, alignment, width);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    private Bitmap loadBitmapFromPath(String path, Context context) {
        if (path == null) return null;
        try {
            if (path.startsWith("data:image")) {
                String base64Part = path.substring(path.indexOf(',') + 1);
                byte[] decoded = Base64.decode(base64Part, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            }

            Uri uri = Uri.parse(path);
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                    return is != null ? BitmapFactory.decodeStream(is) : null;
                }
            }

            String realPath = "file".equalsIgnoreCase(uri.getScheme()) ? uri.getPath() : path;
            return BitmapFactory.decodeFile(realPath);

        } catch (Exception e) {
            Log.e("XprinterPlugin", "loadBitmapFromPath failed: " + e.getMessage());
            return null;
        }
    }

    public void printImageBase64(String base64, int width, int alignment) {
        try {
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            printer.printBitmap(bmp, alignment, width);
            printer.feedLine();
            printer.sendData(new byte[0]);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã base64 hình ảnh", e);
        }
    }

    public void cutPaper() {
        printer.cutPaper();
        printer.sendData(new byte[0]);
    }

    public void openCashDrawer(int pinNum, int onTime, int offTime) {
        printer.openCashBox(pinNum, onTime, offTime);
        printer.sendData(new byte[0]);
    }

    public void resetPrinter() {
        printer.initializePrinter();
        printer.sendData(new byte[0]);
    }

    // === Cấu hình text nâng cao ===
    public void setAlignment(int alignment) {
        printer.setAlignment(alignment);
    }

    public void setTextStyle(int attribute, int textSize) {
        printer.setTextStyle(attribute, textSize);
    }

    public void selectCharacterFont(int font) {
        printer.selectCharacterFont(font);
    }

    public void setLineSpacing(int spacing) {
        printer.setLineSpacing(spacing);
    }

    public void selectCodePage(int codePage) {
        printer.selectCodePage(codePage);
    }

    public void setCharRightSpace(byte space) {
        printer.setCharRightSpace(space);
    }

    public void setTurnUpsideDownMode(boolean mode) {
        printer.setTurnUpsideDownMode(mode);
    }
}
