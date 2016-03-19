package com.cfi.lookout.loos;

import android.content.Context;
import android.util.Log;

import com.cfi.lookout.request.AbstractApiServerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

public class GetLoosRequest extends AbstractApiServerRequest<GetLoosResponse> {

    private final String TAG = "GetConnectionsRequest";


    public GetLoosRequest(Context context)
    {
        super(context);
    }

    @Override
    public GetLoosResponse execute() throws IOException {
        String path = "/loos";
        String url = constructUrl(path);
        HttpGet request = new HttpGet(url);
        authorizeHttpMethod(request);
        GetLoosResponse resp = processHttpRequest(request);
        return resp;
    }

    @Override
    protected GetLoosResponse parseResponse(HttpResponse httpResponse) throws IOException {
        GetLoosResponse response = new GetLoosResponse();
        if(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED)
        {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String body = getBodyString();
                response = mapper.readValue(body, GetLoosResponse.class);
            }catch(Exception e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
        return  response;

    }
}
