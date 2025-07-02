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

    @PluginMethod
    public void connect(PluginCall call) {
        JSObject options = call.getData();
        android.content.Context context = getContext();

        HandshakeResponse response = implementation.connect(options, context);

        JSObject ret = new JSObject();
        ret.put("code", response.code);
        ret.put("msg", response.msg);
        ret.put("data", response.data);

        call.resolve(ret);
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

}
