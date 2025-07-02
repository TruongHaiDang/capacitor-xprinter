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
    public void initialize(PluginCall call) {
        boolean success = implementation.initialize();
        JSObject ret = new JSObject();
        if (success) {
            ret.put("code", 0);
            ret.put("msg", "Initialize success");
            ret.put("data", null);
        } else {
            ret.put("code", -1);
            ret.put("msg", "Initialize success");
            ret.put("data", null);
        }
        call.resolve(ret);
    }
}
