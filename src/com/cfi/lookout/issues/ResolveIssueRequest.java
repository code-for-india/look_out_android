package com.cfi.lookout.issues;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.cfi.lookout.request.AbstractApiServerRequest;

import android.content.Context;

public class ResolveIssueRequest  extends AbstractApiServerRequest<ResolveIssueResponse>{
	  private final String TAG = "ResolveIssueRequest";
	  String mIssueType;
	  String mLooId;

	    public ResolveIssueRequest(Context context, String looId, String issueType)
	    {
	        super(context);
	        mIssueType = issueType;
	        mLooId = looId;
	    }

	    @Override
	    public ResolveIssueResponse execute() throws IOException {
	        String path = "/issues/resolve?gender=female&issue_type=" + 
	        			URLEncoder.encode(mIssueType, "UTF-8") + "&loo_id=" + mLooId;
	        String url = constructUrl(path);
	        HttpGet request = new HttpGet(url);
	        authorizeHttpMethod(request);
	        ResolveIssueResponse resp = processHttpRequest(request);
	        return resp;
	    }

	    @Override
	    protected ResolveIssueResponse parseResponse(HttpResponse httpResponse) throws IOException {
	    	ResolveIssueResponse response = new ResolveIssueResponse();
	        if(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED)
	        {
	        }
	        return  response;

	    }
}
