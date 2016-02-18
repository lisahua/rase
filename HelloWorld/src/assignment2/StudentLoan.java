package assignment2;

import java.text.DecimalFormat;


public class StudentLoan extends SavingsAccount {

	//instance variables
		protected double balance;
		
		//constructors
		public StudentLoan(double initialBalance)
		{
			balance=initialBalance;
		}

		public StudentLoan()
		{
			balance=0;
		}
		
		
		//get balance
		public double getStudentLoanBalance()
		{
			return balance;
		}
		
		//set balance
		public void setStudentLoanBalance(double newBalance)
		{
			balance=newBalance;
		}
		
		//deposit
		public void depositStudentLoan(double deposit)
		{
			DecimalFormat df = new DecimalFormat("#.00");
			balance+=deposit;
			System.out.println("Deposited $" + df.format(deposit) + " in the Student Loan Account. Total balance is $"
					+ df.format(balance) + ".");
		}
		
		
		/**
	     * Update the current balance by subtracting the given amount.
	     * Precondition: the current balance must have at least the amount in it.
	     * Postcondition: the new balance is decreased by the given amount.
	     * @param amount  The amount to subtract
	     */
	    public void withdrawStudentLoan(double amount) 
	    {  
	        if (balance >=  amount)
	            balance = balance - amount; 
	        DecimalFormat df = new DecimalFormat("#.00");
	    	System.out.println("Withdrew $" + df.format(amount) + " in the Student Loan Account. Total balance is $"
					+ df.format(balance) + ".");
	    }
	    
	  //Interest rate
	    public void interestStudentLoan()
	    {
	    	DecimalFormat df = new DecimalFormat("#.00");
	    	double interest=balance*interestRate;//get interest rate
	    	balance+=interest;
	    	 System.out.println("Interest amount of $" + df.format(interest) + " added in the Student Loan Account. Total balance is $"
						+ df.format(balance) + ".");
	    }
	    
	    //transfer function
	    public String transferStudentLoan(double amount, int testNum, String tempString)
	    {
	    	DecimalFormat df = new DecimalFormat("#.00");
	    	if(testNum == 1)//this means its giving away money
	    	{
	    		balance-=amount;
	    		tempString="Transfered $" + df.format(amount) + " to";
	    	}
	    	else//getting the money
	    	{
	    		balance += amount;
	    		tempString +=" the Student Loan Account. ";
	    	}
	    	return tempString;
	    }
}
