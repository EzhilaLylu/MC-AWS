package com.mc.bankapp.aws;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.mc.bankapp.aws.httpmodel.HttpRequest;
import com.mc.bankapp.aws.httpmodel.HttpResponse;
import com.mc.bankapp.aws.ui.model.UserDetailsModel;
import com.mc.bankapp.aws.ui.modelresp.UserResponse;

public class SignUp implements RequestHandler<HttpRequest, HttpResponse> {

	private DynamoDB dynamoDB;
	private String TABLE_NAME = "BankUser";
	private Regions region = Regions.US_EAST_1;

	@Override
	public HttpResponse handleRequest(HttpRequest request, Context context) {
		this.initDynamoDbClient();
		
		String body = request.getBody();
		Gson gson = new Gson();
		UserDetailsModel userDetailsModel = gson.fromJson(body, UserDetailsModel.class);
		
		boolean is_data_not_present = checkDataPresent(userDetailsModel.getEmail());
		
		if(is_data_not_present) {
			userDetailsModel = addPublicUserId(userDetailsModel);
			persistData(userDetailsModel);
			UserResponse userResponse = new UserResponse();
			userResponse.setAccountType(userDetailsModel.getAccountType());
			userResponse.setContactNo(userDetailsModel.getContactNo());
			userResponse.setCountry(userDetailsModel.getCountry());
			userResponse.setDob(userDetailsModel.getDob());
			userResponse.setEmail(userDetailsModel.getEmail());
			userResponse.setName(userDetailsModel.getName());
			userResponse.setState(userDetailsModel.getState());
			userResponse.setPublicUserId(userDetailsModel.getPublicUserId());
			return new HttpResponse(userResponse);			
		}else {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setStatusCode("500");
			httpResponse.setBody("User Already present");
			return httpResponse;
		}
		
		
	}

	private UserDetailsModel addPublicUserId(UserDetailsModel userDetailsModel) {
		final Random RANDOM = new SecureRandom();
	    final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		
	    StringBuilder returnValue = new StringBuilder(10);

	    for (int i = 0; i < 10; i++) {
	        returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
	    }

	    userDetailsModel.setPublicUserId(returnValue.toString());
		return userDetailsModel;
	}

	private boolean checkDataPresent(String email) {
		Table table = dynamoDB.getTable(TABLE_NAME);
		HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#yr", "EmailAddress");

        HashMap<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(":yyyy", email);

        QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#yr = :yyyy").withNameMap(nameMap)
            .withValueMap(valueMap);
        
        ItemCollection<QueryOutcome> items = table.query(querySpec);
        if(items.iterator().hasNext()) {
        	return false;
        }
        return true;
		
	}

	private PutItemOutcome persistData(UserDetailsModel userDetailsModel) 
    	      throws ConditionalCheckFailedException {
    	        return this.dynamoDB.getTable(TABLE_NAME)
    	          .putItem(
    	            new PutItemSpec().withItem(new Item()
    	              .withString("AccountType", userDetailsModel.getAccountType())
    	              .withString("PublicUserId", userDetailsModel.getPublicUserId())
    	              .withString("Name", userDetailsModel.getName())
    	              .withString("UserName",userDetailsModel.getUsername())
    	              .withString("Password",userDetailsModel.getPassword())
    	              .withString("Address", userDetailsModel.getAddress())
    	              .withString("State", userDetailsModel.getState())
    	              .withString("Country", userDetailsModel.getCountry())
    	              .withString("EmailAddress", userDetailsModel.getEmail())
    	              .withString("PAN", userDetailsModel.getPan())
    	              .withString("ContactNumber", userDetailsModel.getContactNo())
    	              .withString("DateOfBirth", userDetailsModel.getDob())));
    	    }

	private void initDynamoDbClient() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		this.dynamoDB = new DynamoDB(client);
	}

}
