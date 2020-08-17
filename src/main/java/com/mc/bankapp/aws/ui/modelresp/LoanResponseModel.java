package com.mc.bankapp.aws.ui.modelresp;

public class LoanResponseModel {
	
	private String loanType;
	private float loanAmount;
	private String date;
	private String rateOfInterest;
	private String durationOfLoan;
	private String loanIdentity;
	
	
	public String getLoanIdentity() {
		return loanIdentity;
	}
	public void setLoanIdentity(String loanIdentity) {
		this.loanIdentity = loanIdentity;
	}
	public String getLoanType() {
		return loanType;
	}
	public void setLoanType(String loanType) {
		this.loanType = loanType;
	}
	public float getLoanAmount() {
		return loanAmount;
	}
	public void setLoanAmount(float loanAmount) {
		this.loanAmount = loanAmount;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getRateOfInterest() {
		return rateOfInterest;
	}
	public void setRateOfInterest(String rateOfInterest) {
		this.rateOfInterest = rateOfInterest;
	}
	public String getDurationOfLoan() {
		return durationOfLoan;
	}
	public void setDurationOfLoan(String durationOfLoan) {
		this.durationOfLoan = durationOfLoan;
	}
	
	
	

}
