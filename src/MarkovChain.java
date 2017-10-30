import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MarkovChain {
	private Map<String, Integer> table;
	private Dictionary dict;
	private int average, min, max;
	private final String NIL = "\0";
	public final String[] CHARS = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",NIL};
	public MarkovChain(Dictionary dict) {
		this.dict = dict;
		table = new HashMap<String,Integer>();
		for(int i = 0; i < CHARS.length; i++) {
			for(int j = 0; j < CHARS.length; j++) {
				table.put(CHARS[i]+CHARS[j],0);
			}
		}
		calculateValues(dict);
	}
	public void calculateValues(Dictionary dict) {
		for(String s : dict.getWords()) {
			String[] word = new String[s.length()+2];
			word[0] = NIL;
			for(int i = 1; i < word.length-1; i++) {
				word[i] = s.substring(i-1, i).toLowerCase();
			}
			word[word.length-1] = NIL;	
			
			for(int start = 0, next = 1; next < word.length; start++, next++) {
				int value = table.get(word[start]+word[next]);
				value++;
				table.put(word[start]+word[next], value);
			}
		}
	}
	public String createWord(int maxLength) {
		Random r = new Random();
		ArrayList<String> string = new ArrayList<String>();
		string.add(NIL);
		while((!string.get(string.size()-1).equals(NIL) || string.size() <= 1)) { //&& string.size() <= 2+maxLength) {
			ArrayList<String> letters = new ArrayList<String>();
			ArrayList<Integer> values = new ArrayList<Integer>();
			int maxTarget = getMaxValue()+5;
			int minTarget = getMinValue()+5;
			int target = r.nextInt((max - min) + 1) + min;
			for(String s : CHARS) {
				if(table.get(string.get(string.size()-1)+s) > 0) {
					letters.add(s);
					values.add(table.get(string.get(string.size()-1)+s));
				}
			}
			int i,value;
			for(i = 0,value = 0; value < target; i++) {
				if(i >= letters.size())
					i = 0;
				value += values.get(i);
			}
			i--;
			if(i < 0)
				i = letters.size()-1;
			string.add(letters.get(i));
		}
		//return
		String result = "";
		for(String s : string)
			if(!s.equals(NIL))
				result += s;
		return result;
	}
	public int getAverageValue() {
		if(average == 0) {
			double result = 0.0;
			int num = 0;
			for(int i = 0; i < CHARS.length; i++) {
				for(int j = 0; j < CHARS.length; j++) {
					num++;
					result += table.get(CHARS[i]+CHARS[j]);
				}
			}
			average = (int)(result / (double)num);
			return average;
		}
		return average;
	}
	public int getMaxValue() {
		if(max == 0) {
			int currentMax = 0;
			for(int i = 0; i < CHARS.length; i++) {
				for(int j = 0; j < CHARS.length; j++) {
					if(currentMax < table.get(CHARS[i]+CHARS[j]))
						currentMax = table.get(CHARS[i]+CHARS[j]);
				}
			}
			max = currentMax;
			return currentMax;
		}
		return max;
	}
	public int getMinValue() {
		if(min == 0) {
			int currentMin = 0;
			for(int i = 0; i < CHARS.length; i++) {
				for(int j = 0; j < CHARS.length; j++) {
					if(currentMin > table.get(CHARS[i]+CHARS[j]))
						currentMin = table.get(CHARS[i]+CHARS[j]);
				}
			}
			min = currentMin;
			return currentMin;
		}
		return min;
	}
	public Integer getSize() {
		return dict.getWords().size();
	}
	public Map<String,Integer> getTable() {
		return table;
	}
	public static void main(String[] args) {
		Dictionary dict = new Dictionary("E:\\ComputerScience\\Github\\Ai-Markov-Chains\\MedDictionary.txt");
		ArrayList<String> result = new ArrayList<String>(dict.getWords().size());
		MarkovChain m = new MarkovChain(dict);
		int i = 0;
		while(result.size() < dict.getWords().size() || 1==1) {
			i++;
			String word = m.createWord(79999);
			System.out.println(word);
			if(dict.contains(word) && !result.contains(word)) {
				result.add(word);
				//System.out.println(word + " " + i);
			}
			//System.out.println(word+"\t\t\t"+result.size()+"vs"+dict.getWords().size());
		}
	}
	//TODO: Fix Randomization? Usually only gets ~4 from original dictionary
}
