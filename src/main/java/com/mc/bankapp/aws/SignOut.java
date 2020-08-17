package com.mc.bankapp.aws;

import java.util.HashMap;
import java.util.Iterator;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.mc.bankapp.aws.httpmodel.HttpRequest;
import com.mc.bankapp.aws.httpmodel.HttpResponse;
import com.mc.bankapp.aws.ui.model.LoggedDetails;

public class SignOut implements RequestHandler<HttpRequest, HttpResponse> {
	private DynamoDB dynamoDB;
	private String BANK_TABLE_NAME = "BankUser";

    @Override
    public HttpResponse handleRequest(HttpRequest request, Context context) {
    	this.initDynamoDbClient();
    	
    	String identity = (String) request.getPathParameters().get("userId");
    	boolean is_user_exist = CommonFunctions.check_user_exist(identity);
    	if(!is_user_exist) {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setBody("No Such User "+identity);
			httpResponse.setStatusCode("403");
			return httpResponse;
		}
    	
    	
    	String pk = findPrimaryKey(identity);
    	clear_session_details(pk);
    	HttpResponse httpResponse = new HttpResponse();
		httpResponse.setBody("Session Details Cleared");
		httpResponse.setStatusCode("200");
		return httpResponse;
    }

    private String findPrimaryKey(String identity) {
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
       
        return iterator.next().getString("EmailAddress");
        
	}

	private void clear_session_details(String pk) {
    	Table table = dynamoDB.getTable(BANK_TABLE_NAME);
    	UpdateItemSpec updateItemSpec = new UpdateItemSpec()
    			.withPrimaryKey("EmailAddress", pk)
                .withUpdateExpression("set AuthToken = :token")
                .withValueMap(new ValueMap()
                		.withString(":token",""))
                .withReturnValues(ReturnValue.UPDATED_NEW);
    	table.updateItem(updateItemSpec);
		
	}

	private void initDynamoDbClient() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		this.dynamoDB = new DynamoDB(client);
	}
}
