import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class HelloEWorld {

	public static void main(String[] args) {
		// System.out.println("Input your palindrome:");
		Scanner scanner = new Scanner(System.in);
		Palindrome pld = new Palindrome();
		try {
			scanner = new Scanner(new File("input2.txt"));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				System.out.println(pld.getPalindrome(line));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
