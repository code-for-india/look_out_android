package com.cfi.lookout.response;

public enum ResponseStatusCodes {


    // success
    OK(200, "Ok"),
    CREATED(201,"Created"),
    Accepted(202,"Accepted"),
    REQUEST_BAD(400, "Bad Request"),
    RESOURCE_NOT_FOUND(404, "Resource Not Found"),
    ERR_INTERNAL_SERVER(500, "Internal Server Error."),
    ERR_INTERNAL_DATABASE(502, "MySQL Failure.");



    private final int statusCode;
    private final String statusMsg;

    ResponseStatusCodes(final int statusCode, final String statusMsgIn) {
        this.statusCode = statusCode;
        this.statusMsg = statusMsgIn;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }


}
