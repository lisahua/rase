package assignment2;
import java.text.DecimalFormat;

import javax.swing.JOptionPane;


public class Driver {

	public static void main(String[] args)
	{
		
		Customer[] customer = new Customer[10];//create an array of 10 elements
		int keepGoing;
		String[] parsedStringArray;
		
		do
		{
			//get the output and parse it into a string array
			String tempString = JOptionPane.showInputDialog("Please Enter Your Transaction:");
			
			parsedStringArray = parseTheString(tempString);
			int tempCount = parsedStringArray.length;
			if(tempCount != 0 && parsedStringArray[0] != "-1")//-1 signifies invalid input
			{
				int customerID=Integer.parseInt(parsedStringArray[0]);//get the ID number
				if(customer[customerID] == null)//if it is an empty customer then create a new customer
				{
					customer[customerID] = new Customer(customerID);
				}
				executeInput(customer[customerID], parsedStringArray);
			}	
			
			//see if user wants to keep going
			keepGoing = JOptionPane.showConfirmDialog(null, "Proceed with the next transaction? (Y/N)", "Continue?", JOptionPane.YES_NO_OPTION);		
		} while(keepGoing == JOptionPane.YES_OPTION);
		for(int count = 0; count < 10; count++)
		{
			if (customer[count] != null)//print the stuff
			{
				System.out.println("Customer "+ count+" has a final Auto Loan balance of "+customer[count].autoLoan.getAutoLoanBalance() + ".");
				System.out.println("Customer "+ count+" has a final Checking Accountbalance of "+customer[count].checkingAccount.getCheckingBalance() + ".");
				System.out.println("Customer "+ count+" has a final Primary Savings Account balance of "+customer[count].primarySavings.getPrimarySavingsBalance() + ".");
				System.out.println("Customer "+ count+" has a final Student Loan balance of "+customer[count].studentLoan.getStudentLoanBalance() + ".");
			}
		}
	}
	
	//LOOK AT THE CASE WHEN NO INPUT IS GIVEN AND WHEN SPACE IS FIRST******************
	//parse the string
	//Maybe put -1 into the first part of array to signify it failed?
		static public String[] parseTheString(String tempString)
		{
		
			String[] parsedString = tempString.split("\\s+");//parse the text by the number of spaces
			int arrayCount = parsedString.length;//get the array length
			String holdString = "";
			//take care of the space cases
			if(arrayCount != 0)
			{
				holdString = parsedString[0];//holds a temp string to be tested
			}
			
			
			
			//make sure the first number is integer from 0-9
			if(arrayCount > 2 && Character.isDigit(holdString.charAt(0)) && Character.isDigit(holdString.charAt(0)) &&
					(Integer.parseInt(holdString) >= 0 && Integer.parseInt(holdString)< 10))
			{
				
				//check for second value correctness
				holdString = parsedString[1];
				arrayCount-=2;
				if(holdString.length() == 1 && holdString.matches("[DIWTG]+") && arrayCount !=0)//correct Input
				{
					holdString = parsedString[2];
					String prevIndex = parsedString[1];
					arrayCount--;
					//check for third values
					//if the second number was a W or D or T
					if(holdString.matches("[.0123456789]+") && prevIndex.matches("[WDT]+") && arrayCount != 0)
					{
						
						//CHECK 4TH NUMBER
						prevIndex=parsedString[2];
						holdString=parsedString[3];
						arrayCount--;
						//make sure the correct input is entered
						if(holdString.matches("[CLAS]+") && arrayCount < 2)
						{
							prevIndex = parsedString[1];
							arrayCount--;
							if(arrayCount == 0 && prevIndex.contains("T"))// IF VALID FOR T
							{
								holdString=parsedString[4];
								if(!(holdString.matches("[CLAS]+")))//invalid for t, otherwise valid
								{
									System.out.println("Invalid last account type or transfer money. Please try again.");
									parsedString[0] = "-1";
								}
							}
							else if(arrayCount == -1 && prevIndex.contains("T"))//invalid input for T
							{
								System.out.println("Invalid number of account type or transfer money. Please try again.");
								parsedString[0] = "-1";
							}
							
						}
						else//incorrect output
						{
							System.out.println("Incorrect account type or too many inputs. Please try again");
							parsedString[0] = "-1";
						}
					}
					//system not good
					else if(!(holdString.matches("[SLAC]+") && prevIndex.matches("[IG]+") && holdString.length() == 1))
					{
						System.out.println("Invalid amount or account type. Please Try again.");
						parsedString[0] = "-1";
					}
					else if(holdString.matches("C") && prevIndex.matches("I"))//cannot add interest to checking
					{
						System.out.println("Cannot add interst to the Checking Account. Please Try again.");
						parsedString[0] = "-1";
					}
				}
				else
				{
					System.out.println("Invalid transaction type. Please Try again.");
					parsedString[0] = "-1";
				}
			}
			else
			{
				System.out.println("Invalid Customer ID. Please try again.");
				if(arrayCount != 0)
				{
					parsedString[0] = "-1";
				}
			}
			
			return parsedString;
		}
		
