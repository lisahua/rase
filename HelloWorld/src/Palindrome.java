
public class Palindrome {

	/**
	 * get the palindrome
	 * @param input input string
	 * @return
	 */
	public boolean getPalindrome(String input) {
		System.out.println(input);
		String trimInput = input.replace(",", "").replace("'", "");
		trimInput = trimInput.replace(" ", "").toLowerCase();
		String reverseOutput = new StringBuilder(trimInput).reverse().toString();
		return reverseOutput.equals(trimInput);
	}
}
