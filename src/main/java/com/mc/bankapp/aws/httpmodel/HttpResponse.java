package com.mc.bankapp.aws.httpmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.mc.bankapp.aws.ui.modelresp.LoanResponseModel;
import com.mc.bankapp.aws.ui.modelresp.UserResponse;

public class HttpResponse {
	
	private String body;
	private String statusCode = "200";
	private Map<String, String> headers = new HashMap<String, String>();
	
	public HttpResponse() {
		super();
        this.headers.put("Content-Type","application/json");
	}

	public HttpResponse(UserResponse response) {
        this();
        Gson gson = new Gson(); 
        this.body = gson.toJson(response);
    }
	
	public HttpResponse(UserResponse[] responses) {
        this();
        Gson gson = new Gson(); 
        this.body = gson.toJson(responses);
    }
	
	public HttpResponse(LoanResponseModel response) {
        this();
        Gson gson = new Gson(); 
        this.body = gson.toJson(response);
    }
	
	public HttpResponse(List<LoanResponseModel> responses) {
        this();
        Gson gson = new Gson(); 
        this.body = gson.toJson(responses);
    }
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
}
