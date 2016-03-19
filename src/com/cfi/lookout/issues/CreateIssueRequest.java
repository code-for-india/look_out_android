package com.cfi.lookout.issues;

import android.content.Context;
import android.util.Log;

import com.cfi.lookout.request.AbstractApiServerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.IOException;

public class CreateIssueRequest extends AbstractApiServerRequest<IssueResponse> {

    private final String TAG = "CreateIssueRequest";
    private Issue mIssue = null;

    public CreateIssueRequest( Context context, Issue issue)
    {
        super(context);
        mIssue = issue;
    }

    public IssueResponse execute() throws IOException
    {
        String path ="/issues";
        String url = constructUrl(path);
        ObjectMapper mapper = new ObjectMapper();
        String postBody = mapper.writeValueAsString(mIssue);
        HttpPut request = new HttpPut(url);
        authorizeHttpMethod(request);
        ByteArrayEntity entity = new ByteArrayEntity(postBody.getBytes("UTF8"));
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        request.setEntity(entity);
        IssueResponse resp = processHttpRequest(request);
        return resp;

    }


    protected IssueResponse parseResponse(HttpResponse httpResponse) throws IOException
    {
        IssueResponse response = new IssueResponse();
        if(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED)
        {
            try {
            ObjectMapper mapper = new ObjectMapper();
            String body = getBodyString();
            response = mapper.readValue(body, IssueResponse.class);
            }catch(Exception e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
        return  response;
    }

}
