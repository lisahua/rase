package assignment2;

class SavingsAccount extends BankAccount {
	
	//instance variables
	protected double interestRate;
	
	//constructors
	public SavingsAccount(double interest)
	{
		interestRate=interest;
	}

	//default constructor is 4%
	public SavingsAccount()
	{ 
		interestRate=0.04;
	}
	
	
	//get interest rate
	public double getSavingsInterest()
	{
		return interestRate;
	}
	
	//set interest rate
	public void setSavingsBalance(double newInterest)
	{
		interestRate=newInterest;
	}
}
