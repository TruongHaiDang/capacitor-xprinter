package com.haidang.xprinter;

import android.util.Log;
import net.posprinter.POSConnect;
import HandshakeResponse;

public class CapacitorXprinter {
    private IDeviceConnection currentDevice = null;
    
    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    /**
     * Kết nối tới thiết bị máy in dựa trên loại thiết bị và các tham số truyền vào.
     * Lưu lại đối tượng thiết bị đã kết nối để phục vụ cho việc ngắt kết nối sau này.
     *
     * @param options  Đối tượng JSObject chứa các tham số cấu hình kết nối (type, name, macAddress, ip, port, serialPort, baudRate, ...)
     * @param context  Context của ứng dụng Android, dùng để khởi tạo hoặc truy cập các tài nguyên hệ thống
     * @return         Đối tượng HandshakeResponse phản hồi trạng thái kết nối (thành công/thất bại và thông điệp tương ứng)
     */
    public HandshakeResponse connect(JSObject options, Context context) {
        // Kiểm tra xem POSConnect đã được khởi tạo với context ứng dụng chưa, nếu chưa thì khởi tạo
        boolean isInitialized = POSConnect.getAppCtx() != null;
        if (!isInitialized) {
            POSConnect.init(context);
        }

        // Lấy loại thiết bị từ options (1: USB, 2: Bluetooth, 3: Ethernet, 4: Serial)
        int deviceType = options.getInteger("type");
        IDeviceConnection device = POSConnect.createDevice(deviceType);

        try {
            // Xử lý kết nối dựa trên loại thiết bị
            switch (deviceType) {
                case 1: // USB
                    // Kết nối qua USB với tên thiết bị
                    String usbName = options.getString("name");
                    device.connect(usbName, context);
                    break;
                case 2: // Bluetooth
                    // Kết nối qua Bluetooth với địa chỉ MAC
                    String mac = options.getString("macAddress");
                    device.connect(mac, context);
                    break;
                case 3: // Ethernet
                    // Kết nối qua Ethernet với IP và port
                    String ip = options.getString("ip");
                    int port = options.getInteger("port");
                    device.connect(ip, port, context);
                    break;
                case 4: // Serial
                    // Kết nối qua Serial với tên cổng và baudrate
                    String serialPort = options.getString("serialPort");
                    int baudRate = options.getInteger("baudRate");
                    device.connect(serialPort, baudRate, context);
                    break;
                default:
                    // Loại thiết bị không hỗ trợ
                    return new HandshakeResponse(400, "Loại thiết bị không hỗ trợ", null);
            }
            // Lưu lại thiết bị đã kết nối để sử dụng cho các thao tác sau này (ví dụ: disconnect)
            this.currentDevice = device;
            // Kết nối thành công
            return new HandshakeResponse(200, "Kết nối thành công", null);
        } catch (Exception e) {
            // Xử lý lỗi khi kết nối thất bại
            return new HandshakeResponse(500, "Kết nối thất bại: " + e.getMessage(), null);
        }
    }

    /**
     * Ngắt kết nối với thiết bị máy in hiện tại nếu đang kết nối.
     * Sử dụng phương thức close() của IDeviceConnection để đóng kết nối.
     *
     * @return Đối tượng HandshakeResponse phản hồi trạng thái ngắt kết nối (thành công/thất bại và thông điệp tương ứng)
     */
    public HandshakeResponse disconnect() {
        // Kiểm tra xem có thiết bị nào đang kết nối không
        if (currentDevice != null) {
            try {
                // Đóng kết nối với thiết bị
                currentDevice.close();
                currentDevice = null;
                // Ngắt kết nối thành công
                return new HandshakeResponse(200, "Ngắt kết nối thành công", null);
            } catch (Exception e) {
                // Xử lý lỗi khi ngắt kết nối thất bại
                return new HandshakeResponse(500, "Ngắt kết nối thất bại: " + e.getMessage(), null);
            }
        } else {
            // Không có thiết bị nào đang kết nối
            return new HandshakeResponse(400, "Không có thiết bị nào đang kết nối", null);
        }
    }

}
