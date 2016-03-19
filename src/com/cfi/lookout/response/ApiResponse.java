package com.cfi.lookout.response;

import org.apache.http.HttpStatus;

public class ApiResponse implements IApiResponse {

    private int httpCode = -1;
    private String httpStatusText = null;

    public ApiResponse() {}

    public boolean isOk() {
        return this.httpCode == HttpStatus.SC_OK;
    }

    public boolean isCreated() {
        return this.httpCode == HttpStatus.SC_CREATED;
    }

    public boolean isSuccess() {
        return (200 <= this.httpCode && this.httpCode < 300);
    }


    @Override
    public void setHttpStatusCode(int code) {
        this.httpCode = code;
    }

    @Override
    public int getHttpStatusCode() {
        return this.httpCode;
    }

    @Override
    public String getHttpStatusText() {
        return httpStatusText;
    }

    @Override
    public void setHttpStatusText(String httpStatusText) {
        this.httpStatusText = httpStatusText;
    }
}
