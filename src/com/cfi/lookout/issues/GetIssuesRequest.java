package com.cfi.lookout.issues;

import android.content.Context;
import android.util.Log;

import com.cfi.lookout.request.AbstractApiServerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

public class GetIssuesRequest extends AbstractApiServerRequest<GetIssuesResponse> {

    private final String TAG = "GetConnectionsRequest";


    public GetIssuesRequest(Context context)
    {
        super(context);
    }

    @Override
    public GetIssuesResponse execute() throws IOException {
        String path = "/issues";
        String url = constructUrl(path);
        HttpGet request = new HttpGet(url);
        authorizeHttpMethod(request);
        GetIssuesResponse resp = processHttpRequest(request);
        return resp;
    }

    @Override
    protected GetIssuesResponse parseResponse(HttpResponse httpResponse) throws IOException {
        GetIssuesResponse response = new GetIssuesResponse();
        if(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED)
        {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String body = getBodyString();
                response = mapper.readValue(body, GetIssuesResponse.class);
            }catch(Exception e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
        return  response;

    }
}
