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

public class CommonFunctions {
	
	private static String LOAN_TABLE_NAME = "LoanTable";
	private static String BANK_TABLE_NAME = "BankUser";
	
	private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
	private static DynamoDB dynamoDB = new DynamoDB(client);
	
	
	public static boolean check_user_exist(String identity) {
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
        
        if(iterator.hasNext()) return true;
		return false;
	}

	public static boolean validateTheUserbyAuth(String identity, String oAuth) {
		
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
        
        while(iterator.hasNext()) {
        	Item item = iterator.next();
        	String storedToken = item.getString("AuthToken");
        	if(!storedToken.isEmpty() && oAuth.equals(storedToken)) {
        		return true;
        	}
        }
        
        return false;
	}

public static boolean validateTheUser(String headerUser, String headerPass) {
		
	Table table = dynamoDB.getTable(BANK_TABLE_NAME);
	HashMap<String, String> nameMap = new HashMap<String, String>();
    nameMap.put("#yr", "EmailAddress");

    HashMap<String, Object> valueMap = new HashMap<String, Object>();
    valueMap.put(":yyyy", headerUser);

    QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#yr = :yyyy").withNameMap(nameMap)
        .withValueMap(valueMap);
    
    ItemCollection<QueryOutcome> items = table.query(querySpec);
    Iterator<Item>iterator = items.iterator();       
        if(iterator.hasNext()) {
        	Item item = iterator.next();
        	String storedPassword = item.getString("Password");
        	if(headerPass.equals(storedPassword)) {
        		return true;
        	}
        }
        
        return false;
	}

}
