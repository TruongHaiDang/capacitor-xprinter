package com.haidang.xprinter;

public class HandshakeResponse {
    public int code;
    public String msg;
    public Object data;

    public HandshakeResponse(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
