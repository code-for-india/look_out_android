package com.cfi.lookout.request;

//import org.apache.http

import android.content.Context;
import android.util.Log;

import com.cfi.lookout.response.IApiResponse;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseApiServerRequest<U extends IApiResponse> {



    protected int statusCode;
    protected U responseObj = null;
    protected HttpResponse httpResponse;
    protected Map<String, String> extraHeaders = new HashMap<String, String>();
    private static final int TIMEOUT_MILLIS = 5000;
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String DEFAULT_USERAGENT = "api_client";
    private final String TAG = "ServiceRequestComp";
    private long reqStart;
    private long reqEnd;
    private long reqTime; //used for logging
    private static final HttpParams DEFAULT_PARAMS;
    protected Context context;
    //TODO: review this and make sure we want these as default
    static {
        DEFAULT_PARAMS = new BasicHttpParams();
        DEFAULT_PARAMS.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        DEFAULT_PARAMS.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_MILLIS);
        DEFAULT_PARAMS.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, DEFAULT_CHARSET);
        DEFAULT_PARAMS.setParameter(CoreProtocolPNames.USER_AGENT, DEFAULT_USERAGENT);
    }

    protected enum RequestType{
        GET,
        POST,
        PUT
    }

    public BaseApiServerRequest(Context context) {
        this.context = context;
    }


    /**
     * If executing the same request multiple times, remember to call reset in
     * between. Without cleaning the request between executions a bad result
     * after a successful one will go unnoticed.
     *
     * @return
     * @throws IOException
     */
    abstract public U execute() throws IOException;


    protected U processHttpRequest(HttpRequestBase request) throws IOException {
        statusCode = executeRequest(request);
        this.responseObj = parseResponse(httpResponse);
        responseObj.setHttpStatusCode(httpResponse.getStatusLine().getStatusCode());
        responseObj.setHttpStatusText(httpResponse.getStatusLine().getReasonPhrase());
        return responseObj;
    }

    /*
     * not implemented in serviceRequest class
     * 
     */
    protected U processHttpRequest(HttpRequestBase request, List<NameValuePair> params) throws IOException, HttpException {
        return this.processHttpRequest(request);
    }

    protected int executeRequest(HttpRequestBase request) throws IOException {

        reqStart = System.currentTimeMillis();
        DefaultHttpClient client = new DefaultHttpClient(DEFAULT_PARAMS);
        this.httpResponse = client.execute(request);
        reqEnd = System.currentTimeMillis();
        reqTime = reqEnd - reqStart;
        String logString = "[request time] " + reqTime + "millis\n";
        log(logString);
        StatusLine status = httpResponse.getStatusLine();
        if (status.getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {  // Status code >= 400
            log("HTTP status code: " + status);
            String body = getStringFromInputStream(httpResponse.getEntity().getContent());
            log("response body: " + body);
        }else
        {
            log("HTTP status code: " + status);
        }
        return status.getStatusCode();
    }


    // TODO: need to implement this
//    protected int executeRequest(HttpRequestBase request, List<NameValuePair> params) throws HttpException, IOException {
//        this.setHttpHeaders(request);
//        return this.executeRequest(request);
//    }

    private void setHttpHeaders(HttpRequestBase request) {
        for (String headerKey : extraHeaders.keySet()) {
            request.setHeader(headerKey, extraHeaders.get(headerKey));
        }
    }

    protected abstract U parseResponse(HttpResponse httpResponse) throws IOException;



    protected RequestType resolveRequestTypeName(Class<?> requestClass) {
        if (HttpGet.class.getCanonicalName().equals(requestClass.getCanonicalName()))
            return RequestType.GET;
        else if (HttpPost.class.getCanonicalName().equals(requestClass.getCanonicalName()))
            return RequestType.POST;
        else if (HttpPut.class.getCanonicalName().equals(requestClass.getCanonicalName()))
            return RequestType.PUT;
        else
            return RequestType.GET;
    }

    private void log(String msg)
    {
        Log.i(TAG, msg);
    }

    // convert InputStream to String
    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    public String getBodyString()throws IOException
    {
        String body = getStringFromInputStream(httpResponse.getEntity().getContent());
        return body;
    }

}
