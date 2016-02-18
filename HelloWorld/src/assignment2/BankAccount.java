package assignment2;

class BankAccount { //bank account is a subclass of customer   
// instance variables (protected to allow inheriting them)
	/**
	 * A unique number that identifies the account
	 */
	protected int accountNumber;
	
	protected CheckingAccount checkingAccount;
	protected SavingsAccount savingsAccount; 
    
//constructors
    
    /**
     * Create an account with initial parameters.
     * @param acct               The account number
     * @param owner              The owner of the account
     * @param initBalance        The initial balance of the account
     */
    public BankAccount(int acct)
    {
        accountNumber = acct; 
    }
    
    public BankAccount(SavingsAccount savingsAccount1, CheckingAccount checkingAccount1)
    {
    	savingsAccount=savingsAccount1;
    	checkingAccount=checkingAccount1;
    	//accountNumber=idNumber;
    	
    }
    
    public BankAccount()
    {
    }   
    
    public BankAccount(SavingsAccount savingsAccount3)
    {
    	savingsAccount=savingsAccount3;
    }

    
// get and set methods 
    /**
     * @return The account number.
     */
    public int getAccountNumber( )
    {
        return accountNumber;
    }

    
// set: postconditions- these all are used to set new values for the instance variables
    /**
     * Set the account number.
     * @param newAcctNumber The new account number.
     */
    public void setAccountNumber(int newAcctNumber )
    {
        accountNumber = newAcctNumber;
    }
    
}
