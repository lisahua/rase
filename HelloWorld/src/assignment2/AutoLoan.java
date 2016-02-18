package assignment2;

import java.text.DecimalFormat;


public class AutoLoan extends SavingsAccount {

	//instance variables
		protected double balance;
		
		//constructors
		public AutoLoan(double initialBalance)
		{
			balance=initialBalance;
		}

		public AutoLoan()
		{
			balance=0;
		}
		
		
		//get balance
		public double getAutoLoanBalance()
		{
			return balance;
		}
		
		//set balance
		public void setAutoLoan(double newBalance)
		{
			balance=newBalance;
		}
		
		/**
	     * Update the current balance by subtracting the given amount.
	     * Precondition: the current balance must have at least the amount in it.
	     * Postcondition: the new balance is decreased by the given amount.
	     * @param amount  The amount to subtract
	     */
	    public void withdrawAutoLoan(double amount) 
	    {  
	        if (balance >=  amount)
	            balance = balance - amount;
	        DecimalFormat df = new DecimalFormat("#.00");
	        System.out.println("Withdrew $" + df.format(amount) + " in the Auto Loan Account. Total balance is $"
					+ df.format(balance) + ".");
	    }
	    
	    //deposit
	    public void depositAutoLoan(double deposit)
	    {
	    	DecimalFormat df = new DecimalFormat("#.00");
	    	balance+=deposit;
	    	System.out.println("Deposited $" + df.format(deposit) + " in the Auto Loan Account. Total balance is $"
					+ df.format(balance) + ".");
	    }
	    
	  //Interest rate
	    public void interestAutoLoan()
	    {
	    	DecimalFormat df = new DecimalFormat("#.00");
	    	double interest=balance*interestRate;//get interest rate
	    	balance+=interest;
	    	 System.out.println("Interest amount of $" + df.format(interest) + " added in the Auto Loan Account. Total balance is $"
						+ df.format(balance) + ".");
	    }
	    
	    //transfer function
	    public String transferAutoLoan(double amount, int testNum, String tempString)
	    {
	    	DecimalFormat df = new DecimalFormat("#.00");
	    	if(testNum == 1)//this means its giving away money
	    	{
	    		balance-=amount;
	    		tempString="Transfered $" + df.format(amount) + " from the Auto Loan account to";
	    	}
	    	else//getting the money
	    	{
	    		balance += amount;
	    		tempString +=" the Auto Loan Account. ";
	    	}
	    	return tempString;
	    }
}
