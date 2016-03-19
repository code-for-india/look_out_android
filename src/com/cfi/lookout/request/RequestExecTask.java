package com.cfi.lookout.request;

import android.os.AsyncTask;
import android.util.Log;

import com.cfi.lookout.response.ApiResponse;

import java.io.IOException;

public class RequestExecTask<T extends AbstractApiServerRequest,U extends ApiResponse> extends AsyncTask<T, Void, U> {

    private final String TAG = "RequestExecTask";
    private RequestExecCallback<U> mCallback;

    public RequestExecTask(RequestExecCallback<U> callback)
    {
        mCallback = callback;
    }
    protected U doInBackground(T... requests){
        T request = requests[0];
        try {
            U resp = (U)request.execute();
            return resp;
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
            return  null;
        }
    }

    protected void onPreExecute() {
        mCallback.onPreRequestExec();
    }
    protected void onPostExecute(U result) {
        mCallback.onPostRequestExec(result);
    }


}
