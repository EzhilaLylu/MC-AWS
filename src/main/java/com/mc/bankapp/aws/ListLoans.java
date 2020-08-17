package com.mc.bankapp.aws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.mc.bankapp.aws.httpmodel.HttpRequest;
import com.mc.bankapp.aws.httpmodel.HttpResponse;
import com.mc.bankapp.aws.ui.modelresp.LoanResponseModel;

public class ListLoans implements RequestHandler<HttpRequest, HttpResponse> {

	private DynamoDB dynamoDB;
	private String LOAN_TABLE_NAME = "LoanTable";
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
		
		String oAuthToken = (String) request.getHeaders().get("Authorization");
		
		boolean validated_user = CommonFunctions.validateTheUserbyAuth(identity,oAuthToken);
		
		if(!validated_user) {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setBody("User Doesnot have valid session.. Please LogIn");
			httpResponse.setStatusCode("403");
			return httpResponse;
		}
		
		List<LoanResponseModel> response =  new ArrayList<LoanResponseModel>(); 
		response = getDataPresent(identity);

		if(response.isEmpty()) {
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setBody("This user doesnt have any loans");
			httpResponse.setStatusCode("203");
			return httpResponse;
		}
		return new HttpResponse(response);
	}

	
	

	private List<LoanResponseModel> getDataPresent(String identity) {
		List<LoanResponseModel> response =  new ArrayList<LoanResponseModel>();
		String indexname = "LoanIdentity-index";
		Table table = dynamoDB.getTable(LOAN_TABLE_NAME);
		Index index = table.getIndex(indexname);

		HashMap<String, String> nameMap = new HashMap<String, String>();
		nameMap.put("#yr", "LoanIdentity");

		HashMap<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put(":yyyy", identity);

		QuerySpec querySpec1 = new QuerySpec()
				.withKeyConditionExpression("#yr = :yyyy")
				.withNameMap(nameMap)
				.withValueMap(valueMap);
		ItemCollection<QueryOutcome> items = index.query(querySpec1);
		Iterator<Item> iterator = items.iterator();
		
		while (iterator.hasNext()) {
			Item data = iterator.next();
			LoanResponseModel model = new LoanResponseModel();
			model = convert_to_java(data);
			response.add(model);
		}
		return response;

	}

	private LoanResponseModel convert_to_java(Item data) {
		LoanResponseModel resp = new LoanResponseModel();
		resp.setDate(data.getString("Date"));
		resp.setDurationOfLoan(data.getString("DurationOfLoan"));
		resp.setLoanAmount(data.getFloat("LoanAmount"));
		resp.setLoanIdentity(data.getString("LoanIdentity"));
		resp.setLoanType(data.getString("LoanType"));
		resp.setRateOfInterest(data.getString("RateOfInterest"));
		return resp;
		
	}
	
	private void initDynamoDbClient() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		this.dynamoDB = new DynamoDB(client);
	}
    

}
