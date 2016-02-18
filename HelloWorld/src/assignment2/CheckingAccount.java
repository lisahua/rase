package assignment2;

import java.text.DecimalFormat;

class CheckingAccount {

	//instance variables
		protected double balance;
		
		//accountNumber
		//constructors
		public CheckingAccount(double initialBalance)
		{
			balance=initialBalance;
		}

		public CheckingAccount()
		{
			balance=0;
		}
		
		
		//get balance
		public double getCheckingBalance()
		{
			return balance;
		}
		
		//set balance
		public void setCheckingBalance(double newBalance)
		{
			balance=newBalance;
		}
		
		//deposit
		//input: amount that is being deposited in account
		public void depositChecking(double deposit)
		{
			DecimalFormat df = new DecimalFormat("#.00");
			balance+=deposit;
			System.out.println("Deposited $" + df.format(deposit) + " in the Checking Account. Total balance is $"
					+ df.format(balance) + ".");
		}
		
		/**
	     * Update the current balance by subtracting the given amount.
	     * Precondition: the current balance must have at least the amount in it.
	     * Postcondition: the new balance is decreased by the given amount.
	     * @param amount  The amount to subtract
	     */
	    public void withdrawChecking(double amount) 
	    {  
	        if (balance >=  amount)//make sure there's enough money
	            balance = balance - amount; 
	        DecimalFormat df = new DecimalFormat("#.00");
	        System.out.println("Withdrew $" + df.format(amount) + " in the Checking Account. Total balance is $"
					+ df.format(balance) + ".");
	    }
	    
	    
	    //transfer function
	    public String transferChecking(double amount, int testNum, String tempString)
	    {
	    	DecimalFormat df = new DecimalFormat("#.00");
	    	if(testNum == 1)//this means its giving away money
	    	{
	    		balance-=amount;
	    		tempString="Transfered $" + df.format(amount) + " from the Checking Account to";
	    	}
	    	else//getting the money
	    	{
	    		balance += amount;
	    		tempString += " the Checking Account. ";
	    	}
	    	return tempString;
	    }
	    
	    
}
