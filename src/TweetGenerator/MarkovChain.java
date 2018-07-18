package TweetGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MarkovChain {
	private Map<String, Map<String, Integer>> table;
	private Map<String,Integer> startWords;
	private final static String NIL = "\0";
	private static Random rand = new Random();
	private TweetDictionary dictionary;
	private final static String[] startSymbols = new String[]{"#","\"","\'","@","("};
	private final static String[] endSymbols = new String[]{",",".","\"","\'","!",")","*","?"};
	private int max, min, startMax, startMin;
	public MarkovChain(TweetDictionary dict) {
		this.dictionary = dict;
		this.table = new HashMap<String,Map<String,Integer>>();
		this.startWords = new HashMap<String,Integer>();
		max = -1;
		min = Integer.MAX_VALUE;

		startMax = -1;
		startMin = Integer.MAX_VALUE;
		System.out.println("Putting tweets into the Table...");

		putAllTweetsInTable();		
	}
	public String writeTweet() {
		ArrayList<String> tweet = new ArrayList<String>();
		tweet.add(NIL);
		int size = 0;
		String lastWord = getStartWord();
		String lastTransition = NIL + lastWord;
		size += lastWord.length();
		tweet.add(lastWord);
		
		while((size <= 1 ||  !tweet.get(tweet.size()-1).equals(NIL)) && size < 280) {
			ArrayList<String> words = new ArrayList<String>();
			ArrayList<Integer> values = new ArrayList<Integer>();
			Set<String> keys = getKeysForTransition(lastTransition);
			for(String word : keys) {
					words.add(word);
					values.add(getCountForWordAtTransition(word, lastTransition));
			}
			int target = rand.nextInt((max - min) + 1) + min; //Random num between min and max
			int index = 0, value = 0;
			String nextWord = NIL;
			if(!words.get(0).equals(NIL)) {
				for(; value < target; index++) {
					if(index >= words.size())
						index = 0;
					value += values.get(index);
				}
				index--;
				if(index < 0)
					index = words.size() - 1;
				nextWord = words.get(index);
			}
			tweet.add(nextWord);
			size += nextWord.length();
			lastTransition = lastWord + nextWord;
			lastWord = nextWord;
		}
		
		return MarkovChain.formatResult(tweet);
}
	private String getStartWord() {
		Random rand = new Random();
		int target = rand.nextInt((max - min) + 1) + min; //Random num between min and max
		int index = 0, value = 0;
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(String word : startWords.keySet()) {
				words.add(word);
				values.add(startWords.get(word));
		}
		
		for(; value < target; index++) {
			if(index >= words.size())
				index = 0;
			value += values.get(index);
		}
		index--;
		if(index < 0)
			index = words.size() - 1;
		return words.get(index);
	}
	public void putTweetInTable(String tweet) {
		ArrayList<String> words = dictionary.getWordsFromTweet(tweet);
		String lastTransition = NIL;
		for(int i = 0; i < words.size()+1; i++) {
			if(lastTransition.equals(NIL)) { //first word
				lastTransition = NIL+words.get(i);
				int value = 1;
				if(startWords.containsKey(words.get(i))) 
					value = startWords.get(words.get(i))+1;
				startWords.put(words.get(i), value);
				
				if(startWords.get(words.get(i)) > startMax)
					startMax = startWords.get(words.get(i));
				if(startWords.get(words.get(i)) < startMin)
					startMin = startWords.get(words.get(i));
			}
			else if(i == words.size()) { //last word
				put(lastTransition,NIL);
			}
			else { //middle words
				put(lastTransition,words.get(i));
				lastTransition = words.get(i-1)+words.get(i);
			}
				
		}
		
	}
	private Set<String> getKeysForTransition(String transition) {
		if(table.containsKey(transition))
			return table.get(transition).keySet();
		Set<String> newSet = new HashSet<String>();
		newSet.add(NIL);
		return newSet;
	}
	private void put(String transition, String word) {
		int value = 1;
		if(table.containsKey(transition)) { //transition already in table
			if(table.get(transition).containsKey(word)) { //word already associated with transition
				value = table.get(transition).get(word) + 1;
				table.get(transition).put(word, value);
			}
			else { //transition not associated with word
				table.get(transition).put(word, value);
			}
		}
		else { //transition not in table
			Map<String, Integer> map = new HashMap<String,Integer>();
			map.put(word, value);
			table.put(transition, map);
		}
		if(value > max) 
			max = value;
		if(value < min) 
			min = value;
}
	private void putAllTweetsInTable() {
		for(String tweet : dictionary.getTweets()) {
			putTweetInTable(tweet.toLowerCase());
		}
	}
	public int getCountForWordAtTransition(String word, String transition) {
		if(table.containsKey(transition) && table.get(transition).containsKey(word))
			return table.get(transition).get(word);
		else 
			return 0;
	}
	public Map<String,Map<String,Integer>> getTable() {
		return table;
	}

	public static String formatResult(ArrayList<String> words) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < words.size(); i++) {
			if(words.get(i).equals("i"))
				words.set(i,"I");
			else if(words.get(i).indexOf("i\'") != -1)
				words.set(i, "I" + words.get(i).substring(1));
			else if(words.get(i).equals("america"))
				words.set(i, "America");
			
			if(isIn(words.get(i),startSymbols)) {
				builder.append(words.get(i));
			}
			else if(i < words.size()-1 && isIn(words.get(i+1),endSymbols)) {
				builder.append(words.get(i));
			}
			else {
				builder.append(words.get(i)).append(" ");
			}
		}
		String result = builder.toString().trim();
		if(result.length() > 0) {
			result = Character.toUpperCase(result.charAt(0))+result.substring(1);
		}
		return result;
	}
	public static boolean isIn(String string, String[] arr) {
		for(String s : arr) 
			if(string.equals(s))
				return true;
			
		return false;
	}
	public static void printTable(MarkovChain chain) {
		File file = new File ("chain.csv"); 
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter (file);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		PrintWriter pWriter = new PrintWriter (fWriter);
		
		
		for(String from : chain.getTable().keySet()) {
			String f = from.trim();
			if(from.equals(NIL))
				f = "NIL";
			pWriter.print(" ");
			for(String to : chain.getTable().get(from).keySet()) {
				String t = to.trim();
				if(to.equals(NIL))
					t = "NIL";
				pWriter.print(t+" ");
			}
			pWriter.println();
			
			pWriter.print(f+" ");
			for(String to : chain.getTable().get(from).keySet()) {
				pWriter.print(chain.getCountForWordAtTransition(to,from)+" ");
			}
			pWriter.println("\n");
		}
		try {
			fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pWriter.close();
	}
	public static void main(String[] args) {
		//Nifty tweeters: DylDTM manacurves ColIegeStudent abominable_andy
		TweetDictionary dict = new TweetDictionary("DylDTM");
		MarkovChain chain = new MarkovChain(dict);
		for(int i = 0; i < 1000; i++) {
			System.out.println(chain.writeTweet() +"\n");
		}
		System.out.println("\nDone Writing Tweets!");
		
	}
	
}
