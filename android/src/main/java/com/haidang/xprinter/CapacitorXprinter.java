package com.haidang.xprinter;

import android.util.Log;
import net.posprinter.POSConnect;

public class CapacitorXprinter {
    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    /**
     * Khởi động thư viện.
     * @return true nếu khởi động thành công, false nếu đã khởi động trước đó.
     */
    public boolean initialize() {
        if (POSConnect.initialized) {
            Log.i("Xprinter", "Already initialized.");
            return POSConnect.initialized;
        };

        Log.i("Xprinter", "Initializing Xprinter SDK...");
        POSConnect.initialize()
        initialized = true;

        return POSConnect.;
    }
}
