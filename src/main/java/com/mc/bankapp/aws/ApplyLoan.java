package com.mc.bankapp.aws;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.mc.bankapp.aws.httpmodel.HttpRequest;
import com.mc.bankapp.aws.httpmodel.HttpResponse;
import com.mc.bankapp.aws.ui.model.LoanDetailsModel;
import com.mc.bankapp.aws.ui.model.UserDetailsModel;
import com.mc.bankapp.aws.ui.modelresp.LoanResponseModel;
import com.mc.bankapp.aws.ui.modelresp.UserResponse;

public class ApplyLoan implements RequestHandler<HttpRequest, HttpResponse> {

	private DynamoDB dynamoDB;
	private String LOAN_TABLE_NAME = "LoanTable";
	private String BANK_TABLE_NAME = "BankUser";
	private Regions region = Regions.US_EAST_1;

	@Override
	public HttpResponse handleRequest(HttpRequest request, Context context) {
		this.initDynamoDbClient();

		String body = request.getBody();
		String identity  = (String) request.getPathParameters().get("userId");
		
		boolean is_user_exist = CommonFunctions.check_user_exist(identity);
		
		if(!is_user_exist) {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setBody("No Such User "+identity);
			httpResponse.setStatusCode("403");
			return httpResponse;
		}
		
		String headerUser = (String) request.getHeaders().get("username");
		String headerPass = (String) request.getHeaders().get("password");
		
		boolean validated_user = CommonFunctions.validateTheUser(identity,headerUser,headerPass);
		
		if(!validated_user) {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setBody("Please Check Username and password");
			httpResponse.setStatusCode("403");
			return httpResponse;
		}
		
		Gson gson = new Gson();
		LoanDetailsModel loanDetailsModel = gson.fromJson(body, LoanDetailsModel.class);
				
		loanDetailsModel.setLoanIdentity(identity);
		loanDetailsModel = addLoanId(loanDetailsModel);
		persistData(loanDetailsModel);
		LoanResponseModel loanResponseModel = responseCreation(loanDetailsModel);
		return new HttpResponse(loanResponseModel);		
		}
		
	

	private LoanResponseModel responseCreation(LoanDetailsModel loanDetailsModel) {
		LoanResponseModel loanResponseModel = new LoanResponseModel();
		loanResponseModel.setDate(loanDetailsModel.getDate());
		loanResponseModel.setDurationOfLoan(loanDetailsModel.getDurationOfLoan());
		loanResponseModel.setLoanAmount(loanDetailsModel.getLoanAmount());
		loanResponseModel.setLoanIdentity(loanDetailsModel.getLoanIdentity());
		loanResponseModel.setLoanType(loanDetailsModel.getLoanType());
		loanResponseModel.setRateOfInterest(loanDetailsModel.getRateOfInterest());
		return loanResponseModel;
	}



	private LoanDetailsModel addLoanId(LoanDetailsModel loanDetailsModel) {
		final Random RANDOM = new SecureRandom();
	    final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		
	    StringBuilder returnValue = new StringBuilder(10);

	    for (int i = 0; i < 10; i++) {
	        returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
	    }

	    loanDetailsModel.setLoanId(returnValue.toString());
		return loanDetailsModel;
	}

	private boolean checkDataPresent(String identity) {
		
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

	private void initDynamoDbClient() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		this.dynamoDB = new DynamoDB(client);
	}
	
	private PutItemOutcome persistData(LoanDetailsModel loanDetailsModel) 
  	      throws ConditionalCheckFailedException {
  	        return this.dynamoDB.getTable(LOAN_TABLE_NAME)
  	          .putItem(
  	            new PutItemSpec().withItem(new Item()
  	              .withString("LoanId", loanDetailsModel.getLoanId())
  	              .withString("LoanType", loanDetailsModel.getLoanType())
  	              .withString("Date",loanDetailsModel.getDate())
  	              .withString("DurationOfLoan", loanDetailsModel.getDurationOfLoan())
  	              .withString("LoanIdentity", loanDetailsModel.getLoanIdentity())
  	              .withString("RateOfInterest", loanDetailsModel.getRateOfInterest())
  	              .withFloat("LoanAmount", loanDetailsModel.getLoanAmount())));
  	    }

}