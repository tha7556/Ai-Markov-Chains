import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Dictionary {
	private ArrayList<String> words;
	public Dictionary(String fileName) {
		words = new ArrayList<String>();
		File file = new File(fileName);
		try {
			Scanner scanner = new Scanner(file);
			while(scanner.hasNextLine()) {
				String s = scanner.nextLine();
				s = s.trim();
				if(!s.contains("-") && !s.contains("'") && s.length() > 1)
					words.add(s.toLowerCase());
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public ArrayList<String> getWords() {
		return words;
	}
	public String toString() {
		String string = "";
		for(int i = 0; i < words.size(); i++) {
			if(i+1 % 12 == 0)
				string += words.get(i)+"\n";
			else
				string +=words.get(i)+"  ";
		}
		return string;
	}
	public Boolean contains(String word) {
		return words.contains(word.toLowerCase());
	}
	public static void main(String[] args) {
		Dictionary dict = new Dictionary("E:\\ComputerScience\\Workspace\\Ai Project2\\Dictionary.txt");
		System.out.println(dict.getWords().size());
	}
}
