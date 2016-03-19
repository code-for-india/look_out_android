package com.cfi.lookout.request;


import com.cfi.lookout.response.ApiResponse;

public abstract class RequestExecCallback<U extends ApiResponse> {

    public abstract void onPostRequestExec(U result);

    public void onPreRequestExec()
    {

    }
}
