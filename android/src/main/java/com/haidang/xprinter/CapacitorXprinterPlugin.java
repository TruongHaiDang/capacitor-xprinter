package com.haidang.xprinter;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CapacitorXprinter")
public class CapacitorXprinterPlugin extends Plugin {

    private CapacitorXprinter implementation = new CapacitorXprinter();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    /**
     * Kết nối tới thiết bị máy in dựa trên loại thiết bị và các tham số truyền vào.
     * Trả về kết quả bất đồng bộ qua IConnectListener.
     */
    @PluginMethod
    public void connect(PluginCall call) {
        JSObject options = call.getData();
        android.content.Context context = getContext();
        implementation.connect(options, context, call);
    }

    /**
     * Ngắt kết nối với thiết bị máy in hiện tại.
     * Gọi implementation.disconnect() và trả về kết quả cho phía JS/TS.
     */
    @PluginMethod
    public void disconnect(PluginCall call) {
        HandshakeResponse response = implementation.disconnect();

        JSObject ret = new JSObject();
        ret.put("code", response.code);
        ret.put("msg", response.msg);
        ret.put("data", response.data);

        call.resolve(ret);
    }

    /**
     * Lấy danh sách cổng/thiết bị khả dụng (USB, BLUETOOTH, SERIAL).
     * Trả về mảng string 'ports'.
     */
    @PluginMethod
    public void listAvailablePorts(PluginCall call) {
        String type = call.getString("type");
        java.util.List<String> ports = implementation.listAvailablePorts(type, getContext());

        com.getcapacitor.JSArray arr = new com.getcapacitor.JSArray(ports);
        JSObject ret = new JSObject();
        ret.put("ports", arr);
        call.resolve(ret);
    }

    /**
     * Trả về các hằng số trạng thái của POSConnect.
     */
    @PluginMethod
    public void getStatusConstants(PluginCall call) {
        JSObject constants = implementation.getStatusConstants();
        call.resolve(constants);
    }

}
