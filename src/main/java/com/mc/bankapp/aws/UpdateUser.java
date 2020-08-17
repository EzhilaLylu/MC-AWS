package com.mc.bankapp.aws;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.introspect.WithMember;
import com.google.gson.Gson;
import com.mc.bankapp.aws.httpmodel.HttpRequest;
import com.mc.bankapp.aws.httpmodel.HttpResponse;
import com.mc.bankapp.aws.ui.model.UserDetailsModel;
import com.mc.bankapp.aws.ui.modelresp.UserResponse;

public class UpdateUser implements RequestHandler<HttpRequest, HttpResponse> {

	private DynamoDB dynamoDB;
	private String BANK_TABLE_NAME = "BankUser";
	
    @Override
    public HttpResponse handleRequest(HttpRequest request, Context context) {
        
    	this.initDynamoDbClient();
		
		String body = request.getBody();
		Gson gson = new Gson();
		UserDetailsModel userDetailsModel = gson.fromJson(body, UserDetailsModel.class);
		
		String identity  = (String) request.getPathParameters().get("userId");
		
		boolean is_user_exist = CommonFunctions.check_user_exist(identity);
		
		if(!is_user_exist) {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setBody("No Such User "+identity+". Please Register!!");
			httpResponse.setStatusCode("403");
			return httpResponse;
		}
		
		String oAuthToken = (String) request.getHeaders().get("Authorization");
		
		boolean validated_user = CommonFunctions.validateTheUserbyAuth(identity,oAuthToken);
		
		if(!validated_user) {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setBody("User Doesnot have valid session.. Please LogIn");
			httpResponse.setStatusCode("403");
			return httpResponse;
		}
		
		updateData(userDetailsModel);
		UserResponse userResponse = responseCreation(userDetailsModel,identity);
			
		return new HttpResponse(userResponse);	
		
    }
    
    private UserResponse responseCreation(UserDetailsModel userDetailsModel, String identity) {
    	UserResponse userResponse = new UserResponse();
		userResponse.setAccountType(userDetailsModel.getAccountType());
		userResponse.setContactNo(userDetailsModel.getContactNo());
		userResponse.setCountry(userDetailsModel.getCountry());
		userResponse.setDob(userDetailsModel.getDob());
		userResponse.setEmail(userDetailsModel.getEmail());
		userResponse.setName(userDetailsModel.getName());
		userResponse.setState(userDetailsModel.getState());
		userResponse.setPublicUserId(identity);
		return userResponse;
	}

	private void initDynamoDbClient() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		this.dynamoDB = new DynamoDB(client);
	}
    
    private void updateData(UserDetailsModel userDetailsModel) 
  	      throws ConditionalCheckFailedException {
    	Table table = dynamoDB.getTable(BANK_TABLE_NAME);
    	UpdateItemSpec updateItemSpec = new UpdateItemSpec()
    			.withPrimaryKey("EmailAddress", userDetailsModel.getEmail())
                .withUpdateExpression("set AccountType = :accountType, #nme=:name, UserName=:userName, Address=:address, #st=:state, Country=:country, PAN=:pan,ContactNumber=:contactNumber, DateOfBirth=:dateOfBirth")
                .withNameMap(new NameMap()
                		.with("#nme", "Name")
                		.with("#st", "State"))
                .withValueMap(new ValueMap()
                		.withString(":accountType",userDetailsModel.getAccountType())
                		.withString(":name",userDetailsModel.getName())
                		.withString(":userName",userDetailsModel.getUsername())
                		.withString(":address",userDetailsModel.getAddress())
                		.withString(":state",userDetailsModel.getState())
                		.withString(":country",userDetailsModel.getCountry())
                		.withString(":pan",userDetailsModel.getPan())
                		.withString(":contactNumber",userDetailsModel.getContactNo())
                		.withString(":dateOfBirth",userDetailsModel.getDob()))
                .withReturnValues(ReturnValue.UPDATED_NEW);
    	table.updateItem(updateItemSpec);

  	 }
    
   	private boolean checkDataPresent(String email, String identity) {
   		
   		String indexname = "PublicUserId-index";
		Table table = dynamoDB.getTable(BANK_TABLE_NAME);
		Index index = table.getIndex(indexname); 
		
		HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#yr", "PublicUserId");

        HashMap<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(":yyyy", identity);

        QuerySpec querySpec1 = new QuerySpec().withKeyConditionExpression("#yr = :yyyy").withNameMap(nameMap)
                .withValueMap(valueMap);
        ItemCollection<QueryOutcome> items = index.query(querySpec1);
        Iterator<Item>iterator = items.iterator();
        
        if(iterator.hasNext()) {
        	return true;
        }
        return false;
		
	}

}
