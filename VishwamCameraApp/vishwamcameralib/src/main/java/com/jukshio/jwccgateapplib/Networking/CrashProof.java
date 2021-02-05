package com.jukshio.jwccgateapplib.Networking;

import org.json.JSONObject;

import okhttp3.Response;

/**
 * This is data class or POJO used for hold network call responses on AsyncTask and retrieve it back on Main or UI thread.
 * **/
public class CrashProof {

    public String errorType;
    public Response response;
    String referenceid;
    String requestid;
    String bodyStatusCode;
    String responseCode;
    JSONObject responseBodyJson, responseHeadJson;
    int exceptionCode;

    public JSONObject getResponseHeadJson() {
        return responseHeadJson;
    }

    public void setResponseHeadJson(JSONObject responseHeadJson) {
        this.responseHeadJson = responseHeadJson;
    }

    public CrashProof(String errorType, Response response, int exceptionCode ) {
        this.errorType = errorType;
        this.response = response;
        this.exceptionCode = exceptionCode;

    }

    public void setExceptionCode(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public Response getResponse() {
        return response;
    }

    public String getReferenceid() {
        return referenceid;
    }

    public void setReferenceid(String referenceid) {
        this.referenceid = referenceid;
    }

    public String getRequestid() {
        return requestid;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid;
    }


    public String getBodyStatusCode() {
        return bodyStatusCode;
    }

    public void setBodyStatusCode(String bodyStatusCode) {
        this.bodyStatusCode = bodyStatusCode;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCodeCode) {
        this.responseCode = responseCodeCode;
    }

    public JSONObject getResponseBodyJson() {
        return responseBodyJson;
    }

    public void setResponseBodyJson(JSONObject responseBodyJson) {
        this.responseBodyJson = responseBodyJson;
    }
}
