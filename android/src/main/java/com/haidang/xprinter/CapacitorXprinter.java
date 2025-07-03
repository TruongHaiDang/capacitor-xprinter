package com.haidang.xprinter;

import android.util.Log;
import net.posprinter.POSConnect;
import HandshakeResponse;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.Set;
import java.util.Collections;
import java.util.List;
import android.content.Context;
import net.posprinter.IConnectListener;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

public class CapacitorXprinter {
    private IDeviceConnection currentDevice = null;
    
    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    /**
     * Kết nối tới thiết bị máy in dựa trên loại thiết bị và các tham số truyền vào.
     * Sử dụng IConnectListener để nhận trạng thái kết nối bất đồng bộ.
     *
     * @param options  Đối tượng JSObject chứa các tham số cấu hình kết nối (type, name, macAddress, ip, port, serialPort, baudRate, ...)
     * @param context  Context của ứng dụng Android, dùng để khởi tạo hoặc truy cập các tài nguyên hệ thống
     * @param call     Đối tượng PluginCall để trả về kết quả cho phía JS/TS
     */
    public void connect(JSObject options, Context context, PluginCall call) {
        // Kiểm tra xem POSConnect đã được khởi tạo với context ứng dụng chưa, nếu chưa thì khởi tạo
        boolean isInitialized = POSConnect.getAppCtx() != null;
        if (!isInitialized) {
            POSConnect.init(context);
        }

        // Lấy loại thiết bị từ options (1: USB, 2: Bluetooth, 3: Ethernet, 4: Serial)
        int deviceType = options.getInteger("type");
        IDeviceConnection device = POSConnect.createDevice(deviceType);

        // Tạo listener để nhận trạng thái kết nối bất đồng bộ
        IConnectListener listener = new IConnectListener() {
            @Override
            public void onStatus(int status, String info, String msg) {
                com.getcapacitor.JSObject result = new com.getcapacitor.JSObject();
                result.put("code", status);
                result.put("msg", msg);
                result.put("data", info);
                if (status == POSConnect.CONNECT_SUCCESS) {
                    currentDevice = device;
                    call.resolve(result);
                } else {
                    call.reject(msg, null, result);
                }
            }
        };

        try {
            // Xử lý kết nối dựa trên loại thiết bị
            switch (deviceType) {
                case POSConnect.DEVICE_TYPE_USB:
                    // Kết nối qua USB với tên thiết bị
                    String usbName = options.getString("name");
                    device.connect(usbName, listener);
                    break;
                case POSConnect.DEVICE_TYPE_BLUETOOTH:
                    // Kết nối qua Bluetooth với địa chỉ MAC
                    String mac = options.getString("macAddress");
                    device.connect(mac, listener);
                    break;
                case POSConnect.DEVICE_TYPE_ETHERNET:
                    // Kết nối qua Ethernet với IP và port
                    String ip = options.getString("ip");
                    int port = options.getInteger("port");
                    device.connect(ip, port, listener);
                    break;
                case POSConnect.DEVICE_TYPE_SERIAL:
                    // Kết nối qua Serial với tên cổng và baudrate
                    String serialPort = options.getString("serialPort");
                    int baudRate = options.getInteger("baudRate");
                    device.connect(serialPort, baudRate, listener);
                    break;
                default:
                    // Loại thiết bị không hỗ trợ
                    com.getcapacitor.JSObject result = new com.getcapacitor.JSObject();
                    result.put("code", 400);
                    result.put("msg", "Loại thiết bị không hỗ trợ");
                    result.put("data", null);
                    call.reject("Loại thiết bị không hỗ trợ", null, result);
            }
        } catch (Exception e) {
            // Xử lý lỗi khi kết nối thất bại (lỗi ngoại lệ)
            com.getcapacitor.JSObject result = new com.getcapacitor.JSObject();
            result.put("code", 500);
            result.put("msg", "Kết nối thất bại: " + e.getMessage());
            result.put("data", null);
            call.reject("Kết nối thất bại: " + e.getMessage(), null, result);
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
                // Dọn dẹp tài nguyên
                POSConnect.exit();
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

    /**
     * Lấy danh sách cổng/thiết bị khả dụng tuỳ theo type.
     * Hỗ trợ USB, BLUETOOTH, SERIAL. Nếu type không hợp lệ trả về danh sách rỗng.
     *
     * @param type    Chuỗi 'USB' | 'BLUETOOTH' | 'SERIAL'
     * @param context Context ứng dụng để truy cập hệ thống (cần cho USB)
     * @return        Danh sách tên cổng/thiết bị
     */
    public List<String> listAvailablePorts(String type, Context context) {
        if (type == null) return Collections.emptyList();

        switch (type.toUpperCase()) {
            case "USB":
                return POSConnect.getUsbDevices(context);
            case "SERIAL":
                return POSConnect.getSerialPort();
            case "BLUETOOTH":
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) return Collections.emptyList();
                Set<BluetoothDevice> bonded = adapter.getBondedDevices();
                List<String> btDevices = new java.util.ArrayList<>();
                for (BluetoothDevice dev : bonded) {
                    btDevices.add(dev.getName());
                }
                return btDevices;
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Expose các hằng số trạng thái của POSConnect cho phía JS/TS.
     * @return JSObject chứa mapping { CONST_NAME: value }
     */
    public com.getcapacitor.JSObject getStatusConstants() {
        com.getcapacitor.JSObject constants = new com.getcapacitor.JSObject();
        constants.put("CONNECT_SUCCESS", POSConnect.CONNECT_SUCCESS);
        constants.put("CONNECT_FAIL", POSConnect.CONNECT_FAIL);
        constants.put("SEND_FAIL", POSConnect.SEND_FAIL);
        constants.put("CONNECT_INTERRUPT", POSConnect.CONNECT_INTERRUPT);
        constants.put("USB_ATTACHED", POSConnect.USB_ATTACHED);
        constants.put("USB_DETACHED", POSConnect.USB_DETACHED);
        constants.put("BLUETOOTH_INTERRUPT", POSConnect.BLUETOOTH_INTERRUPT);
        return constants;
    }

}
