package com.mc.bankapp.aws;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.mc.bankapp.aws.httpmodel.HttpRequest;
import com.mc.bankapp.aws.httpmodel.HttpResponse;
import com.mc.bankapp.aws.ui.model.LoggedDetails;
import com.mc.bankapp.aws.ui.model.UserDetailsModel;

public class SignIn implements RequestHandler<HttpRequest, HttpResponse> {
	private DynamoDB dynamoDB;
	private String BANK_TABLE_NAME = "BankUser";

    @Override
    public HttpResponse handleRequest(HttpRequest request, Context context) {
    	this.initDynamoDbClient();
    	
    	String body = request.getBody();
		Gson gson = new Gson();
		LoggedDetails details  = gson.fromJson(body, LoggedDetails.class);
		boolean validated_user = CommonFunctions.validateTheUser(details.getUser(),details.getPassword());
		
		String token = "";
		if(validated_user) {
			try {
				token = create_token(details.getUser(),details.getPassword());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			update_bank_user(token,details.getUser());
		}else {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setBody("User Not Authorized. Please check Credentials");
			httpResponse.setStatusCode("403");
			return httpResponse;
		}
		Map<String,String> header = new HashMap<String,String>();
		header.put("Authorization", token);
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setBody("Valid User");
		httpResponse.setHeaders(header);
		httpResponse.setStatusCode("200");
		return httpResponse;
        
    }

	
	private void update_bank_user(String token, String email) {
		Table table = dynamoDB.getTable(BANK_TABLE_NAME);
    	UpdateItemSpec updateItemSpec = new UpdateItemSpec()
    			.withPrimaryKey("EmailAddress", email)
                .withUpdateExpression("set AuthToken = :token")
                .withValueMap(new ValueMap()
                		.withString(":token",token))
                .withReturnValues(ReturnValue.UPDATED_NEW);
    	table.updateItem(updateItemSpec);
		
	}


	private String create_token(String username, String password) throws UnsupportedEncodingException {
		String tokenInRaw = username.concat(":").concat(password);
		String token = Base64.getEncoder().encodeToString(tokenInRaw.getBytes("utf-8"));

	    return token;
	}
	
	private void initDynamoDbClient() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		this.dynamoDB = new DynamoDB(client);
	}
}