		static public void executeInput(Customer tempCustomer, String[] tempStringArray)
		{
			if(tempStringArray[1].matches("D"))//deposit function
			{
				if(tempStringArray[3].matches("C"))
				{
					tempCustomer.checkingAccount.depositChecking(Double.parseDouble(tempStringArray[2]));
				}
				else if(tempStringArray[3].matches("L"))
				{
					tempCustomer.studentLoan.depositStudentLoan(Double.parseDouble(tempStringArray[2]));
				}
				else if(tempStringArray[3].matches("A"))
				{
					tempCustomer.autoLoan.depositAutoLoan(Double.parseDouble(tempStringArray[2]));
				}
				else if(tempStringArray[3].matches("S"))
				{
					tempCustomer.primarySavings.depositPrimaySavings(Double.parseDouble(tempStringArray[2]));
				}	
			
					
						
			}
			else if(tempStringArray[1].matches("W"))//withdraw
			{
				if(tempStringArray[3].matches("C"))//checking
				{
					//make sure checking account has enough
					if(tempCustomer.checkingAccount.getCheckingBalance()>=Double.parseDouble(tempStringArray[2]))
					{
						tempCustomer.checkingAccount.withdrawChecking(Double.parseDouble(tempStringArray[2]));
					}
					else if((tempCustomer.checkingAccount.getCheckingBalance() + tempCustomer.primarySavings.getPrimarySavingsBalance())>=
							(20 + Double.parseDouble(tempStringArray[2])))//ensure that no overdraw occurs
					{
						double tempNumber=20 + Double.parseDouble(tempStringArray[2])-tempCustomer.checkingAccount.getCheckingBalance();
						tempCustomer.checkingAccount.withdrawChecking(tempCustomer.checkingAccount.getCheckingBalance());//take out the most it can
						tempCustomer.primarySavings.withdrawPrimarySavings(tempNumber);
					}
					else//error
						System.out.println("Error. Too low of funds to continue. Please try again.");
				}
				else if(tempStringArray[3].matches("L"))
				{
					if(tempCustomer.studentLoan.getStudentLoanBalance()>=Double.parseDouble(tempStringArray[2]))
						tempCustomer.studentLoan.withdrawStudentLoan(Double.parseDouble(tempStringArray[2]));
					else//error
						System.out.println("Error. Too low of funds to continue. Please try again.");
				}
				else if(tempStringArray[3].matches("A"))
				{
					if(tempCustomer.autoLoan.getAutoLoanBalance()>=Double.parseDouble(tempStringArray[2]))
						tempCustomer.autoLoan.withdrawAutoLoan(Double.parseDouble(tempStringArray[2]));
					else//error
						System.out.println("Error. Too low of funds to continue. Please try again.");
				}
				else if(tempStringArray[3].matches("S"))
				{
					if(tempCustomer.primarySavings.getPrimarySavingsBalance()>=Double.parseDouble(tempStringArray[2]))
						tempCustomer.primarySavings.withdrawPrimarySavings(Double.parseDouble(tempStringArray[2]));
					else//error
						System.out.println("Error. Too low of funds to continue. Please try again.");
				}	
			}
			else if(tempStringArray[1].matches("I"))//interest rate
			{
				if(tempStringArray[2].matches("L"))
				{
					if(tempCustomer.studentLoan.getStudentLoanBalance() >= 1000)//must have over this balance
					{
						tempCustomer.studentLoan.interestStudentLoan();
					}
					else
						System.out.println("Error. Do not have over $1000 in this account.");
				}
				else if(tempStringArray[2].matches("A"))
				{
					if(tempCustomer.autoLoan.getAutoLoanBalance() >= 1000)//must have over this balance
					{
						tempCustomer.autoLoan.interestAutoLoan();
					}
					else
						System.out.println("Error. Do not have over $1000 in this account.");
				}
				else if(tempStringArray[2].matches("S"))
				{
					if(tempCustomer.primarySavings.getPrimarySavingsBalance() >= 1000)//must have over this balance
					{
						tempCustomer.primarySavings.interestPrimarySavings();
					}
					else
						System.out.println("Error. Do not have over $1000 in this account.");
				}
				
				
			}
			else if(tempStringArray[1].matches("T"))
			{
				String tempString="";
				if(tempStringArray[3].matches("C"))//checking
				{
					if(tempCustomer.checkingAccount.getCheckingBalance() >= 
							Double.parseDouble(tempStringArray[2]))//make sure there is enough funds
					{
							if(tempStringArray[4].matches("L"))
							{
								tempString = tempCustomer.checkingAccount.transferChecking(Double.parseDouble(tempStringArray[2]), 1, tempString);
								tempString = tempCustomer.studentLoan.transferStudentLoan(Double.parseDouble(tempStringArray[2]), 0, tempString);
								System.out.println(tempString + "The balance on the Checking account is $" + tempCustomer.checkingAccount.getCheckingBalance() +
										" and the new value to the Student Loans Account is $" + tempCustomer.studentLoan.getStudentLoanBalance() + ".");
							}
							else if(tempStringArray[4].matches("A"))
							{
								tempString = tempCustomer.checkingAccount.transferChecking(Double.parseDouble(tempStringArray[2]), 1, tempString);
								tempString = tempCustomer.autoLoan.transferAutoLoan(Double.parseDouble(tempStringArray[2]), 0, tempString);
								System.out.println(tempString + "The balance on the Checking account is $" + tempCustomer.checkingAccount.getCheckingBalance() +
										" and the new value to the Auto Loan Account is $" + tempCustomer.autoLoan.getAutoLoanBalance() + ".");
							}
							else if(tempStringArray[4].matches("S"))
							{
								tempString = tempCustomer.checkingAccount.transferChecking(Double.parseDouble(tempStringArray[2]), 1, tempString);
								tempString = tempCustomer.primarySavings.transferPrimarySavings(Double.parseDouble(tempStringArray[2]), 0, tempString);
								System.out.println(tempString + "The balance on the Checking account is $" + tempCustomer.checkingAccount.getCheckingBalance() +
										" and the new value to the Primary Savings Account is $" + tempCustomer.primarySavings.getPrimarySavingsBalance() + ".");
							}
					}
					else
					{
						System.out.println("Error. There is not enough funds to complete this transaction.");
					}
					
				}
				else if(tempStringArray[3].matches("A"))
				{
					if(tempCustomer.autoLoan.getAutoLoanBalance() >= 
							Double.parseDouble(tempStringArray[2]))//make sure there is enough funds
					{
						if(tempStringArray[4].matches("C"))
						{
							
							tempString = tempCustomer.autoLoan.transferAutoLoan(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.checkingAccount.transferChecking(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Auto Loan account is $" + tempCustomer.autoLoan.getAutoLoanBalance() +
									" and the new value to the Checking Account is $" + tempCustomer.checkingAccount.getCheckingBalance() + ".");
						}
						else if(tempStringArray[4].matches("L"))
						{
							tempString = tempCustomer.autoLoan.transferAutoLoan(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.studentLoan.transferStudentLoan(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Auto Loan account is $" + tempCustomer.autoLoan.getAutoLoanBalance() +
									" and the new value to the Student Loans Account is $" + tempCustomer.studentLoan.getStudentLoanBalance() + ".");
						}
						else if(tempStringArray[4].matches("S"))
						{
							tempString = tempCustomer.autoLoan.transferAutoLoan(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.primarySavings.transferPrimarySavings(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Auto Loan account is $" + tempCustomer.autoLoan.getAutoLoanBalance() +
									" and the new value to the Primary Savings Account is $" + tempCustomer.primarySavings.getPrimarySavingsBalance() + ".");
						}
					}
					else
					{
						System.out.println("Error. There is not enough funds to complete this transaction.");
					}
				}
				else if(tempStringArray[3].matches("L"))
				{
					if(tempCustomer.studentLoan.getStudentLoanBalance() >= 
							Double.parseDouble(tempStringArray[2]))//make sure there is enough funds
					{
						if(tempStringArray[4].matches("S"))
						{
							tempString = tempCustomer.studentLoan.transferStudentLoan(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.primarySavings.transferPrimarySavings(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Student Loan account is $" + tempCustomer.studentLoan.getStudentLoanBalance() +
									" and the new value to the Primary Savings Account is $" + tempCustomer.primarySavings.getPrimarySavingsBalance() + ".");
						}
						else if(tempStringArray[4].matches("C"))
						{
							tempString = tempCustomer.studentLoan.transferStudentLoan(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.checkingAccount.transferChecking(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Student Loan account is $" + tempCustomer.studentLoan.getStudentLoanBalance() +
									" and the new value to the Checking Account is $" + tempCustomer.checkingAccount.getCheckingBalance() + ".");
						}
						else if(tempStringArray[4].matches("A"))
						{
							tempString = tempCustomer.studentLoan.transferStudentLoan(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.autoLoan.transferAutoLoan(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Student Loan account is $" + tempCustomer.studentLoan.getStudentLoanBalance() +
									" and the new value to the Auto Loan Account is $" + tempCustomer.autoLoan.getAutoLoanBalance() + ".");
						}
					}
					else
					{
						System.out.println("Error. There is not enough funds to complete this transaction.");
					}
				}
				else if(tempStringArray[3].matches("S"))
				{
					if(tempCustomer.primarySavings.getPrimarySavingsBalance() >= 
							Double.parseDouble(tempStringArray[2]))//make sure there is enough funds
					{
						if(tempStringArray[4].matches("A"))
						{
							tempString = tempCustomer.primarySavings.transferPrimarySavings(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.autoLoan.transferAutoLoan(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Primary Savings Account is $" + tempCustomer.primarySavings.getPrimarySavingsBalance() +
									" and the new value to the Auto Loan Account is $" + tempCustomer.autoLoan.getAutoLoanBalance() + ".");
						}
						else if(tempStringArray[4].matches("C"))
						{
							tempString = tempCustomer.primarySavings.transferPrimarySavings(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.checkingAccount.transferChecking(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Primary Savings Account is $" + tempCustomer.primarySavings.getPrimarySavingsBalance() +
									" and the new value to the Checking Account is $" + tempCustomer.checkingAccount.getCheckingBalance() + ".");
						}
						else if(tempStringArray[4].matches("L"))
						{
							tempString = tempCustomer.primarySavings.transferPrimarySavings(Double.parseDouble(tempStringArray[2]), 1, tempString);
							tempString = tempCustomer.studentLoan.transferStudentLoan(Double.parseDouble(tempStringArray[2]), 0, tempString);
							System.out.println(tempString + "The balance on the Primary Savings Account is $" + tempCustomer.primarySavings.getPrimarySavingsBalance() +
									" and the new value to the Student Loan Account is $" + tempCustomer.studentLoan.getStudentLoanBalance() + ".");
						}
						
					}
					else
					{
						System.out.println("Error. There is not enough funds to complete this transaction.");
					}
				}	
			}
			
			
			else if(tempStringArray[1].matches("G"))//get
			{
				 DecimalFormat df = new DecimalFormat("#.00");
				if(tempStringArray[2].matches("S"))
					System.out.println("The Primary Savings account for customer " + tempCustomer.customerNumber + 
							" has a balance of $" + df.format(tempCustomer.primarySavings.getPrimarySavingsBalance()));
				else if(tempStringArray[2].matches("L"))
					System.out.println("The Student Loan account for customer " + tempCustomer.customerNumber + 
							" has a balance of $" +df.format(tempCustomer.studentLoan.getStudentLoanBalance()));
				else if(tempStringArray[2].matches("A"))
					System.out.println("The Auto Loan account for customer " + tempCustomer.customerNumber + 
							" has a balance of $" + df.format(tempCustomer.autoLoan.getAutoLoanBalance()));
				else if(tempStringArray[2].matches("C"))
					System.out.println("The Checking account for customer" + tempCustomer.customerNumber +
							" has a balance of $" + df.format(tempCustomer.checkingAccount.getCheckingBalance()));
			}
		}
		
}
