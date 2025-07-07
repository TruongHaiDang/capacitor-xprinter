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

    /**
     * In mã QR
     * @param data Dữ liệu mã QR
     * @param moduleSize Kích thước module (1-16)
     * @param ecLevel Mức độ sửa lỗi (0-3)
     * @param alignment Căn lề (0-left, 1-center, 2-right)
     */
    public void printQRCode(String data, int moduleSize, int ecLevel, int alignment) {
        printer.printQRCode(data, moduleSize, ecLevel, alignment);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    // /**
    //  * In mã vạch 1D
    //  * @param data Dữ liệu mã vạch
    //  * @param codeType Loại mã vạch
    //  * @param width Độ rộng
    //  * @param height Chiều cao
    //  * @param alignment Căn lề
    //  * @param textPosition Vị trí text (0-không in, 1-trên, 2-dưới, 3-cả hai)
    //  */
    // public void printBarcode(String data, int codeType, int width, int height, int alignment, int textPosition) {
    //     printer.printBarCode(data, codeType, width, height, alignment, textPosition);
    //     printer.feedLine();
    //     printer.sendData(new byte[0]);
    // }

        /**
     * In mã vạch 1-D bằng lệnh ESC/POS thuần, bỏ qua hàm lỗi trong SDK.
     *
     * @param data         Chuỗi dữ liệu mã vạch (String)
     * @param codeType     Loại mã vạch (int, dùng hằng trong POSConst)
     * @param width        Độ rộng vạch  (int, 2-6 theo ESC/POS)
     * @param height       Chiều cao vạch (int, 1-255 dots)
     * @param alignment    Căn lề        (int: 0-left,1-center,2-right)
     * @param textPosition Vị trí HRI    (int: 0-none,1-above,2-below,3-both)
     */
    public void printBarcode(
            final String data,
            final int codeType,
            final int width,
            final int height,
            final int alignment,
            final int textPosition) {

        /*--- Bước 1: Chuẩn hóa tham số theo giới hạn ESC/POS ---*/
        int w  = Math.max(2, Math.min(width, 6));      // module width 2-6
        int h  = Math.max(24, Math.min(height, 255));  // height ≥24 để dễ đọc
        int tp = Math.max(0, Math.min(textPosition, 3));
        int al = Math.max(0, Math.min(alignment, 2));

        /*--- Bước 2: Xây chuỗi lệnh ESC/POS từng phần ---*/
        java.util.List<byte[]> cmds = new java.util.ArrayList<>();

        cmds.add(new byte[]{0x1B, 0x61, (byte) al});          // ESC a n  – căn lề
        cmds.add(new byte[]{0x1D, 0x48, (byte) tp});          // GS H n   – vị trí HRI
        cmds.add(new byte[]{0x1D, 0x77, (byte) w});           // GS w n   – độ rộng vạch
        cmds.add(new byte[]{0x1D, 0x68, (byte) h});           // GS h n   – chiều cao
                                                             // GS k m … – dữ liệu barcode
        byte[] payload = data.getBytes(java.nio.charset.Charset.forName("GBK"));
        byte[] header  = new byte[]{0x1D, 0x6B, (byte) codeType, (byte) payload.length};
        byte[] body    = new byte[header.length + payload.length + 1]; // +1 kết thúc NUL
        System.arraycopy(header, 0, body, 0, header.length);
        System.arraycopy(payload, 0, body, header.length, payload.length);
        body[body.length - 1] = 0x00;                        // kết thúc NUL
        cmds.add(body);

        cmds.add(new byte[]{0x0A});                          // LF xuống dòng

        /*--- Bước 3: Gửi toàn bộ lệnh tới máy in ---*/
        printer.sendData(cmds);
    }

    /**
     * In hình ảnh từ đường dẫn (hỗ trợ cả "file://", "content://" và data URI).
     * Nếu SDK không tự giải mã được chuỗi đường dẫn (ví dụ có scheme hoặc là Content-Uri)
     * thì chúng ta chủ động nạp Bitmap rồi truyền sang hàm printBitmap(Bitmap, ...).
     *
     * @param imagePath  Đường dẫn / uri / data-uri của hình ảnh
     * @param width      Độ rộng mong muốn (0 = auto, đơn vị pixel của máy in)
     * @param alignment  Căn lề (0-left, 1-center, 2-right)
     * @param context    Context – dùng để lấy ContentResolver khi hình ảnh là content://
     */
    public void printImageFromPath(String imagePath, int width, int alignment, Context context) {
        Bitmap bmp = loadBitmapFromPath(imagePath, context);
        if (bmp == null) {
            throw new RuntimeException("Không thể đọc hình ảnh từ đường dẫn: " + imagePath);
        }

        printer.printBitmap(bmp, alignment, width);
        printer.feedLine();
        printer.sendData(new byte[0]);
    }

    /**
     * Chuyển một đường dẫn Uri / file path / data-uri thành Bitmap.
     */
    private Bitmap loadBitmapFromPath(String path, Context context) {
        if (path == null) {
            Log.e("XprinterPlugin", "loadBitmapFromPath: path=null");
            return null;
        }

        try {
            Log.e("XprinterPlugin", "loadBitmapFromPath: path=" + path);

            // Trường hợp data URI:  data:image/png;base64,...
            if (path.startsWith("data:image")) {
                Log.e("XprinterPlugin", "→ Đây là base64 image");
                String base64Part = path.substring(path.indexOf(',') + 1);
                byte[] decoded = Base64.decode(base64Part, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            }

            Uri uri = Uri.parse(path);
            Log.e("XprinterPlugin", "→ URI scheme: " + uri.getScheme());

            // Trường hợp content://
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                Log.e("XprinterPlugin", "→ Đây là content:// URI");
                try (java.io.InputStream is = context.getContentResolver().openInputStream(uri)) {
                    if (is == null) {
                        Log.e("XprinterPlugin", "→ InputStream từ ContentResolver null");
                        return null;
                    }
                    return BitmapFactory.decodeStream(is);
                }
            }

            // Trường hợp file:// hoặc path thuần
            String realPath;
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                realPath = uri.getPath();
                Log.e("XprinterPlugin", "→ Đây là file:// path, realPath=" + realPath);
            } else {
                realPath = path;
                Log.e("XprinterPlugin", "→ Đây là path thuần: " + realPath);
            }

            java.io.File f = new java.io.File(realPath);
            if (!f.exists()) {
                Log.e("XprinterPlugin", "⚠️ File KHÔNG tồn tại: " + realPath);
            } else {
                Log.e("XprinterPlugin", "✅ File tồn tại: " + realPath);
            }

            Bitmap bmp = BitmapFactory.decodeFile(realPath);
            if (bmp == null) {
                Log.e("XprinterPlugin", "⚠️ decodeFile trả về null");
            } else {
                Log.e("XprinterPlugin", "✅ decodeFile OK, size = " + bmp.getWidth() + "x" + bmp.getHeight());
            }

            return bmp;

        } catch (Exception e) {
            Log.e("XprinterPlugin", "❌ loadBitmapFromPath: exception=" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * In hình ảnh từ base64
     * @param base64 Chuỗi base64 của hình ảnh
     * @param width Độ rộng (0 = auto)
     * @param alignment Căn lề
     */
    public void printImageBase64(String base64, int width, int alignment) {
        // Chuyển base64 sang Bitmap rồi in
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

    /**
     * Cắt giấy
     */
    public void cutPaper() {
        printer.cutPaper();
        printer.sendData(new byte[0]);
    }

    /**
     * Mở két tiền
     * @param pinNum Số pin (0 hoặc 1)
     * @param onTime Thời gian bật (ms)
     * @param offTime Thời gian tắt (ms)
     */
    public void openCashDrawer(int pinNum, int onTime, int offTime) {
        printer.openCashBox(pinNum, onTime, offTime);
        printer.sendData(new byte[0]);
    }

    /**
     * Reset máy in
     */
    public void resetPrinter() {
        printer.initializePrinter();
        printer.sendData(new byte[0]);
    }

    /**
     * Self test
     */
    // Không có hàm selfTest trong POSPrinter, có thể bỏ hoặc implement nếu cần
} 

