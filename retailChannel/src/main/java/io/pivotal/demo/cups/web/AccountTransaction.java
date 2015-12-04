package io.pivotal.demo.cups.web;

public class AccountTransaction {

	private String accountId;
	private long dateUTC;
	private double amount;
	private String description;
	
	public AccountTransaction() {
		super();
	}
	public AccountTransaction(String accountId, long dateUTC, double amount, String description) {
		super();
		this.accountId = accountId;
		this.dateUTC = dateUTC;
		this.amount = amount;
		this.description = description;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public long getDateUTC() {
		return dateUTC;
	}
	public void setDateUTC(long dateUTC) {
		this.dateUTC = dateUTC;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
