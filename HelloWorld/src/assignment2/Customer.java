package assignment2;

class Customer {

	// instance variables (protected to allow inheriting them)
	
	//the name of the customer
	protected String customerName;
	
	//the customer id
	protected int customerNumber;
	
	//the customer address
	protected String customerAddress = "Unknown Address";
	
	//bank account object
	protected BankAccount bankAccount; 
	
	//savings account object
    protected SavingsAccount savingsAccount = new SavingsAccount();
	
    //checking account object
	protected CheckingAccount checkingAccount = new CheckingAccount();
	
	//primary savings object
    protected PrimarySavings primarySavings = new PrimarySavings();
	
    //auto loan object
	protected AutoLoan autoLoan = new AutoLoan();
	
	//student loan object
	protected StudentLoan studentLoan = new StudentLoan();
	
	//constructors
	public Customer(String name, int number, String address, BankAccount bankAccountPassed)
	{
		customerName=name;
		customerNumber=number;
		customerAddress=address;
		bankAccount=bankAccountPassed;
	}
	
	public Customer(int idNumber)
	{
		customerName="John Doe" + idNumber;
		customerNumber = idNumber;
		//BankAccount(savingsAccount, checkingAccount);
		bankAccount = new BankAccount(customerNumber);
	}
	public Customer (BankAccount bankAccount3)
	{
		bankAccount=bankAccount3;
	}
	
	//get methods

	//return the customers name
	public String getCustomerName()
	{
		return customerName;
	}
	
	//return the customers number (positive integer)
	public int getCustomerNumber()
	{
		return customerNumber;
	}
	
	//return the customers address
	public String getCustomerAddress()
	{
		return customerAddress;
	}
	
	//set methods
	//change the customers name
	public void setCustomerName(String name)
	{
		customerName=name;
	}
	
	//change the customer number
	public void setCustomerNumber(int number)
	{
		customerNumber=number;
	}
	
	//change the customers address
	public void setCustomersAddress(String address)
	{
		customerAddress=address;
	}
}
