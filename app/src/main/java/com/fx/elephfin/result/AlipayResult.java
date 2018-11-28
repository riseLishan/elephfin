package com.fx.elephfin.result;

/**
 * 接收从服务端传回的支付加签数据
 * Created by 莫仁周 on 2017/2/16 0016.
 */

public class AlipayResult {
    private String message;
    private boolean success;
    private String  data; //
    private Boolean has_next;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getHas_next() {
        return has_next;
    }

    public void setHas_next(Boolean has_next) {
        this.has_next = has_next;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
